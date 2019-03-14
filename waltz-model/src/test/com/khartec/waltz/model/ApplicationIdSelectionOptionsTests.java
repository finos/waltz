/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.model;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationIdSelectionOptionsTests {

    @Test
    public void convertsIdSelectionOptionsWhenAppOptions() {
        IdSelectionOptions appOptions = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .entityLifecycleStatuses(ListUtilities.newArrayList(EntityLifecycleStatus.PENDING, EntityLifecycleStatus.ACTIVE))
                .applicationKinds(ListUtilities.newArrayList(ApplicationKind.CUSTOMISED, ApplicationKind.EUC))
                .build();

        ApplicationIdSelectionOptions convertedAppOptions = ApplicationIdSelectionOptions.mkOpts(appOptions);

        Assert.assertEquals(appOptions, convertedAppOptions);
        Assert.assertEquals(SetUtilities.fromArray(ApplicationKind.CUSTOMISED, ApplicationKind.EUC), convertedAppOptions.applicationKinds());
    }


    @Test
    public void convertsIdSelectionOptionsWhenIdOptions() {
        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .entityLifecycleStatuses(ListUtilities.newArrayList(EntityLifecycleStatus.PENDING, EntityLifecycleStatus.ACTIVE))
                .build();

        ApplicationIdSelectionOptions convertedOptions = ApplicationIdSelectionOptions.mkOpts(options);

        ApplicationIdSelectionOptions targetAppOptions = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .entityLifecycleStatuses(ListUtilities.newArrayList(EntityLifecycleStatus.PENDING, EntityLifecycleStatus.ACTIVE))
                .applicationKinds(SetUtilities.fromArray(ApplicationKind.values()))
                .build();

        Assert.assertEquals(targetAppOptions, convertedOptions);
    }


    @Test
    public void mkOptsCorrect() {
        ApplicationIdSelectionOptions optionsWithScope = ApplicationIdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 1), HierarchyQueryScope.CHILDREN);

        ApplicationIdSelectionOptions targetOptions = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        Assert.assertEquals(targetOptions, optionsWithScope);

        ApplicationIdSelectionOptions optionsNoScope = ApplicationIdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 1));

        Assert.assertEquals(targetOptions, optionsNoScope);
    }

}
