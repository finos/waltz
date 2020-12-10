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

package com.khartec.waltz.model.software_catalog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSoftwareUsage.class)
@JsonDeserialize(as = ImmutableSoftwareUsage.class)
public abstract class SoftwareUsage implements ProvenanceProvider {

    public abstract long applicationId();
    public abstract long softwareVersionId();
    public abstract long softwarePackageId();

    @Nullable
    public abstract Long licenceId();
}
