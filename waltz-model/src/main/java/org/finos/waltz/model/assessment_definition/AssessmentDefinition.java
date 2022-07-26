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

package org.finos.waltz.model.assessment_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentDefinition.class)
@JsonDeserialize(as = ImmutableAssessmentDefinition.class)
public abstract class AssessmentDefinition implements
        IdProvider,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract EntityKind entityKind();

    public abstract long ratingSchemeId();

    public abstract Optional<String> permittedRole();

    public abstract boolean isReadOnly();

    public abstract AssessmentVisibility visibility();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.ASSESSMENT_DEFINITION;
    }

    public abstract Optional<EntityReference> qualifierReference();
}
