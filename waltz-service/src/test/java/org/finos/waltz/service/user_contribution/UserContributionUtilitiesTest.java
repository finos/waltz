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

package org.finos.waltz.service.user_contribution;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.service.user_contribution.UserContributionUtilities.findWindow;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class UserContributionUtilitiesTest {

    @Test
    public void indexOnStartOfBoundaryReturnCorrectWindow(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> startBoundaryWindow = findWindow(list, 1, 10);

        assertArrayEquals(
                new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                startBoundaryWindow.toArray(),
                "Start boundary window is incorrect");
    }


    @Test
    public void indexInMiddleReturnCorrectWindow(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> endBoundaryWindow = findWindow(list, 8, 10);

        assertArrayEquals(
                new Integer[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13},
                endBoundaryWindow.toArray(),
                "Middle boundary window is incorrect");
    }


    @Test
    public void indexOnEndOfBoundaryReturnCorrectWindow(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> endBoundaryWindow = findWindow(list, 15, 10);

        assertArrayEquals(
                new Integer[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
                endBoundaryWindow.toArray(),
                "End boundary window is incorrect");
    }


    @Test
    public void indexNearStartOfBoundaryReturnCorrectWindow(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> endBoundaryWindow = findWindow(list, 3, 10);

        assertArrayEquals(
                new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10},
                endBoundaryWindow.toArray(),
                "Near start boundary window is incorrect");
    }


    @Test
    public void indexNearEndOfBoundaryReturnCorrectWindow(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> endBoundaryWindow = findWindow(list, 12, 10);

        assertArrayEquals(
                new Integer[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
                endBoundaryWindow.toArray(),
                "Near end boundary window is incorrect");
    }


    @Test
    public void oddValueOfWindowSizeReturnsCorrectWindow(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> endBoundaryWindow = findWindow(list, 8, 5);

        assertArrayEquals(
                new Integer[]{6, 7, 8, 9, 10},
                endBoundaryWindow.toArray(),
                "Odd window size does not return correctly");
    }


    @Test
    public void ifWindowSizeGreaterThanListLengthFullListReturned(){
        List<Integer> list = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

        List<Integer> endBoundaryWindow = findWindow(list, 8, 20);

        assertArrayEquals(
                new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
                endBoundaryWindow.toArray(),
                "window size is greater than the length of the list");
    }


}
