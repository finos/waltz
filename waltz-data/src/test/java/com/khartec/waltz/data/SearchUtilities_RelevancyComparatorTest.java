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

import com.khartec.waltz.common.ListUtilities;
import org.junit.Test;

import java.util.ArrayList;
import java.util.function.Function;

import static com.khartec.waltz.data.SearchUtilities.mkRelevancyComparator;
import static org.junit.Assert.assertEquals;

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
        Function<String, String> extractor = x -> x.toString();
        list.sort(mkRelevancyComparator(extractor, term));

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
