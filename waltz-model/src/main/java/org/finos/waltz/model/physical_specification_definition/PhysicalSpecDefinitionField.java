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

package org.finos.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionField.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionField.class)
public abstract class PhysicalSpecDefinitionField implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider {

    public abstract long specDefinitionId();
    public abstract int position();
    public abstract FieldDataType type();
    public abstract Optional<Long> logicalDataElementId();
}
