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
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.rating.AuthoritativenessRatingValue;
import org.immutables.value.Value;

/**
 * Data Type decorator rating information from classification rules
 */
@Value.Immutable
@JsonSerialize(as = ImmutableDataTypeDecoratorRatingCharacteristics.class)
@JsonDeserialize(as = ImmutableDataTypeDecoratorRatingCharacteristics.class)
public abstract class DataTypeDecoratorRatingCharacteristics {

        public abstract EntityReference source();
        public abstract EntityReference target();

        public abstract long dataTypeId();

        @Value.Default
        public AuthoritativenessRatingValue sourceOutboundRating(){
            return AuthoritativenessRatingValue.NO_OPINION;
        }

        @Value.Default
        public AuthoritativenessRatingValue targetInboundRating(){
            return AuthoritativenessRatingValue.NO_OPINION;
        };

        @Nullable
        public abstract String outboundMessage();

        @Value.Default
        public Severity outboundMessageSeverity() {
                return Severity.INFORMATION;
        }

        @Nullable
        public abstract String inboundMessage();

        @Value.Default
        public Severity inboundMessageSeverity() {
                return Severity.INFORMATION;
        }

}
