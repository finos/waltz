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

package com.khartec.waltz.model.authoritativesource;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.command.Command;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAuthoritativeSourceCreateCommand.class)
@JsonDeserialize(as = ImmutableAuthoritativeSourceCreateCommand.class)
public abstract class AuthoritativeSourceCreateCommand implements Command, DescriptionProvider {
    public abstract AuthoritativenessRating rating();
    public abstract long dataTypeId();
    public abstract long applicationId();
    public abstract long orgUnitId();
}
