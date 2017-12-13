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
