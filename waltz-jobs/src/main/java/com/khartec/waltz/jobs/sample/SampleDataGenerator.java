package com.khartec.waltz.jobs.sample;

import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface SampleDataGenerator extends Function<ApplicationContext, Map<String, Integer>> {
}
