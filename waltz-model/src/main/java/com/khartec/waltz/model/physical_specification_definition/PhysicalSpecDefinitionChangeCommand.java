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

package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionChangeCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionChangeCommand.class)
public abstract class PhysicalSpecDefinitionChangeCommand implements Command, IdProvider {

    public abstract String version();
    public abstract ReleaseLifecycleStatus status();
    public abstract Optional<String> delimiter();
    public abstract PhysicalSpecDefinitionType type();
}
