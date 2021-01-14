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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.schema.Tables;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectSeekStep1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.COST_KIND;
import static com.khartec.waltz.schema.tables.Cost.COST;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static java.lang.String.format;
import static spark.Spark.get;


@Service
public class EntityCostExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(EntityCostExtractor.class);

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            Tables.COST.ENTITY_ID,
            Tables.COST.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    @Autowired
    public EntityCostExtractor(DSLContext dsl) {

        super(dsl);
    }


    @Override
    public void register() {
        get(mkPath("data-extract", "cost", "kind", ":kind", "id", ":id"), (request, response) -> {

            EntityReference ref = getEntityReference(request);

            SelectSeekStep1<Record, Integer> qry = dsl
                    .select(ENTITY_NAME_FIELD)
                    .select(COST_KIND.NAME.as("kind"))
                    .select(COST.YEAR,
                            COST.AMOUNT,
                            COST.PROVENANCE)
                    .from(COST)
                    .innerJoin(COST_KIND)
                    .on(COST.COST_KIND_ID.eq(COST_KIND.ID))
                    .where(COST.ENTITY_ID.eq(ref.id())
                            .and(COST.ENTITY_KIND.eq(ref.kind().name())))
                    .orderBy(COST.YEAR.desc());

            return writeExtract(
                    mkFilename(ref),
                    qry,
                    request,
                    response);
        });
    }


    private String mkFilename(EntityReference ref) {
        return format("%s-%d-cost-info", ref.kind(), ref.id());
    }


}
