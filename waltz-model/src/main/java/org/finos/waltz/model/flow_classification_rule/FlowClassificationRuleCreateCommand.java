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
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.MessageSeverity;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFlowClassificationRuleCreateCommand.class)
@JsonDeserialize(as = ImmutableFlowClassificationRuleCreateCommand.class)
public abstract class FlowClassificationRuleCreateCommand implements Command, DescriptionProvider {
    public abstract long classificationId();

    @Nullable
    public abstract Long dataTypeId();

    public abstract EntityReference subjectReference();

    public abstract EntityReference parentReference();

    @Nullable
    public abstract MessageSeverity severity();

    @Nullable
    public abstract String message();
}
