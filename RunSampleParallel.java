package com.cybersource.sample;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RunSampleParallel {
    public static AtomicInteger txnTPS = new AtomicInteger(0);
    public static AtomicInteger totalSuccessfulTxn = new AtomicInteger(0);
    public static AtomicInteger totalFailureTxn = new AtomicInteger(0);
    public static AtomicInteger totalTxnSent = new AtomicInteger(0);
    public static ExecutorService executorService;
    public static int tps;
    public static boolean shutdownTriggered;
    public static void main(String[] args) {
        System.out.println("Start time >> "+ LocalDateTime.now().toString());
        tps = Integer.parseInt(args[0]);
        registerShutdownHook();
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
        executorService = Executors.newFixedThreadPool(100);
        String argument="cybs";
        Properties props = RunSample.readProperty(argument + ".properties");

        TPSCalculator tpsCalculator = new TPSCalculator();
        Thread t = new Thread(tpsCalculator);
        t.start();

        TxnThread txnThread = new TxnThread(props);
        Thread txnTh = new Thread(txnThread);
        txnTh.start();

      /*  while (true) {
                executeTask(props, executorService);
        }*/
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter 1 to stop ");
        int input = scan.nextInt();
        if (input == 1){
            shutdownTriggered = true;
            System.out.println("Received input ");
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown hook triggered.");
                try {
                    Thread.sleep(15000);
                    System.out.println("=====================================================");
                    System.out.println("Total transaction executed >> " + totalTxnSent);
                    System.out.println("Total transaction successfully executed >> " + totalSuccessfulTxn);
                    System.out.println("Total transaction failure executed >> " + totalFailureTxn);
                    System.out.println("=====================================================");
                    System.out.println("End time >> "+ LocalDateTime.now().toString());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    executorService.shutdown();
                }

            }
        });
    }

    public static void executeTask(Properties props, ExecutorService executorService) {
        List<Callable<String>> tasks = new ArrayList<Callable<String>>();

        for (int i = 0; i < RunSampleParallel.tps; i++) {
            RunCallable trans = new RunCallable(props);
            tasks.add(trans);
        }

        List<Future<String>> results = null;

        try {
            results = executorService.invokeAll(tasks);
            try {

                for (Future future : results) {
                    String requestId = (String)future.get();
                    if (requestId == null) {
                        totalFailureTxn.incrementAndGet();
                    } else {
                        totalSuccessfulTxn.incrementAndGet();
                    }
                    System.out.println("Request id : " + future.get());
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
    public Object call() throws Exception {
        RunSampleParallel.totalTxnSent.incrementAndGet();
        return RunSample.runAuth(cybsProperties);
    }
}

class TPSCalculator implements Runnable {

    public boolean shutdown;
    @Override
    public void run() {
        while(!shutdown) {
            System.out.println("Current TPS >>> " + RunSampleParallel.txnTPS.get() / 60);
            RunSampleParallel.txnTPS.set(0);
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class TxnThread implements Runnable {
    Properties props;
    public TxnThread (Properties props) {
        this.props = props;
    }
    @Override
    public void run() {
        while (!RunSampleParallel.shutdownTriggered) {
            RunSampleParallel.executeTask(props, RunSampleParallel.executorService);
        }
    }
}