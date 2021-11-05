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

package org.finos.waltz.model.database_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@JsonSerialize(as = ImmutableDatabaseInformation.class)
@JsonDeserialize(as = ImmutableDatabaseInformation.class)
public abstract class DatabaseInformation implements
        IdProvider,
        ProvenanceProvider,
        ExternalIdProvider,
        WaltzEntity,
        CustomEnvironmentAsset,
        EntityKindProvider {


    public abstract String databaseName();
    public abstract String instanceName();
    public abstract String dbmsName();
    public abstract String dbmsVersion();
    public abstract String dbmsVendor();
    public abstract LifecycleStatus lifecycleStatus();


    @Nullable
    public abstract Date endOfLifeDate();


    @Value.Derived
    public EndOfLifeStatus endOfLifeStatus() {
        return EndOfLifeStatus.calculateEndOfLifeStatus(endOfLifeDate());
    }


    @Value.Default
    public EntityKind kind(){
        return EntityKind.DATABASE;
    }


    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.DATABASE)
                .id(id().get())
                .name(databaseName())
                .entityLifecycleStatus(EntityLifecycleStatus.ACTIVE)
                .build();
    }
}
