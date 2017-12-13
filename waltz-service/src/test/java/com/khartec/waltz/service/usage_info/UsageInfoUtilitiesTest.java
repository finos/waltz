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

package com.khartec.waltz.service.usage_info;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.system.SystemChangeSet;
import com.khartec.waltz.model.usage_info.ImmutableUsageInfo;
import com.khartec.waltz.model.usage_info.UsageInfo;
import com.khartec.waltz.model.usage_info.UsageKind;
import org.jooq.tools.json.ParseException;
import org.junit.Test;

import java.util.Set;

import static com.khartec.waltz.model.usage_info.UsageInfoUtilities.mkChangeSet;
import static com.khartec.waltz.model.usage_info.UsageKind.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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