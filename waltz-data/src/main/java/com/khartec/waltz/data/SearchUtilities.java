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

import com.khartec.waltz.common.StringUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;

public class SearchUtilities
{
    public static List<String> mkTerms(String query) {
        checkNotNull(query, "query cannot be null");
        if(query.length() < 3) return new ArrayList<>();

        String safeQuery = query
                .replace("[", " ")
                .replace("]", " ")
                .replace("'", " ")
                .replace("\"", " ")
                .replace("|", " ")
                .replace("!", " ")
                .replace("%", " ")
                .replace("(", " ")
                .replace(")", " ")
                .replace(",", " ")
                .replace("~", " ");

        String[] terms = safeQuery.split(" ");

        // ensure the first term is at least 3 characters
        if(terms.length == 1 && terms[0].length() < 3) return new ArrayList<>();

        return Stream.of(terms)
                .filter(StringUtilities::notEmpty)
                .collect(toList());
    }


    /**
     * Constructs a RelevancyComparator.  This comparator can be used to refine search results
     * based on the location of the term in the value calculated by the extractor function.  In
     * the result of a tie, the natural sort order of the extracted values will be used.
     * @param extractor
     * @param term
     * @return
     */
    public static <T> Comparator<T> mkRelevancyComparator(Function<T, String> extractor,
                                                               String term) {
        return (a, b) -> {
            String sA = extractor.apply(a).toLowerCase();
            String sB = extractor.apply(b).toLowerCase();

            int idxA = sA.indexOf(term);
            int idxB = sB.indexOf(term);

            boolean onlyInA = idxA != -1 && idxB == -1;
            boolean onlyInB = idxA == -1 && idxB != -1;

            if (onlyInA) {
                return -1;
            } else if (onlyInB) {
                return 1;
            } else if (idxA != idxB) {
                return Integer.compare(idxA, idxB);
            } else {
                return sA.compareTo(sB);
            }
        };
    }


}
