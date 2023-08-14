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

package org.finos.waltz.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.MapUtilities.indexBy;


public class EntityReferenceUtilities {

    public static String pretty(EntityReference ref) {
        return String.format(
                "%s [%s/%d]",
                ref.name().orElse("?"),
                ref.kind().name(),
                ref.id());
    }


    public static String safeName(EntityReference ref) {
        String idStr = "[" + ref.id() + "]";
        return ref
                .name()
                .map(n -> n + " " + idStr)
                .orElse(idStr);
    }


    public static boolean sameRef(Optional<EntityReference> refA, EntityReference refB) {
        if (refB == null) {
            return false;
        }
        return refA
                .map(refB::equals)
                .orElse(false);
    }


    public static Map<Long, EntityReference> indexById(Collection<EntityReference> refs) {
        return indexBy(
                refs,
                EntityReference::id);
    }
}
