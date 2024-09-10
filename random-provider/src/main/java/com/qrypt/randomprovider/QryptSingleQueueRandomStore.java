package com.qrypt.randomprovider;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import org.apache.log4j.Level;
import sun.security.jca.Providers;
import sun.security.jca.ProviderList;
import org.apache.log4j.Logger;

public class QryptSingleQueueRandomStore implements RandomStore {
    private static final  Logger logger = Logger.getLogger(QryptSingleQueueRandomStore.class);

    private static final String DEFAULT_API_URL = "https://api-eus.qrypt.com/api/v1/entropy";
    private static final String DEFAULT_TOKEN = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY0NjEzODdkOTg2ZjQ2OTliOTQzOGI5MTA1MTYwYTliIn0.eyJleHAiOjE3NTUxOTM5NjUsIm5iZiI6MTcyMzY1Nzk2NSwiaXNzIjoiQVVUSCIsImlhdCI6MTcyMzY1Nzk2NSwiZ3JwcyI6WyJQVUIiXSwiYXVkIjpbIlJQUyJdLCJybHMiOlsiUk5EVVNSIl0sImNpZCI6IjBqX2N0cFF3UW9YT0NkLVhaeEZvRiIsImR2YyI6IjNlM2NlZTBlYjVlMDRjNmZiNjM0OWViZDIxNjFmNGE1IiwianRpIjoiMzM5ZWMzNmVkMTlmNGE2YWI3ZWZkMTFiNGI1YzcxMWMiLCJ0eXAiOjN9.Tr_0vh4u0GpnRUFYjsy0Adg_VckMrhssrzfCrS9wmjNZ6PSk8B0xhinO4TCIKVW3xYn7ztssthmWYCj-pA3_NA";
    private static final int DEFAULT_STORE_SIZE = 1000;
    private static final int DEFAULT_MIN_THRESHOLD = 200;

    private final ConcurrentLinkedQueue<Byte> randomQueue = new ConcurrentLinkedQueue<>();
    /*
        we need to isolate store fill-up execution to a separate thread,
        so that we could thread-local Provider list could be utiilized
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RestAPIClient apiClient;
    private List<Provider> defaultNonQryptProviders;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean isBeingPopulated = new AtomicBoolean(false);
    private final int storeSize;
    private final int minThreshold;

    @Override
    public void destroy() {
        logger.debug("Destroying executeService...");
        executorService.shutdown();
    }

    //the store has to absolutely be SINGLETON
    private static QryptSingleQueueRandomStore instance;

    private static <T> T getSystemProperty(String property, T defaultValue, Function<String,T> converter) {
        String value = System.getProperty(property);
        if (value == null) {
            return defaultValue;
        }

        try {
            return converter.apply(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * return singleton instance of this class
     * @param client - optional client, primarily for testing purposes
     * @return singleton instance of this store
     */
    public static QryptSingleQueueRandomStore getInstance(RestAPIClient client) {
        if (instance == null) {
            //thread safety
            synchronized (QryptSingleQueueRandomStore.class) {
                if (instance == null) {
                    String apiUrl = getSystemProperty("qrypt.api.url", DEFAULT_API_URL, Function.identity());
                    String token = getSystemProperty("qrypt.api.token",DEFAULT_TOKEN,Function.identity());
                    int storeSize = getSystemProperty("qrypt.store.size",DEFAULT_STORE_SIZE, Integer::valueOf);
                    int storeMinThreshold = getSystemProperty("qrypt.store.min_threshold",DEFAULT_MIN_THRESHOLD,Integer::valueOf);

                    RestAPIClient apiClient = client!=null ? client :new RestAPIClient(apiUrl, token);
                    instance = new QryptSingleQueueRandomStore(apiClient, storeSize, storeMinThreshold);
                }
            }
        }
        return instance;
    }

    private QryptSingleQueueRandomStore(RestAPIClient apiClient,
                                        int storeSize,
                                        int minThreshold) {
        logger.info("Initializing Random Store....");
        this.apiClient = apiClient;
        this.storeSize=storeSize;
        this.minThreshold=minThreshold;
    }

    private boolean isReady() {
        if (randomQueue.size() < minThreshold) {
            checkOrPopulateStore();
            return false;
        }
        return true;
    }

    private List<Provider> getDefaultNonQryptProviders() {
        //using re-entrant lock to make sure we populate defaultNonQryptProviders only once
        if (defaultNonQryptProviders != null) {
            return defaultNonQryptProviders;
        }
        try {
            lock.lock();
            //another thread might have populated providers by the time we acquired this lock
            if (defaultNonQryptProviders != null) {
                return defaultNonQryptProviders;
            }

            defaultNonQryptProviders = new ArrayList<>();
            Provider[] providers = Security.getProviders();
            if (providers == null || providers.length < 2)
                throw new IllegalStateException("Unable to fetch list of security providers: need at least one more other than Qrypt");

            for (Provider provider : providers) {
                if (!(provider instanceof QryptProvider)) {
                    defaultNonQryptProviders.add(provider);
                }
            }
            if (defaultNonQryptProviders.isEmpty())
                throw new IllegalStateException("getDefaultNontQryptProvider: Unable to find non-Qrypt security providers");

            logger.debug("getDefaultNonQryptProvider: added providers " + defaultNonQryptProviders);

        } finally {
            lock.unlock();
        }

        return defaultNonQryptProviders;
    }

    private void checkOrPopulateStore () {
        //prevent other processes from simultaneously populating the store
        if (randomQueue.size() <= this.minThreshold) {
            logger.info("checkOrPopulateStore: min criteria met(queueSize<="+randomQueue.size()+"), populating store...");
            if (! isBeingPopulated.getAndSet(true)) {
                if (randomQueue.size() <= this.minThreshold) {

                    Future<Boolean> executed = executorService.submit(() -> {
                        List<Provider> providers = getDefaultNonQryptProviders();
                        logger.info("Populating store started... using providers " + providers.toString());

                        ProviderList providerList = ProviderList.newList(
                                        providers.toArray(new Provider[providers.size()])
                        );
                        Providers.beginThreadProviderList(providerList);

                        try {
                            for (byte b : apiClient.getRandom(storeSize)) {
                                randomQueue.offer(b);
                            }
                            logger.info("Populating store finished...");
                            return true;
                        } catch (Exception e) {
                            logger.log(Level.FATAL, "Error populating store",  e);
                            return false;
                        } finally {
                            //remove this security provider from this thread
                            Providers.endThreadProviderList(providerList);
                        }
                    });

                    //need to wait (???) and check for executed future completion or failure
                    try {
                        if (!executed.get(20, TimeUnit.SECONDS))
                            throw new StorePopulationException("Unable to populate store on time");
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        throw new StorePopulationException("Unable to get the status of populating store in time", e);
                    }
                }
                isBeingPopulated.set(false);
            }
        }
    }

    @Override
    public void nextBytes(byte[] bytes) {
        //at the beginning of each operation
        waitForStoreReady();

        logger.debug("nextBytes: fetching " + bytes.length + " bytes from the store");
        for (int i = 0; i < bytes.length; i++) {
            Byte nextElem = randomQueue.poll();
            if (nextElem == null) {
                //TODO: execute direct fetch in a separate thread with wait instead of throwing runtime exception
                throw new RuntimeException("Random store is empty, please retry in a few moments");
            }
            bytes[i] = nextElem;
        }
    }

    private void waitForStoreReady() {
        for (int count = 0; !isReady() && count< 20; count++) {
            logAndSleep();
        }
    }

    private void logAndSleep() {
        try {
            logger.info("Store is not ready, still populating, sleeping....");
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

}
