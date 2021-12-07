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

package org.finos.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ExternalIdProvider;
import org.finos.waltz.model.IdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyQuestion.class)
@JsonDeserialize(as = ImmutableSurveyQuestion.class)
public abstract class SurveyQuestion implements
        IdProvider,
        ExternalIdProvider {

    public abstract Long surveyTemplateId();
    public abstract String questionText();
    public abstract Optional<String> helpText();
    public abstract SurveyQuestionFieldType fieldType();
    public abstract Optional<String> sectionName();
    public abstract Optional<String> inclusionPredicate();

    public abstract Optional<EntityReference> qualifierEntity();

    @Value.Default
    public Integer position() {
        return 1;
    }

    @Value.Default
    public boolean isMandatory() {
        return false;
    }

    @Value.Default
    public Boolean allowComment() {
        return false;
    }

    public abstract Optional<String> label();

    public abstract Optional<String> parentExternalId();

    @Value.Default
    public EntityKind kind(){
        return EntityKind.SURVEY_QUESTION;
    }

}
