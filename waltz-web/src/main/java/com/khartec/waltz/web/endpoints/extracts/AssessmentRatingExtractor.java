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
import com.khartec.waltz.data.assessment_definition.AssessmentDefinitionDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record6;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.SetUtilities.asSet;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.post;


@Service
public class AssessmentRatingExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(AssessmentRatingExtractor.class);

    private final Field<String> entityNameField = InlineSelectFieldFactory.mkNameField(
            ASSESSMENT_RATING.ENTITY_ID,
            ASSESSMENT_RATING.ENTITY_KIND,
            asSet(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE));

    private final Field<String> entityExtIdField = InlineSelectFieldFactory.mkExternalIdField(
            ASSESSMENT_RATING.ENTITY_ID,
            ASSESSMENT_RATING.ENTITY_KIND,
            asSet(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE));

    private final Field<String> entityLifecycleField = InlineSelectFieldFactory.mkEntityLifecycleField(
            ASSESSMENT_RATING.ENTITY_ID,
            ASSESSMENT_RATING.ENTITY_KIND,
            asSet(EntityKind.APPLICATION, EntityKind.CHANGE_INITIATIVE));

    private final AssessmentDefinitionDao assessmentDefinitionDao;

    @Autowired
    public AssessmentRatingExtractor(DSLContext dsl, AssessmentDefinitionDao assessmentDefinitionDao) {
        super(dsl);
        this.assessmentDefinitionDao = assessmentDefinitionDao;
    }


    @Override
    public void register() {
        post(mkPath("data-extract", "assessment-rating", "by-definition", ":id"), (request, response) -> {
            long definitionId = getId(request);
            AssessmentDefinition definition = assessmentDefinitionDao.getById(definitionId);
            SelectConditionStep<?> qry = prepareExtractQuery(definitionId);
            String fileName = String.format("assessment-ratings-for-%s",
                    definition.name().toLowerCase());
            LOG.debug("extracted assessment ratings for definition {}", definition.name());
            return writeExtract(fileName, qry, request, response);
        });
    }


    private SelectConditionStep<Record6<Long, String, String, String, String, String>> prepareExtractQuery(Long definitionId) {

        return dsl
                .selectDistinct(
                        ASSESSMENT_RATING.ENTITY_ID.as("Waltz Id"),
                        entityExtIdField.as("External Id"),
                        entityNameField.as("Name"),
                        RATING_SCHEME_ITEM.CODE.as("Code"),
                        RATING_SCHEME_ITEM.NAME.as("Rating Name"),
                        ASSESSMENT_DEFINITION.NAME.as("Definition Name"))
                .from(ASSESSMENT_RATING)
                .innerJoin(RATING_SCHEME_ITEM)
                .on(ASSESSMENT_RATING.RATING_ID.eq(RATING_SCHEME_ITEM.ID))
                .innerJoin(ASSESSMENT_DEFINITION)
                .on(ASSESSMENT_DEFINITION.ID.eq(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID))
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(definitionId))
                .and(entityLifecycleField.ne(EntityLifecycleStatus.REMOVED.name()));
    }
}
