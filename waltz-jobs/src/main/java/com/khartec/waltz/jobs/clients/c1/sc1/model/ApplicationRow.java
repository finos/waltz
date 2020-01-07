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

package com.khartec.waltz.jobs.clients.c1.sc1.model;

import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.FlagToBoolean;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.StatusToLifecyclePhase;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.application.LifecyclePhase;
import org.apache.poi.ss.usermodel.Row;
import org.immutables.value.Value;

import static com.khartec.waltz.jobs.XlsUtilities.mapStrCell;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;

@Value.Immutable
public abstract class ApplicationRow {

    public abstract String externalId();
    public abstract String internalId();
    public abstract String name();
    public abstract String version();
    public abstract boolean businessCritical();
    public abstract LifecyclePhase lifecyclePhase();

    @Nullable
    public abstract String startDate();

    @Nullable
    public abstract String endDate();


    public static ApplicationRow fromRow(Row row) {
        return ImmutableApplicationRow
                .builder()
                .externalId(strVal(row, Columns.A))
                .internalId(strVal(row, Columns.B))
                .name(strVal(row, Columns.C))
                .version(strVal(row, Columns.D))
                .businessCritical(mapStrCell(row, Columns.E, FlagToBoolean::apply))
                .lifecyclePhase(mapStrCell(row, Columns.F, StatusToLifecyclePhase::apply))
                .build();
    }

}
