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
import com.khartec.waltz.jobs.clients.c1.sc1.parse.WartungstatusToMaintenanceStatus;
import org.apache.poi.ss.usermodel.Row;
import org.immutables.value.Value;

import static com.khartec.waltz.jobs.XlsUtilities.mapStrCell;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;

@Value.Immutable
public abstract class ComponentRow {

    /*
    0  Anwendungs-ID
    1  Anwendung
    2  Anwendung Version
    3  Anwendung Unternehmenskritisch
    4  Anwendung QS-Datum Technische Architektur
    5  Tier
    6  Layer
    7  Komponente Objekt-ID
    8  Komponente
    9  Komponente Version
    10 Komponente Client-Server-Kennzeichen
    11 Komponente Eigentümer
    12 Komponente Wartungsstatus
    13 Komponente Enddatum
    14 Komponente QS-Datum
    */

    public abstract String tier();
    public abstract String layer();

    public abstract String internalId();
    public abstract String name();
    public abstract String version();
    public abstract String category();
    public abstract String owner();
    public abstract MaintenanceStatus maintenanceStatus();
    public abstract String endDate();
    public abstract String reviewDate();


    public static ComponentRow fromRow(Row row) {
        return ImmutableComponentRow.builder()
                .tier(strVal(row, Columns.F))
                .layer(strVal(row, Columns.G))
                .internalId(strVal(row, Columns.H))
                .name(strVal(row, Columns.I))
                .version(strVal(row, Columns.J))
                .category(strVal(row, Columns.K))
                .owner(strVal(row, Columns.L))
                .maintenanceStatus(mapStrCell(row, Columns.M, WartungstatusToMaintenanceStatus::apply))
                .endDate(strVal(row, Columns.N))
                .reviewDate(strVal(row, Columns.O))
                .build();
    }
}
