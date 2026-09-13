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

package org.finos.waltz.service.survey;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.ImmutableSurveyQuestion;
import org.finos.waltz.model.survey.ImmutableSurveyQuestionResponse;
import org.finos.waltz.model.survey.SurveyQuestion;
import org.finos.waltz.model.survey.SurveyQuestionFieldType;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.survey.SurveyInstanceUtilities.getVal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SurveyInstanceUtilitiesTest {

    private static final EntityReference APP = mkRef(EntityKind.APPLICATION, 1L);
    private static final List<EntityReference> ENTITIES = asList(APP, mkRef(EntityKind.PERSON, 2L));
    private static final List<String> STRINGS = asList("a", "b");
    private static final LocalDate DATE = LocalDate.of(2024, 1, 2);

    private static final SurveyQuestionResponse FULL_RESPONSE = ImmutableSurveyQuestionResponse.builder()
            .questionId(1L)
            .stringResponse("str")
            .numberResponse(4.2)
            .booleanResponse(true)
            .dateResponse(DATE)
            .entityResponse(APP)
            .listResponse(STRINGS)
            .entityListResponse(ENTITIES)
            .jsonResponse("{}")
            .build();


    private static SurveyQuestion mkQuestion(SurveyQuestionFieldType fieldType) {
        return ImmutableSurveyQuestion.builder()
                .surveyTemplateId(1L)
                .questionText("q")
                .fieldType(fieldType)
                .build();
    }


    private static Optional<?> val(SurveyQuestionFieldType fieldType) {
        return getVal(mkQuestion(fieldType), FULL_RESPONSE);
    }


    @Test
    public void nullResponseGivesEmpty() {
        assertFalse(getVal(mkQuestion(SurveyQuestionFieldType.TEXT), null).isPresent());
    }


    @Test
    public void stringFieldTypesUseStringResponse() {
        assertEquals(Optional.of("str"), val(SurveyQuestionFieldType.TEXT));
        assertEquals(Optional.of("str"), val(SurveyQuestionFieldType.TEXTAREA));
        assertEquals(Optional.of("str"), val(SurveyQuestionFieldType.DROPDOWN));
    }


    @Test
    public void scalarFieldTypesUseMatchingResponse() {
        assertEquals(Optional.of(4.2), val(SurveyQuestionFieldType.NUMBER));
        assertEquals(Optional.of(DATE), val(SurveyQuestionFieldType.DATE));
        assertEquals(Optional.of(true), val(SurveyQuestionFieldType.BOOLEAN));
        assertEquals(Optional.of("{}"), val(SurveyQuestionFieldType.ARC));
    }


    @Test
    public void listFieldTypesUseListResponse() {
        assertEquals(Optional.of(STRINGS), val(SurveyQuestionFieldType.DROPDOWN_MULTI_SELECT));
        assertEquals(Optional.of(STRINGS), val(SurveyQuestionFieldType.STRING_LIST));
    }


    @Test
    public void entityFieldTypesUseEntityResponses() {
        assertEquals(Optional.of(APP), val(SurveyQuestionFieldType.APPLICATION));
        assertEquals(Optional.of(APP), val(SurveyQuestionFieldType.PERSON));
        assertEquals(Optional.of(ENTITIES), val(SurveyQuestionFieldType.MEASURABLE_MULTI_SELECT));
        assertEquals(Optional.of(ENTITIES), val(SurveyQuestionFieldType.LEGAL_ENTITY));
    }


    @Test
    public void absentResponseValueGivesEmpty() {
        SurveyQuestionResponse emptyResponse = ImmutableSurveyQuestionResponse.builder()
                .questionId(1L)
                .build();

        assertFalse(getVal(mkQuestion(SurveyQuestionFieldType.TEXT), emptyResponse).isPresent());
        assertFalse(getVal(mkQuestion(SurveyQuestionFieldType.NUMBER), emptyResponse).isPresent());
    }

}
