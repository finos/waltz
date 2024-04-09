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

package org.finos.waltz.model.datatype;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Data Type decoration for physical specifications
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDataTypeDecorator.class)
@JsonDeserialize(as = ImmutableDataTypeDecorator.class)
public abstract class DataTypeDecorator implements
        LastUpdatedProvider,
        ProvenanceProvider,
        WaltzEntity,
        IdProvider {

    public abstract EntityReference decoratorEntity();

    public abstract Optional<AuthoritativenessRatingValue> rating();
    public abstract Optional<AuthoritativenessRatingValue> targetInboundRating();

    public abstract Optional<Long> flowClassificationRuleId();
    public abstract Optional<Long> inboundFlowClassificationRuleId();

    @Value.Derived
    public long dataTypeId() { return decoratorEntity().id(); }

    @Value.Derived
    public long dataFlowId() { return entityReference().id(); }

    @Value.Default
    public boolean isReadonly() { return false; };

}
