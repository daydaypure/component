package com.chenz.component.threadpool;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 线程池,具有监控功能
 */
public class ThreadMonitorExecutor extends ThreadPoolExecutor {
    /**
     * 执行超时，单位（毫秒）
     */
    private long runTimeout = - 1;

    /**
     * 等待超时，单位（毫秒）
     */
    private long waitTimeout = -1;

    /**
     * 执行超时数量
     */
    private final AtomicLong runTimeoutCounter = new AtomicLong();

    /**
     * 等待超时数量
     */
    private final AtomicLong waitTimeoutCounter = new AtomicLong();

    private final AtomicLong totalCounter = new AtomicLong();

    private ThreadPoolLogger logger = new ThreadPoolLogger(){};

    public ThreadMonitorExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ThreadMonitorExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ThreadMonitorExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ThreadMonitorExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        //记录提交时间
        command = new MonitorRunnable(command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        ((MonitorRunnable) r).setStartExeTime(System.currentTimeMillis());
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        doLog(((MonitorRunnable) r), t);
    }

    private void doLog(MonitorRunnable r, Throwable t) {
        long totalQty = totalCounter.incrementAndGet();
        long waitCost = r.getStartExeTime() - r.getSubmitTime();
        boolean isWaitTimeout = (waitTimeout > 0 && waitCost > waitTimeout);
        long waitTimeoutQty = isWaitTimeout ? waitTimeoutCounter.incrementAndGet() : waitTimeoutCounter.get();
        long runCost = System.currentTimeMillis() - r.getStartExeTime();
        boolean isRunTimeout = (runTimeout > 0 && runCost > runTimeout);
        long runTimeoutQty = isRunTimeout ? runTimeoutCounter.incrementAndGet() : runTimeoutCounter.get();
        ExecutorSnapshot executorSnapshot = new ExecutorSnapshot();
        executorSnapshot.setTotalQty(totalQty);
        executorSnapshot.setWaitCost(waitCost);
        executorSnapshot.setIsWaitTimeout(isWaitTimeout);
        executorSnapshot.setWaitTimeoutQty(waitTimeoutQty);
        executorSnapshot.setRunCost(runCost);
        executorSnapshot.setIsRunTimeout(isRunTimeout);
        executorSnapshot.setRunTimeoutQty(runTimeoutQty);
        executorSnapshot.setThreadName(r.threadName);
        executorSnapshot.setWaitTimeout(this.waitTimeout);
        executorSnapshot.setRunTimeout(this.runTimeout);
        logger.log(executorSnapshot);
    }


    public void setRunTimeout(long runTimeout) {
        this.runTimeout = runTimeout;
    }

    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public void setLogger(ThreadPoolLogger logger) {
        this.logger = logger;
    }

    /**
     *
     */
    public static class MonitorRunnable implements Runnable {
        private String threadName;
        /**
         * runnable
         */
        private final Runnable runnable;
        /**
         * 任务创建(提交)时间
         */
        private final long submitTime;
        /**
         * 任务开始执行时间
         */
        private long startExeTime;

        public MonitorRunnable(Runnable runnable){
            this.runnable = runnable;
            submitTime = System.currentTimeMillis();
        }

        @Override
        public void run(){
            this.threadName = Thread.currentThread().getName();
            runnable.run();
        }

        public long getSubmitTime(){
            return submitTime;
        }

        public void setStartExeTime(long startExeTime){
            this.startExeTime = startExeTime;
        }

        public long getStartExeTime(){
            return startExeTime;
        }

        public String getThreadName() {
            return threadName;
        }
    }

    public interface ThreadPoolLogger {
        default void log(ExecutorSnapshot executorSnapshot){
            String messageTemplate = "%s execute finish. waitCost/waitTimeout: [%d/%d=%b], runCost/runTimeout: [%d/%d=%b], ex: %s, totalQty/waitTimeout/runTimeout: [%d/%d/%d]";
            String msg = String.format(messageTemplate,
                    executorSnapshot.getThreadName(), executorSnapshot.getWaitCost(), executorSnapshot.getWaitTimeout(), executorSnapshot.getIsWaitTimeout(),
                    executorSnapshot.getRunCost(), executorSnapshot.getRunTimeout(), executorSnapshot.getIsRunTimeout(),
                    Optional.ofNullable(executorSnapshot.getT()).map(Throwable::getMessage).orElse(""),
                    executorSnapshot.getTotalQty(), executorSnapshot.getWaitTimeoutQty(), executorSnapshot.getRunTimeoutQty());
            System.out.println(msg);
        }
    }

    @Getter
    @Setter
    public static class ExecutorSnapshot {
        private Long  totalQty;
        private Long waitCost;
        private Boolean isWaitTimeout;
        private Long waitTimeoutQty;
        private Long runCost;
        private Boolean isRunTimeout;
        private Long runTimeoutQty;
        private String threadName;
        private long waitTimeout;
        private long runTimeout;
        private Throwable t;
    }

    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

        scheduledExecutorService.scheduleWithFixedDelay(()->{
            System.out.println("aaaa: " + System.currentTimeMillis());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleWithFixedDelay(()->{
            System.out.println("bbbb: " + System.currentTimeMillis());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);

        ThreadMonitorExecutor executor = new ThreadMonitorExecutor(5, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
        executor.terminated();
        /*
        //设置超时时间
        executor.setWaitTimeout(500);
        executor.setRunTimeout(600);
        Random random = new Random();
        for (int i = 0; i < 1; i++) {
            int finalI = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    int sleepTime = random.nextInt(1000);
                    System.out.println(finalI + " will sleep " + sleepTime);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }*/
    }
}
