package com.cybersource.sample;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RunSampleWithTPS {
    static ExecutorService executorService;
    private static int tps;
    static boolean shutdownTriggered;
    static AtomicInteger txnTPS = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Start time >> "+ new Date());
        tps = Integer.parseInt(args[0]);
        registerShutdownHook();

        //enable below if you enable apache http client logging.
        enableApacheHttpClientLogging();

        executorService = Executors.newFixedThreadPool(100);

        TPSCalculator tpsCalculator = startAndGetTpsCalculator();

        startLoadRunner();

        Scanner scan = new Scanner(System.in);
        printUserHelpMessage();
        int input = scan.nextInt();
        while (input != 0) {
            changeTPS(input);
            printUserHelpMessage();
            input = scan.nextInt();
        }
        System.out.println("0 entered, processing in-flight transactions before shutting down. Please wait for 15 seconds....");
        shutdownTriggered = true;
        try {
            tpsCalculator.setShutdown();
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static void printUserHelpMessage() {
        System.out.println("Enter any positive number to change tps or 0 to exit.");
    }

    private static void startLoadRunner() {
        Properties props = RunSample.readProperty("cybs.properties");
        TxnThread txnThread = new TxnThread(props);
        Thread txnTh = new Thread(txnThread);
        txnTh.start();
    }

    private static TPSCalculator startAndGetTpsCalculator() {
        TPSCalculator tpsCalculator = new TPSCalculator();
        Thread t = new Thread(tpsCalculator);
        t.start();
        return tpsCalculator;
    }

    private static void enableApacheHttpClientLogging() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
    }

    private static void changeTPS (int input) {
        System.out.println("Changing TPS to " + RunSampleWithTPS.tps);
        tps = input;
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook triggered.");
                try {
                    Thread.sleep(15000);
                    System.out.println("=========================AUTH============================");
                    System.out.println("Total auth transaction executed >> " + RunSample.totalTxnSent);
                    System.out.println("Total auth transaction successfully executed >> " + RunSample.totalSuccessfulTxn);
                    System.out.println("Total auth transaction failure executed >> " + RunSample.totalFailedTxn);
                    System.out.println("=====================================================");

                    System.out.println("=========================AUTH REVERSAL============================");
                    System.out.println("Total auth reversal transaction executed >> " + RunSample.authReversalTotalTxnSent);
                    System.out.println("Total auth reversal transaction successfully executed >> " + RunSample.authReversalTotalSuccessfulTxn);
                    System.out.println("Total auth reversal transaction failure executed >> " + RunSample.authReversalTotalFailedTxn);
                    System.out.println("End time >> "+ new Date());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }

            }
        });
    }

    static void executeTask(Properties props, ExecutorService executorService) {
        List<Callable<String>> tasks = new ArrayList<Callable<String>>();

        System.out.println("Current loop count >>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + RunSampleWithTPS.tps);
        for (int i = 0; i < RunSampleWithTPS.tps; i++) {
            RunCallable trans = new RunCallable(props);
            tasks.add(trans);
        }

        try {
            List<Future<String>> results = executorService.invokeAll(tasks);
            try {

                for (Future future : results) {
                    String requestId = (String)future.get();
                    if (requestId == null) {
                        RunSample.totalFailedTxn.incrementAndGet();
                    } else {
                        RunSample.totalSuccessfulTxn.incrementAndGet();
                    }
                    //System.out.println(LocalDateTime.now().toString() + " Auth Request id : " + future.get());
                    txnTPS.incrementAndGet();
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class RunCallable implements Callable {

    private final Properties cybsProperties;

    RunCallable(Properties cybsProperties) {
        this.cybsProperties = cybsProperties;
    }
    @Override
    public Object call() {
        RunSample.totalTxnSent.incrementAndGet();
        String requestID = RunSample.runAuth(cybsProperties);
        if (requestID != null) {
            RunSample.authReversalTotalTxnSent.incrementAndGet();
            RunSample.runAuthReversal(cybsProperties, requestID, "");
        }
        return requestID;
    }
}

class TPSCalculator implements Runnable {

    private boolean shutdown;
    @Override
    public void run() {
        while(!shutdown) {
            System.out.println("Current TPS >>> " + RunSampleWithTPS.txnTPS.get() / 60);
            RunSampleWithTPS.txnTPS.set(0);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void setShutdown() {
        this.shutdown = true;
    }
}

class TxnThread implements Runnable {
    private Properties props;
    TxnThread(Properties props) {
        this.props = props;
    }

    @Override
    public void run() {
        while (!RunSampleWithTPS.shutdownTriggered) {
            RunSampleWithTPS.executeTask(props, RunSampleWithTPS.executorService);
        }
    }
}