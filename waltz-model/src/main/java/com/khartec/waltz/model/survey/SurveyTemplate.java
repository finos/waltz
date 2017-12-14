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
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyTemplate.class)
@JsonDeserialize(as = ImmutableSurveyTemplate.class)
public abstract class SurveyTemplate implements IdProvider, NameProvider, DescriptionProvider {

    public abstract EntityKind targetEntityKind();
    public abstract Long ownerId();


    @Value.Default
    public LocalDateTime createdAt() {
        return LocalDateTime.now();
    }


    @Value.Default
    public ReleaseLifecycleStatus status() {
        return ReleaseLifecycleStatus.ACTIVE;
    }
}
