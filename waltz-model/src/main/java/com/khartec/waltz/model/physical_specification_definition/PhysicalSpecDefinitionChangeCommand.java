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

package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionChangeCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionChangeCommand.class)
public abstract class PhysicalSpecDefinitionChangeCommand implements Command, IdProvider {

    public abstract String version();
    public abstract ReleaseLifecycleStatus status();
    public abstract Optional<String> delimiter();
    public abstract PhysicalSpecDefinitionType type();
}
