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

package org.finos.waltz.model.flow_classification_rule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.Severity;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableFlowClassificationRuleVantagePoint.class)
@JsonDeserialize(as = ImmutableFlowClassificationRuleVantagePoint.class)
public abstract class FlowClassificationRuleVantagePoint {

    public abstract EntityReference vantagePoint();
    public abstract int vantagePointRank();
    @Nullable
    public abstract Long dataTypeId();
    public abstract int dataTypeRank();

    public abstract EntityReference subjectReference();

    public abstract Long ruleId();
    public abstract String classificationCode();
    @Nullable
    public abstract String message();

    @Value.Default
    public Severity messageSeverity() {
        return Severity.INFORMATION;
    };

}