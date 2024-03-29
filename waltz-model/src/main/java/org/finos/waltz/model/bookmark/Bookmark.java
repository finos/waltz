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

package org.finos.waltz.model.bookmark;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableBookmark.class)
@JsonDeserialize(as = ImmutableBookmark.class)
public abstract class Bookmark implements
        EntityKindProvider,
        IdProvider,
        ProvenanceProvider,
        LastUpdatedProvider {

    public abstract Optional<Long> id();
    public abstract EntityReference parent();
    public abstract BookmarkKindValue bookmarkKind();
    public abstract Optional<String> title();
    public abstract Optional<String> url();
    public abstract Optional<String> description();

    @Value.Default
    public EntityKind kind() { return EntityKind.BOOKMARK; }

    @Value.Default
    public String provenance() {
        return "waltz";
    }

    @Value.Default
    public boolean isPrimary() { return false; }

    @Value.Default
    public boolean isRequired() {
        return false;
    }

    @Value.Default
    public boolean isRestricted() { return false; }


    public EntityReference entityReference() {
        return EntityReference.mkRef(
                EntityKind.BOOKMARK,
                id().orElse(null),
                title().orElse(""),
                description().orElse(""));
    }
}
