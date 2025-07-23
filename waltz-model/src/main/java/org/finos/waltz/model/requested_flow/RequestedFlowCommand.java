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

package org.finos.waltz.model.requested_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.command.Command;
import org.finos.waltz.model.physical_flow.FlowAttributes;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.immutables.value.Value;

import java.util.Set;

import static java.util.Collections.emptySet;

@Value.Immutable
@JsonSerialize(as = ImmutableRequestedFlowCommand.class)
@JsonDeserialize(as = ImmutableRequestedFlowCommand.class)
public abstract class RequestedFlowCommand {
    public abstract EntityReference source();
    public abstract EntityReference target();
    public abstract String reasonCode();
    public abstract long logicalFlowId();
    public abstract long physicalFlowId();
    public abstract PhysicalSpecification specification();
    public abstract FlowAttributes flowAttributes();
    @Value.Default
    public Set<Long> dataTypeIds() {
        return emptySet();
    }

}
