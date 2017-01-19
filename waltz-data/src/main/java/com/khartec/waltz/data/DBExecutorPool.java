package com.khartec.waltz.data;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DBExecutorPool implements DBExecutorPoolInterface {

    private final ExecutorService executorPool;


    @Autowired
    public DBExecutorPool(int dbPoolMin, int dbPoolMax) {
        executorPool = Executors.newFixedThreadPool(
                Integer.max(dbPoolMax / 2, 1),
                (runnable) -> {
                    Thread t = new Thread(runnable, "DB Executor");
                    t.setDaemon(true);
                    return t;
                });

    }


    @Override
    public <T> Future<T> submit(Callable<T> task) {
        System.out.println("Submit task");
        return executorPool.submit(task);
    }

}
