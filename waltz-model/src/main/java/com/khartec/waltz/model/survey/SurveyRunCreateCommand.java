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
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyRunCreateCommand.class)
@JsonDeserialize(as = ImmutableSurveyRunCreateCommand.class)
public abstract class SurveyRunCreateCommand implements Command, NameProvider, DescriptionProvider {

    public abstract Long surveyTemplateId();
    public abstract IdSelectionOptions selectionOptions();
    public abstract Set<Long> involvementKindIds();
    public abstract Optional<LocalDate> dueDate();
    public abstract SurveyIssuanceKind issuanceKind();
    public abstract Optional<String> contactEmail();

}
