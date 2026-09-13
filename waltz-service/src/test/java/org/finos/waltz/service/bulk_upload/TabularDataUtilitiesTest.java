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

package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TabularDataUtilitiesTest {

    @Test
    public void emptyInputIsRejected() {
        assertThrows(IllegalStateException.class, () -> TabularDataUtilities.streamData(null));
        assertThrows(IllegalStateException.class, () -> TabularDataUtilities.streamData(""));
    }


    @Test
    public void duplicateHeadersAreRejected() {
        assertThrows(IllegalStateException.class, () -> TabularDataUtilities.streamData("a,b,a\n1,2,3"));
    }


    @Test
    public void headersAreTrimmedAndRowsExposedByColumnName() {
        List<Row> rows = TabularDataUtilities
                .streamData(" name , kind \nfoo,APPLICATION\nbar,ACTOR")
                .collect(Collectors.toList());

        assertEquals(2, rows.size());

        Row first = rows.get(0);
        assertEquals(asSet("name", "kind"), first.getHeaders());
        assertEquals("foo", first.getValue("name"));
        assertEquals("APPLICATION", first.getValue("kind"));
        assertEquals(2, first.getRowNum());

        Row second = rows.get(1);
        assertEquals("bar", second.getValue("name"));
        assertEquals("ACTOR", second.getValue("kind"));
        assertEquals(3, second.getRowNum());
    }


    @Test
    public void unknownColumnGivesNull() {
        Row row = TabularDataUtilities
                .streamData("a,b\n1,2")
                .findFirst()
                .get();

        assertNull(row.getValue("c"));
    }


    @Test
    public void shortRowsGiveNullForMissingCells() {
        Row row = TabularDataUtilities
                .streamData("a,b,c\n1,2")
                .findFirst()
                .get();

        assertEquals("1", row.getValue("a"));
        assertEquals("2", row.getValue("b"));
        assertNull(row.getValue("c"));
    }


    @Test
    public void tabAndPipeDelimitersAreSupported() {
        Row tabRow = TabularDataUtilities.streamData("a\tb\n1\t2").findFirst().get();
        assertEquals("2", tabRow.getValue("b"));

        Row pipeRow = TabularDataUtilities.streamData("a|b\n1|2").findFirst().get();
        assertEquals("2", pipeRow.getValue("b"));
    }


    @Test
    public void headerOnlyInputGivesNoRows() {
        assertEquals(0, TabularDataUtilities.streamData("a,b").count());
    }

}
