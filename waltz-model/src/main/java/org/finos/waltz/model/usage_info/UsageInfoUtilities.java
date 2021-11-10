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

package org.finos.waltz.model.usage_info;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.model.system.ImmutableSystemChangeSet;
import org.finos.waltz.model.system.SystemChangeSet;
import org.jooq.lambda.tuple.Tuple;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.CollectionUtilities.find;
import static org.finos.waltz.common.SetUtilities.intersection;

public class UsageInfoUtilities {

    public static SystemChangeSet<UsageInfo, UsageKind> mkChangeSet(Set<UsageInfo> base, Set<UsageInfo> replacements) {

        Set<UsageKind> baseKinds = SetUtilities.map(base, ui -> ui.kind());
        Set<UsageKind> replacementKinds = SetUtilities.map(replacements, ui -> ui.kind());

        Set<UsageKind> newKinds = SetUtilities.minus(replacementKinds, baseKinds);


        Set<UsageKind> deletes = replacements.stream()
                .filter(ui -> ! ui.kind().isReadOnly())
                .filter(ui -> ! ui.isSelected())  // if it is selected we can't delete
                .filter(ui -> StringUtilities.isEmpty(ui.description()))
                .map(r -> r.kind())
                .collect(Collectors.toSet());

        Set<UsageInfo> updates = intersection(baseKinds, replacementKinds)
                .stream()
                .map(potentiallyUpdatedKind -> Tuple.tuple(
                        find(ui -> ui.kind() == potentiallyUpdatedKind, base),
                        find(ui -> ui.kind() == potentiallyUpdatedKind, replacements)))
                .filter(t -> t.v1.isPresent() && t.v2.isPresent())
                .filter(t -> ! t.v1.get().equals(t.v2.get()))
                .map(t -> t.v2().get())
                .filter(ui -> ! deletes.contains(ui.kind()))
                .collect(Collectors.toSet());

        Collection<UsageInfo> inserts = replacements.stream()
                .filter(r -> newKinds.contains(r.kind()))
                .filter(r -> StringUtilities.notEmpty(r.description()) || r.isSelected())
                .collect(Collectors.toSet());

        return ImmutableSystemChangeSet.<UsageInfo, UsageKind>builder()
                .inserts(inserts)
                .updates(updates)
                .deletes(deletes)
                .build();
    }
}
