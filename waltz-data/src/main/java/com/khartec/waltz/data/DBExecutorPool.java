/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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
        return executorPool.submit(task);
    }

}
