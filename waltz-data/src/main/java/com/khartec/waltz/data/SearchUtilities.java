/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data;

import com.khartec.waltz.common.StringUtilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class SearchUtilities
{
    public static List<String> mkTerms(String query) {
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
