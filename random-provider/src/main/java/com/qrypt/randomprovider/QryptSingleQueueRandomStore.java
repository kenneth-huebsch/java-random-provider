package com.qrypt.randomprovider;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import sun.security.jca.Providers;
import sun.security.jca.ProviderList;

public class QryptSingleQueueRandomStore implements RandomStore {
    private static final String DEFAULT_API_URL = "https://api-eus.qrypt.com/api/v1/entropy";
    private static final String DEFAULT_TOKEN = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImY0NjEzODdkOTg2ZjQ2OTliOTQzOGI5MTA1MTYwYTliIn0.eyJleHAiOjE3NTUxOTM5NjUsIm5iZiI6MTcyMzY1Nzk2NSwiaXNzIjoiQVVUSCIsImlhdCI6MTcyMzY1Nzk2NSwiZ3JwcyI6WyJQVUIiXSwiYXVkIjpbIlJQUyJdLCJybHMiOlsiUk5EVVNSIl0sImNpZCI6IjBqX2N0cFF3UW9YT0NkLVhaeEZvRiIsImR2YyI6IjNlM2NlZTBlYjVlMDRjNmZiNjM0OWViZDIxNjFmNGE1IiwianRpIjoiMzM5ZWMzNmVkMTlmNGE2YWI3ZWZkMTFiNGI1YzcxMWMiLCJ0eXAiOjN9.Tr_0vh4u0GpnRUFYjsy0Adg_VckMrhssrzfCrS9wmjNZ6PSk8B0xhinO4TCIKVW3xYn7ztssthmWYCj-pA3_NA";
    private static final int DEFAULT_STORE_SIZE = 10000;
    private static final int DEFAULT_MIN_THRESHOLD = 1000;

    private final ConcurrentLinkedQueue<Byte> randomQueue = new ConcurrentLinkedQueue<>();

    /*
        we need to isolate store fill-up execution to a separate thread,
        so that we could thread-local Provider list could be utiilized
     */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RestAPIClient apiClient;
    private List<Provider> defaultNonQryptProviders;
    private final ReentrantLock lock = new ReentrantLock();
    private AtomicBoolean isBeingPopulated = new AtomicBoolean(false);
    private int storeSize;
    private int minThreshold;

    @Override
    public void destroy() {
        System.out.println("Destroying executeService...");
        executorService.shutdown();
    }

    //the store has to absolutely be SINGLETON
    private static QryptSingleQueueRandomStore instance;
    public static QryptSingleQueueRandomStore getInstance() {
        if (instance == null) {
            //thread safety
            synchronized (QryptSingleQueueRandomStore.class) {
                if (instance == null) {
                    String apiUrl = System.getProperty("qrypt.api.url");
                    String token = System.getProperty("qrypt.api.token");
                    if (apiUrl == null)
                        apiUrl = DEFAULT_API_URL;
                    if (token == null)
                        token = DEFAULT_TOKEN;
                    RestAPIClient apiClient = new RestAPIClient(apiUrl, token);
                    instance = new QryptSingleQueueRandomStore(apiClient, DEFAULT_STORE_SIZE, DEFAULT_MIN_THRESHOLD);
                }
            }
        }
        return instance;
    }

    private QryptSingleQueueRandomStore(RestAPIClient apiClient,
                                        Integer storeSize,
                                        Integer minThreshold) {
        System.out.println("....Initializing Random Store....");
        this.apiClient = apiClient;
        if (storeSize!=null)
            this.storeSize=storeSize;
        if (minThreshold!=null)
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
        if (defaultNonQryptProviders==null) {
            try {
                lock.lock();
                if (defaultNonQryptProviders == null) {
                    defaultNonQryptProviders = new ArrayList<>();
                    Provider[] providers = Security.getProviders();
                    if (providers == null || providers.length < 2)
                        throw new RuntimeException("Unable to fetch list of security provider: need at least one more other than Qrypt");

                    for (Provider provider : providers) {
                        if (!(provider instanceof QryptProvider)) {
                            defaultNonQryptProviders.add(provider);
                        }
                    }
                    if (defaultNonQryptProviders.isEmpty())
                        throw new RuntimeException("getDefaultNontQryptProvider: Unable to find non-Qrypt security providers");

                    System.out.println("getDefaultNonQryptProvider: added providers " + defaultNonQryptProviders.toString());
                }
            } finally {
                lock.unlock();
            }
        }
        return defaultNonQryptProviders;
    }

    private void checkOrPopulateStore () {
        //prevent other processes from simultaneously populating the store
        if (randomQueue.size() <= this.minThreshold) {
            System.out.println("checkOrPopulateStore: min threshold met(queueSize="+randomQueue.size()+"), populating store...");
            if (! isBeingPopulated.getAndSet(true)) {
                if (randomQueue.size() <= this.minThreshold) {
                    
                    Future<Boolean> executed = executorService.submit(() -> {
                        List<Provider> providers = getDefaultNonQryptProviders();
                        System.out.println("Populating store started... using providers " + providers.toString());
                        Provider[] pArr = new Provider[providers.size()];
                        providers.toArray(pArr);
                        ProviderList providerList = ProviderList.newList(pArr);
                        Providers.beginThreadProviderList(providerList);

                        try {
                            byte[] res = apiClient.getRandom(storeSize);
                            for (byte b : res) {
                                randomQueue.offer(b);
                            }
                            System.out.println("Populating store finished...");
                            return true;
                        } catch (Exception e) {
                            System.err.println("Error populating store" + e);
                            e.printStackTrace();//TODO: introduce logger used in parent project
                            return false;
                        } finally {
                            //remove this security provider from this thread
                            Providers.endThreadProviderList(providerList);
                        }
                    });

                    //need to wait and check for executed future completion or failure
                    try {
                        if (!executed.get(20, TimeUnit.SECONDS))
                            throw new RuntimeException("Unable to populate store on time");
                    } catch (Exception e) {
                        throw new FailStopException("Unable to get the status of populating store in time", e);
                    }
                }

                isBeingPopulated.set(false);
            }
        }
    }

    @Override
    public void nextBytes(byte[] bytes) {
        int count=0;
        while (!isReady() && count< 20) {
            try {
                System.out.println("Store not ready, sleeping....");
                Thread.sleep(1000);
                count++;
            } catch (InterruptedException ioe) {
            }
        }
        System.out.println("nextBytes: fetching " + bytes.length + " bytes from the store");
        for (int i = 0; i < bytes.length; i++) {
            Byte nextElem = randomQueue.poll();
            if (nextElem == null) {
                throw new RuntimeException("Random store is empty, please retry in a few moments");
            }
            bytes[i] = nextElem;
        }
        //at the end of each operation
        checkOrPopulateStore();
    }

}
