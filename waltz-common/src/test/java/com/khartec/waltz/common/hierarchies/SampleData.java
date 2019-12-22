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

package com.khartec.waltz.common.hierarchies;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;

import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;


public class SampleData {

    public static List<FlatNode<Void, String>> TWO_TREES = ListUtilities.newArrayList(
            new FlatNode<>("a", empty(), null),
            new FlatNode<>("b", of("a"), null),
            new FlatNode<>("c", of("b"), null),
            new FlatNode<>("d", of("b"), null),
            new FlatNode<>("e", of("a"), null),
            new FlatNode<>("f", empty(), null),
            new FlatNode<>("g", of("f"), null)
    );


    public static List<FlatNode<Void, String>> CIRCULAR = ListUtilities.newArrayList(
            new FlatNode<>("a", of("c"), null),
            new FlatNode<>("b", of("a"), null),
            new FlatNode<>("c", of("a"), null)
    );


    public static List<FlatNode<Void, String>> SELF_REFERENCE = ListUtilities.newArrayList(
            new FlatNode<>("a", of("a"), null),
            new FlatNode<>("b", of("a"), null),
            new FlatNode<>("c", of("a"), null)
    );

}
