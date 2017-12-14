/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyQuestion.class)
@JsonDeserialize(as = ImmutableSurveyQuestion.class)
public abstract class SurveyQuestion implements IdProvider {

    public abstract Long surveyTemplateId();
    public abstract String questionText();
    public abstract Optional<String> helpText();
    public abstract SurveyQuestionFieldType fieldType();
    public abstract Optional<String> sectionName();

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
}
