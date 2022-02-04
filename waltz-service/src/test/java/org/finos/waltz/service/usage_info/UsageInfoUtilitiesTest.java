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

package org.finos.waltz.service.usage_info;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.system.SystemChangeSet;
import org.finos.waltz.model.usage_info.ImmutableUsageInfo;
import org.finos.waltz.model.usage_info.UsageInfo;
import org.finos.waltz.model.usage_info.UsageKind;
import org.jooq.tools.json.ParseException;
import org.junit.jupiter.api.Test;


import java.util.Set;

import static org.finos.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;
import static org.finos.waltz.model.usage_info.UsageKind.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UsageInfoUtilitiesTest {

    private static final UsageInfo originator = ImmutableUsageInfo.builder()
            .kind(ORIGINATOR)
            .description("desc")
            .isSelected(true)
            .build();

    private static final UsageInfo consumer = ImmutableUsageInfo
            .copyOf(originator)
            .withKind(UsageKind.CONSUMER);

    private static final UsageInfo modifier = ImmutableUsageInfo
            .copyOf(originator)
            .withKind(MODIFIER);

    private static final UsageInfo distributor = ImmutableUsageInfo
            .copyOf(originator)
            .withKind(DISTRIBUTOR);


    private static final UsageInfo consumerUpd = ImmutableUsageInfo
            .copyOf(consumer)
            .withDescription("upd");

    private static final UsageInfo distributorUpd = ImmutableUsageInfo
            .copyOf(distributor)
            .withDescription("upd");

    private static final UsageInfo distributorSneaky = ImmutableUsageInfo
            .copyOf(distributor)
            .withDescription("desc");

    private static final UsageInfo modifierUpd = ImmutableUsageInfo
            .copyOf(modifier)
            .withDescription("upd");

    private static final UsageInfo modifierDel = ImmutableUsageInfo
            .copyOf(modifier)
            .withDescription("")
            .withIsSelected(false);


    private static final UsageInfo originatorUpd = ImmutableUsageInfo
            .copyOf(originator)
            .withDescription("upd");


    private static UsageInfo mkEmpty(UsageKind kind) {
        return ImmutableUsageInfo.builder()
                .kind(kind)
                .isSelected(false)
                .build();
    }

    @Test
    public void updatesAndInserts() throws ParseException {

        Set<UsageInfo> base = SetUtilities.fromArray(consumer, distributor);
        Set<UsageInfo> newSet = SetUtilities.fromArray(consumerUpd, distributorUpd, modifier);

        SystemChangeSet<UsageInfo, UsageKind> changes = mkChangeSet(base, newSet);

        assertEquals(2, changes.updates().size());
        assertEquals(1, changes.inserts().size());
        assertEquals(0, changes.deletes().size());

        assertTrue(changes.updates().contains(consumerUpd));
        assertTrue(changes.updates().contains(distributorUpd));
        assertTrue(changes.inserts().contains(modifier));

    }


    @Test
    public void mixedBag() throws ParseException {

        Set<UsageInfo> base = SetUtilities.fromArray(consumer, distributor, modifier);
        Set<UsageInfo> newSet = SetUtilities.fromArray(consumerUpd, distributor, originator, modifierDel);

        SystemChangeSet<UsageInfo, UsageKind> changes = mkChangeSet(base, newSet);
        assertEquals(1, changes.inserts().size());
        assertEquals(1, changes.deletes().size());
        assertEquals(1, changes.updates().size());

        assertTrue(changes.updates().contains(consumerUpd));
        assertTrue(changes.deletes().contains(MODIFIER));
        assertTrue(changes.inserts().contains(originator));

    }

    @Test
    public void sneaky() throws ParseException {

        Set<UsageInfo> base = SetUtilities.fromArray(consumer, distributor);
        Set<UsageInfo> newSet = SetUtilities.fromArray(consumer, distributorSneaky);

        SystemChangeSet<UsageInfo, UsageKind> changes = mkChangeSet(base, newSet);
        assertEquals(0, changes.inserts().size());
        assertEquals(0, changes.deletes().size());
        assertEquals(0, changes.updates().size());

    }

    @Test
    public void hmmmm() throws ParseException {

        Set<UsageInfo> base = SetUtilities.fromArray(consumer);
        Set<UsageInfo> newSet = SetUtilities.fromArray(consumer, mkEmpty(MODIFIER), mkEmpty(ORIGINATOR), mkEmpty(DISTRIBUTOR));

        SystemChangeSet<UsageInfo, UsageKind> changes = mkChangeSet(base, newSet);
        assertEquals(0, changes.inserts().size());
//        assertEquals(0, changes.deletes().size());
//        assertEquals(0, changes.updates().size());

    }






}