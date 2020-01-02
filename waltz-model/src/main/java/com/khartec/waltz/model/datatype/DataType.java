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

package com.khartec.waltz.model.datatype;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableDataType.class)
@JsonDeserialize(as = ImmutableDataType.class)
public abstract class DataType implements
        EntityKindProvider,
        NameProvider,
        DescriptionProvider,
        CodeProvider,
        IdProvider,
        ParentIdProvider,
        WaltzEntity {

    @Value.Default
    public boolean concrete() {
        return true;
    }

    @Value.Default
    public boolean deprecated() {
        return false;
    }

    @Value.Default
    public EntityKind kind() { return EntityKind.DATA_TYPE; }

    @Value.Default
    public boolean unknown() { return false; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.DATA_TYPE)
                .id(id().get())
                .name(name())
                .description(description())
                .build();
    }
}
