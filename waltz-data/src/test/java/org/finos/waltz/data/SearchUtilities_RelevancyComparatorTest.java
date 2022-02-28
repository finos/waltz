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

package org.finos.waltz.data;

import org.finos.waltz.common.ListUtilities;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchUtilities_RelevancyComparatorTest {

    @Test
    public void foo() {
        ArrayList<String> list = ListUtilities.newArrayList(
                "zappy",
                "silly",
                "SAP foo",
                "SAP Baa",
                "MySap",
                "Where is sap",
                "bogus",
                "");

        String term = "sap";
        Function<String, String> extractor = x -> x;
        list.sort(SearchUtilities.mkRelevancyComparator(extractor, term));

        // -- check results

        ArrayList<String> expected = ListUtilities.newArrayList(
                "SAP Baa",
                "SAP foo",
                "MySap",
                "Where is sap",
                "",
                "bogus",
                "silly",
                "zappy"
                );

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), list.get(i));
        }
    }
}
