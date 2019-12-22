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

package com.khartec.waltz.model.changelog;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableChangeLog.class)
@JsonDeserialize(as = ImmutableChangeLog.class)
public abstract class ChangeLog {

    public abstract EntityReference parentReference();
    public abstract String message();
    public abstract String userId();
    public abstract Optional<EntityKind> childKind();
    public abstract Operation operation();


    @Value.Default
    public Severity severity() {
        return Severity.INFORMATION;
    }


    @Value.Default
    public LocalDateTime createdAt() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
}
