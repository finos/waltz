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
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyRunCompletionRate.class)
@JsonDeserialize(as = ImmutableSurveyRunCompletionRate.class)
public abstract class SurveyRunCompletionRate {
    public abstract long surveyRunId();

    @Value.Default
    public int notStartedCount() {
        return 0;
    };


    @Value.Default
    public int inProgressCount() {
        return 0;
    };


    @Value.Default
    public int completedCount() {
        return 0;
    };


    @Value.Derived
    public int totalCount() {
        return notStartedCount() + inProgressCount() + completedCount();
    };

    public static SurveyRunCompletionRate mkNoData(long runId) {
        return ImmutableSurveyRunCompletionRate
                .builder()
                .surveyRunId(runId)
                .build();
    }
}
