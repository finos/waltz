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

package org.finos.waltz.web.endpoints.extracts;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.web.WebUtilities;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSeekStep1;
import org.jooq.SelectSeekStep2;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.schema.Tables.COST;
import static org.finos.waltz.schema.Tables.COST_KIND;
import static spark.Spark.get;
import static spark.Spark.post;


@Service
public class EntityCostExtractor extends DirectQueryBasedDataExtractor {

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
    private final SettingsService settingsService;


    private static final Logger LOG = LoggerFactory.getLogger(EntityCostExtractor.class);

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            COST.ENTITY_ID,
            COST.ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");

    @Autowired
    public EntityCostExtractor(DSLContext dsl, SettingsService settingsService) {
        super(dsl);
        this.settingsService = settingsService;
    }


    @Override
    public void register() {
        String costsForEntityPath = WebUtilities.mkPath("data-extract", "cost", "kind", ":kind", "id", ":id");
        String costsForSelectorPath = WebUtilities.mkPath("data-extract", "cost", "target-kind", ":kind", "selector");

        String costExportsAllowedSetting = getCostExportsAllowedSetting();

        if(Boolean.valueOf(costExportsAllowedSetting)){
            registerCostsForEntity(costsForEntityPath);
            registerCostsForSelector(costsForSelectorPath);
        } else {
            LOG.info(format("EntityCostExtractor not registered - %s: %s", SettingsService.ALLOW_COST_EXPORTS_KEY, costExportsAllowedSetting));
        };

    }


    private void registerCostsForEntity(String costsForEntityPath) {
        get(costsForEntityPath, (request, response) -> {

            EntityReference ref = WebUtilities.getEntityReference(request);

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


    private void registerCostsForSelector(String costsForSelectorPath) {
        post(costsForSelectorPath, (request, response) -> {

            IdSelectionOptions idSelectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);
            EntityKind targetKind = WebUtilities.getKind(request);

            GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, idSelectionOptions);

            SelectJoinStep<Record1<Integer>> latestYear = DSL
                    .select(DSL.max(COST.YEAR))
                    .from(COST);

            SelectSeekStep2<Record, String, Long> qry = dsl
                    .select(COST.ENTITY_ID.as("Entity ID"),
                            COST.ENTITY_KIND.as("Entity Kind"))
                    .select(ENTITY_NAME_FIELD.as("Name"))
                    .select(COST_KIND.NAME.as("Kind"),
                            COST_KIND.DESCRIPTION.as("Kind Description"))
                    .select(COST.YEAR.as("Year"),
                            COST.AMOUNT.as("Amount"),
                            COST.PROVENANCE.as("Provenance"))
                    .from(COST)
                    .innerJoin(COST_KIND).on(COST.COST_KIND_ID.eq(COST_KIND.ID))
                    .where(COST.ENTITY_ID.in(genericSelector.selector())
                            .and(COST.ENTITY_KIND.eq(genericSelector.kind().name())))
                    .and(COST.YEAR.eq(latestYear))
                    .orderBy(ENTITY_NAME_FIELD.as("Name"), COST.COST_KIND_ID);

            return writeExtract(
                    mkFilename(idSelectionOptions.entityReference()),
                    qry,
                    request,
                    response);
        });
    }


    private String mkFilename(EntityReference ref) {
        return format("%s-%d-cost-info", ref.kind(), ref.id());
    }

    private String getCostExportsAllowedSetting() {
        return settingsService.getValue(SettingsService.ALLOW_COST_EXPORTS_KEY).orElse("true");
    }

}
