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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstance.class)
@JsonDeserialize(as = ImmutableSurveyInstance.class)
public abstract class SurveyInstance implements IdProvider {

    public abstract Long surveyRunId();
    public abstract EntityReference surveyEntity();

    @Nullable
    public abstract String surveyEntityExternalId();
    public abstract SurveyInstanceStatus status();
    public abstract LocalDate dueDate();

    @Nullable
    public abstract LocalDateTime submittedAt();

    @Nullable
    public abstract String submittedBy();

    @Nullable
    public abstract LocalDateTime approvedAt();

    @Nullable
    public abstract String approvedBy();

    @Nullable
    public abstract Long originalInstanceId();
}
