package com.khartec.waltz.data;


import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface DBExecutorPoolInterface {

    <T> Future<T> submit(Callable<T> task);
}
