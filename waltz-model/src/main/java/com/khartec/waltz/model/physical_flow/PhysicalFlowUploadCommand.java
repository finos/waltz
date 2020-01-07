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

package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowUploadCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowUploadCommand.class)
public abstract class PhysicalFlowUploadCommand implements
        Command,
        DescriptionProvider {

    // logical flow
    public abstract String source();
    public abstract String target();

    // spec
    public abstract String owner();
    public abstract String name();
    public abstract String format();

    @Nullable
    public abstract String specDescription();

    @Nullable
    public abstract String specExternalId();

    // flow attributes
    public abstract String basisOffset();
    public abstract String criticality();
    public abstract String description();

    @Nullable
    public abstract String externalId();

    public abstract String frequency();
    public abstract String transport();

    public abstract String dataType();
}
