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

package org.finos.waltz.model.attestation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationRunCreateCommand.class)
@JsonDeserialize(as = ImmutableAttestationRunCreateCommand.class)
public abstract class AttestationRunCreateCommand implements
        Command,
        NameProvider,
        DescriptionProvider {

    public abstract EntityKind targetEntityKind();
    public abstract IdSelectionOptions selectionOptions();
    public abstract Set<Long> involvementKindIds();

    public abstract EntityKind attestedEntityKind();
    public abstract Optional<Long> attestedEntityId();

    @Value.Default
    public LocalDate issuedOn() {
        return LocalDate.now();
    }
    public abstract LocalDate dueDate();
}
