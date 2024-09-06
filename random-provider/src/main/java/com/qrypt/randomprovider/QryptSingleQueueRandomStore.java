package com.qrypt.randomprovider;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import sun.security.jca.Providers;
import sun.security.jca.ProviderList;

public class QryptSingleQueueRandomStore implements RandomStore {
    private static final int MIN_THRESHOLD = 1000;
    private final ConcurrentLinkedQueue<Byte> randomQueue = new ConcurrentLinkedQueue<>();

    //need to isolate store fill-up execution in a separate thread,
    //so that we could use thread-local Provider
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RestAPIClient apiClient;
    private List<Provider> defaultNonQryptProviders;
    private final ReentrantLock lock = new ReentrantLock();
    private AtomicBoolean isBeingPopulated = new AtomicBoolean(false);
    private int storeSize = 20000;
    private int minThreshold = 1000;

    public QryptSingleQueueRandomStore(RestAPIClient apiClient,
                                       Integer storeSize,
                                       Integer minThreshold) {
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
            System.out.println("checkOrPopulateStore: min threshold met, populating store...");
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
                        if (!executed.get())
                            throw new RuntimeException("Unable to populate store");
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
