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

package org.finos.waltz.common;

import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class FunctionUtilities_getTimeUsingSupplierTest {

    @Test
    public void timeChecksHowLongAFunctionRunsFor() {
        String result = FunctionUtilities.time(
                "foo",
                Unchecked.supplier(() -> {
                    Thread.sleep(500);
                    return "a";
                }));
        assertEquals("a", result);
    }


    @Test
    public void timeWithFuncRunningForZeroSec() {
        String result = FunctionUtilities.time(
                "foo",
                Unchecked.supplier(() -> {
                    Thread.sleep(0);
                    return "a";
                }));
        assertEquals("a", result);
    }


    @Test
    public void timeDurationOfFuncRunningForNegSec() throws IllegalArgumentException{
        FunctionUtilities.time(
                "foo",
                Unchecked.supplier(() -> {
                    Thread.sleep(-1);
                    return "a";
                }));
    }


    @Test
    public void timeDurationForFuncRunningWithEmptyName(){
        String result = FunctionUtilities.time(
                "",
                Unchecked.supplier(() -> {
                    Thread.sleep(500);
                    return "a";
                }));
        assertEquals("a", result);
    }


    @Test
    public void timeDurationForFuncRunningWithNullName(){
        String result = FunctionUtilities.time(
                null,
                Unchecked.supplier(() -> {
                    Thread.sleep(500);
                    return "a";
                }));
        assertEquals("a", result);
    }
}
