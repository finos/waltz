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

package com.khartec.waltz.model.data_flow_decorator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.FlowDirection;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableDataTypeDirectionKey.class)
@JsonDeserialize(as = ImmutableDataTypeDirectionKey.class)
public abstract class DataTypeDirectionKey {

    public static DataTypeDirectionKey mkKey(Long dataTypeId, FlowDirection flowDirection) {
        return ImmutableDataTypeDirectionKey
                .builder()
                .DatatypeId(dataTypeId)
                .flowDirection(flowDirection)
                .build();
    }

    public abstract Long DatatypeId();
    public abstract FlowDirection flowDirection();

}
