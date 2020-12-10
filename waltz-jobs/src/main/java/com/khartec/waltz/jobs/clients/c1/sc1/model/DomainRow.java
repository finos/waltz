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

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.common.hierarchy.FlatNode;
import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.FlagToBoolean;
import com.khartec.waltz.jobs.clients.c1.sc1.parse.StatusToLifecyclePhase;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.application.LifecyclePhase;
import org.apache.poi.ss.usermodel.Row;
import org.immutables.value.Value;

import java.util.Optional;

import static com.khartec.waltz.jobs.XlsUtilities.mapStrCell;
import static com.khartec.waltz.jobs.XlsUtilities.strVal;

@Value.Immutable
public abstract class DomainRow {

    public abstract String domainObjectId();
    public abstract String domainName();

    @Nullable
    public abstract String parentDomainObjectId();

    @Nullable
    public abstract String parentExtId();

    @Nullable
    public abstract String crossReference();

    public String categoryCode() {
        if (parentExtId() == null) return null;
        if (parentExtId().startsWith("K")) return "K";
        if (parentExtId().length() > 2) return parentExtId().substring(0, 3);
        else return null;
    }


    public FlatNode<DomainRow, String> toFlatNode() {
        return new FlatNode(domainObjectId(), StringUtilities.toOptional(parentDomainObjectId()), this);
    }


    public static DomainRow fromRow(Row row) {
        return ImmutableDomainRow
                .builder()
                .domainObjectId(strVal(row, Columns.A))
                .domainName(strVal(row, Columns.B))
                .parentDomainObjectId(strVal(row, Columns.C))
                .parentExtId(strVal(row, Columns.D))
                .crossReference(strVal(row, Columns.F))
                .build();
    }

}
