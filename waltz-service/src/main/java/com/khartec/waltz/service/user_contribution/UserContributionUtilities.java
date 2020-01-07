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

package com.khartec.waltz.service.user_contribution;

import com.khartec.waltz.model.tally.OrderedTally;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserContributionUtilities {



    public static List<OrderedTally<String>> getOrderedListOf10(List<OrderedTally<String>> list, String userId){

        Optional<OrderedTally<String>> tallyForUserId = list
                .stream()
                .filter(d -> d.id().equals(userId))
                .findFirst();

        int userRank = tallyForUserId.map(t -> t.index()).orElse(0);

        List<OrderedTally<String>> subsectionOrderedList = findWindow(list, userRank, 10);

        return subsectionOrderedList;
    }

    public static <T> List<T> findWindow(List<T> list, int windowFocus, int windowSize) {

        int maxRange = list.size();
        int minRange = 1;

        int range;
        int upperBound;
        int lowerBound;

        if (windowSize%2 == 0){
            range = windowSize/2;
            upperBound = windowFocus + range;
            lowerBound = windowFocus - (range - 1);
        } else {
            range = (windowSize - 1)/2;
            upperBound = windowFocus + range;
            lowerBound = windowFocus - range;
        }

        if (lowerBound <= minRange) {
            lowerBound = minRange;
            upperBound = windowSize;
        } else if (upperBound >= maxRange) {
            lowerBound = maxRange - windowSize + 1;
            upperBound = maxRange;
        }

        int finalUpperBound = upperBound;
        int finalLowerBound = lowerBound;

        List<T> orderedList = list
                .stream()
                .filter(d -> list.indexOf(d) >= (finalLowerBound - 1)
                && list.indexOf(d) <= (finalUpperBound - 1))
                .collect(Collectors.toList());

        return orderedList;
    }

}

