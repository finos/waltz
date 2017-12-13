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

package com.khartec.waltz.model.server_information;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.util.Date;

import static com.khartec.waltz.model.EndOfLifeStatus.calculateEndOfLifeStatus;


@Value.Immutable
@JsonSerialize(as = ImmutableServerInformation.class)
@JsonDeserialize(as = ImmutableServerInformation.class)
public abstract class ServerInformation implements
        IdProvider,
        ProvenanceProvider,
        AssetCodeProvider {

    public abstract String hostname();
    public abstract String operatingSystem();
    public abstract String operatingSystemVersion();
    public abstract String environment();
    public abstract String location();
    public abstract String country();
    public abstract LifecycleStatus lifecycleStatus();

    @Nullable
    public abstract Date hardwareEndOfLifeDate();


    @Value.Derived
    public EndOfLifeStatus hardwareEndOfLifeStatus() {
        return calculateEndOfLifeStatus(hardwareEndOfLifeDate());
    }


    @Nullable
    public abstract Date operatingSystemEndOfLifeDate();


    @Value.Derived
    public EndOfLifeStatus operatingSystemEndOfLifeStatus() {
        return calculateEndOfLifeStatus(operatingSystemEndOfLifeDate());
    }


    @Value.Default
    public String provenance() {
        return "waltz";
    }


    @Value.Default
    public boolean virtual() {
        return false;
    }

}
