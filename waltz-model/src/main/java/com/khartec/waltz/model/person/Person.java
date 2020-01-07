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

package com.khartec.waltz.model.person;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePerson.class)
@JsonDeserialize(as = ImmutablePerson.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Person implements
        EntityKindProvider,
        IdProvider,
        WaltzEntity {

    public abstract String employeeId();
    public abstract String displayName();
    public abstract String email();
    public abstract boolean isRemoved();
    public abstract PersonKind personKind();
    public abstract Optional<String> title();
    public abstract Optional<String> mobilePhone();
    public abstract Optional<String> officePhone();
    public abstract Optional<String> userPrincipalName();
    public abstract Optional<String> managerEmployeeId();
    public abstract Optional<String> departmentName();
    public abstract Optional<Long> organisationalUnitId();

    @Value.Derived
    public String name() {
        return displayName();
    }

    @Value.Default
    public String userId() { //TODO change as part of 247
        return email();
    }

    @Value.Default
    public EntityKind kind() { return EntityKind.PERSON; }

    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.PERSON)
                .id(id().get())
                .name(displayName())
                .entityLifecycleStatus(EntityLifecycleStatus.fromIsRemovedFlag(this.isRemoved()))
                .build();
    }
}
