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

package com.khartec.waltz.jobs.harness;

import java.util.function.Supplier;

public class HarnessUtilities {

    public static <T> T time(String name, Supplier<T> s) {
        System.out.println("-- begin [" + name + "]");

        long st = System.currentTimeMillis();

        try {
            T r = s.get();
            long end = System.currentTimeMillis();

            System.out.println("-- end [" + name + "]");
            System.out.println("-- dur [" + name + "]:" + (end - st));
            System.out.println("-- result [" + name + "]:" + r);
            System.out.println();

            return r;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }
}
