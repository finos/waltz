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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.ColorUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.rating.RatingScheme;
import org.finos.waltz.schema.tables.records.AssessmentDefinitionRecord;
import org.finos.waltz.schema.tables.records.AssessmentRatingRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeItemRecord;
import org.finos.waltz.schema.tables.records.RatingSchemeRecord;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.schema.Tables.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AssessmentGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        RatingScheme confidentialityRatingScheme = getOrCreateConfidentialityRatingScheme(ctx);
        Long appDefnId = createAppAssessmentDefinition(ctx, confidentialityRatingScheme);

        RatingScheme impactRatingScheme = getOrCreateImpactRatingScheme(ctx);
        Long archAssessmentRating = createArchImpactAssessmentDefinition(ctx, impactRatingScheme);
        Long regAssessmentRating = createRegulatoryImpactAssessmentDefinition(ctx, impactRatingScheme);

        createLotsOfAssessmentDefinitions(ctx, impactRatingScheme);

        createAssessmentRecords(getDsl(ctx), confidentialityRatingScheme, appDefnId, EntityKind.APPLICATION, APPLICATION.ID, 0.9);
        createAssessmentRecords(getDsl(ctx), impactRatingScheme, archAssessmentRating, EntityKind.CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, 0.9);
        createAssessmentRecords(getDsl(ctx), impactRatingScheme, regAssessmentRating, EntityKind.CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, 0.5);


        return null;
    }


    private void createAssessmentRecords(DSLContext dsl,
                                         RatingScheme ratingScheme,
                                         Long assessmentDefnId,
                                         EntityKind targetKind,
                                         TableField<? extends Record, Long> targetIdField,
                                         double density) {
        List<Long> targetIds = loadAllIds(dsl, targetIdField);

        List<AssessmentRatingRecord> records = targetIds
                .stream()
                .filter(d -> RandomUtilities.getRandom().nextDouble() < density)
                .map(id -> tuple(id, randomPick(ratingScheme.ratings())))
                .filter(t -> t.v2.id().isPresent())
                .map(t -> {
                    AssessmentRatingRecord record = new AssessmentRatingRecord();
                    record.setAssessmentDefinitionId(assessmentDefnId);
                    record.setRatingId(t.v2.id().get());
                    record.setEntityKind(targetKind.name());
                    record.setEntityId(t.v1);
                    record.setLastUpdatedBy(SAMPLE_DATA_USER);
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setDescription("sample data");
                    return record;
                })
                .collect(toList());

        log("About to store %d assessments for kind: %s", records.size(), targetIdField.getTable().getName());

        dsl.batchInsert(records).execute();
    }


    private Long createAppAssessmentDefinition(ApplicationContext ctx, RatingScheme ratingScheme) {

        return createAssessmentDefinition(
                getDsl(ctx),
                "Sensitive Data",
                "Indicates if the application contains sensitive data",
                EntityKind.APPLICATION,
                ratingScheme);
    }


    private Long createArchImpactAssessmentDefinition(ApplicationContext ctx, RatingScheme ratingScheme) {
        return createAssessmentDefinition(
                getDsl(ctx),
                "Architectural Impact Assessment",
                "Indicates what impact a change initiative will have on the organizations architecture",
                EntityKind.CHANGE_INITIATIVE,
                ratingScheme);
    }


    private void createLotsOfAssessmentDefinitions(ApplicationContext ctx, RatingScheme ratingScheme) {
        List<Tuple3<EntityKind, String, String>> defns = ListUtilities.newArrayList(
                tuple(EntityKind.APPLICATION, "Sanctions Apply", "Indicates if sanctions apply to this application"),
                tuple(EntityKind.APPLICATION, "Records Retentions Relevancy", "Indicates what record retention rules (if any) apply to this app"),
                tuple(EntityKind.APPLICATION, "Internet facing", "Indicates if an application has a direct connection to the internet"));

        defns.forEach(d -> createAssessmentDefinition(
                getDsl(ctx),
                d.v2,
                d.v3,
                d.v1,
                ratingScheme));
    }


    private Long createRegulatoryImpactAssessmentDefinition(ApplicationContext ctx, RatingScheme ratingScheme) {
        return createAssessmentDefinition(
                getDsl(ctx),
                "Regulatory Impact Assessment",
                "Indicates what impact a change initiative will have on the organizations regulatory obligations",
                EntityKind.CHANGE_INITIATIVE,
                ratingScheme);
    }


    private RatingScheme getOrCreateImpactRatingScheme(ApplicationContext ctx) {
        return getOrCreateRatingScheme(
                ctx,
                "Impact",
                tuple("Z", "No Impact", ColorUtilities.HexStrings.BLUE),
                tuple("S", "Small Impact", ColorUtilities.HexStrings.GREEN),
                tuple("L", "Large Impact", ColorUtilities.HexStrings.AMBER));
    }


    private RatingScheme getOrCreateConfidentialityRatingScheme(ApplicationContext ctx) {
        return getOrCreateRatingScheme(
                    ctx,
                    "Confidentiality",
                    tuple("C", "Confidential", ColorUtilities.HexStrings.AMBER),
                    tuple("S", "Strictly Confidential", ColorUtilities.HexStrings.RED),
                    tuple("I", "Internal", ColorUtilities.HexStrings.BLUE),
                    tuple("P", "Public", ColorUtilities.HexStrings.GREEN),
                    tuple("U", "Unrated", ColorUtilities.HexStrings.GREY));
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        Condition condition = ASSESSMENT_DEFINITION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE);
        SelectConditionStep<Record1<Long>> definitionSelector = DSL
                .select(ASSESSMENT_DEFINITION.ID)
                .from(ASSESSMENT_DEFINITION)
                .where(condition);

        // delete ratings
        getDsl(ctx)
                .deleteFrom(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.in(definitionSelector))
                .execute();

        // delete defns
        getDsl(ctx)
                .deleteFrom(ASSESSMENT_DEFINITION)
                .where(condition)
                .execute();

        return true;
    }



    private RatingScheme getOrCreateRatingScheme(ApplicationContext ctx, String name, Tuple3<String, String, String>... options) {
        DSLContext dsl = getDsl(ctx);
        Long schemeId = dsl
                .select(RATING_SCHEME.ID)
                .from(RATING_SCHEME)
                .where(RATING_SCHEME.NAME.eq(name))
                .fetchOne(RATING_SCHEME.ID);

        if (schemeId == null) {
            schemeId = createRatingScheme(
                    dsl,
                    name,
                    options);
        }

        RatingSchemeService ratingSchemeService = ctx.getBean(RatingSchemeService.class);
        return ratingSchemeService.getById(schemeId);
    }


    private Long createRatingScheme(DSLContext dsl, String name, Tuple3<String, String, String>... options) {
        // create
        RatingSchemeRecord ratingSchemeRecord = dsl.newRecord(RATING_SCHEME);
        ratingSchemeRecord.setName(name);
        ratingSchemeRecord.setDescription(name +" ratings");
        ratingSchemeRecord.insert();

        System.out.println("Inserted scheme "+ ratingSchemeRecord.getId());

        List<RatingSchemeItemRecord> ratingRecords = Stream
                .of(options)
                .map(t -> {
                    RatingSchemeItemRecord itemR = dsl.newRecord(RATING_SCHEME_ITEM);
                    itemR.setCode(t.v1);
                    itemR.setName(t.v2);
                    itemR.setColor(t.v3);
                    itemR.setDescription(t.v2);
                    itemR.setSchemeId(ratingSchemeRecord.getId());
                    itemR.setUserSelectable(true);
                    return itemR;
                })
                .collect(toList());

        dsl.batchInsert(ratingRecords).execute();

        return ratingSchemeRecord.getId();
    }


    private Long createAssessmentDefinition(DSLContext dsl,
                                            String name,
                                            String description,
                                            EntityKind kind,
                                            RatingScheme ratingScheme) {
        AssessmentDefinitionRecord defnRecord = dsl.newRecord(ASSESSMENT_DEFINITION);

        defnRecord.setName(name);
        defnRecord.setDescription(description);
        defnRecord.setEntityKind(kind.name());
        defnRecord.setIsReadonly(true);
        defnRecord.setLastUpdatedBy(SAMPLE_DATA_USER);
        defnRecord.setProvenance(SAMPLE_DATA_PROVENANCE);
        defnRecord.setRatingSchemeId(ratingScheme
                .id()
                .orElseThrow(() -> new RuntimeException("Could not find rating scheme")));

        defnRecord.insert();

        return defnRecord.getId();
    }



}
