package com.chenz.component.threadpool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 *
 * 划动时间窗批量处理
 * 两种实现思路，前提是“业务线程”需要等待响应
 * 思路1：
 *   "轮循线程"，先sleep，再执行。“业务线程”在add时如果数量超了,"轮循线程"被打断，继续sleep，则需要来处理合并逻辑。 将压力分摊到业务线程上了
 * 思路2：
 *   "轮循线程"，先执行，再sleep。“业务线程”在add时如果数量超了，"轮循线程"被打断，"轮循线程"立马去执行，但是有可能"轮循线程"执行不过来，需要单独开线程处理。但是“业务线程”其实是空闲的
 * 结论：前提是“业务线程”需要等待响应，使用思路1。
 *      如果“业务线程”不需要响应，使用思路2
 */
public class CallerRunsBatchExecutor {

    private long period = 1000L;

    private int maxQty = 10;

    //ScheduledExecutorService
    private final LinkedBlockingQueue<Integer> queue;

    private final Thread loopTask;

    public CallerRunsBatchExecutor() {
        queue = new LinkedBlockingQueue<>(1000);
        loopTask = new Thread(()->{
            while (true) {
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    continue;
                }
                log("======= loop task processData ====");
                processData();
            }
        });
        loopTask.start();
    }

    private void processData() {
        if(queue.isEmpty()) {
            return;
        }
        List<Integer> list = new ArrayList<>(maxQty);
        queue.drainTo(list,maxQty);
        if(list.isEmpty()) {
            return;
        }
        String remainString = queue.stream().map(Object::toString).collect(Collectors.joining(","));
        String proccessedString = list.stream().map(Object::toString).collect(Collectors.joining(","));
        log("======= remain ======" + remainString);
        log("======= procce ======" + proccessedString);
    }

    /**
     *
     * @param i
     * @throws BatchExecutorAddException 添加失败后，需要单独处理
     */
    public void add(Integer i) throws BatchExecutorAddException {
        try {
            queue.add(i);
            if(queue.size() >= maxQty) {
                loopTask.interrupt();
                log("======= add processData ====");
                processData();
            }
        } catch (Exception e) {
            /*
            IllegalStateException–如果由于容量限制，此时无法添加元素
            ClassCastException–如果指定元素的类阻止将其添加到此队列中
            NullPointerException–如果指定的元素为null
            IllegalArgumentException–如果指定元素的某些属性阻止将其添加到此队列中
             */
            throw new BatchExecutorAddException("queue.add failed", e);
        }
    }

    public static class BatchExecutorAddException extends Exception {

        private static final long serialVersionUID = 1L;

        public BatchExecutorAddException() {
        }

        public BatchExecutorAddException(String message) {
            super(message);
        }

        public BatchExecutorAddException(String message, Throwable cause) {
            super(message, cause);
        }

        public BatchExecutorAddException(Throwable cause) {
            super(cause);
        }

        public BatchExecutorAddException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static void log(String msg) {
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        System.out.println(format + ": " + msg);
    }

    public static void main(String[] args) {
        /*BatchExecutor executor = new BatchExecutor();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(random.nextInt(200));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                executor.add(i);
            } catch (BatchExecutorAddException e) {
                log("add failed: " + i);
            }
        }*/
        Thread thread = new Thread(()->{
            while(true) {
                System.out.println("1111");
            }
        });
        thread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread.interrupt();
        System.out.println(thread.isInterrupted());
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}