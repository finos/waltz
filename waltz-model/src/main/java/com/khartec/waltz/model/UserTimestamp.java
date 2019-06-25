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

package com.khartec.waltz.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.common.DateTimeUtilities;
import org.immutables.value.Value;

import java.sql.Timestamp;
import java.time.LocalDateTime;


@Value.Immutable
@JsonSerialize(as = ImmutableUserTimestamp.class)
@JsonDeserialize(as = ImmutableUserTimestamp.class)
public abstract class UserTimestamp {

    @Value.Default
    public LocalDateTime at() {
        return DateTimeUtilities.nowUtc();
    }


    public abstract String by();


    @Value.Derived
    @JsonIgnore
    public Timestamp atTimestamp() {
        return Timestamp.valueOf(at());
    }


    public static UserTimestamp mkForUser(String username) {
        return ImmutableUserTimestamp.builder()
                .by(username)
                .build();
    }


    public static UserTimestamp mkForUser(String username, LocalDateTime at) {
        return ImmutableUserTimestamp.builder()
                .by(username)
                .at(at)
                .build();
    }


    public static UserTimestamp mkForUser(String username, Timestamp at) {
        return ImmutableUserTimestamp.builder()
                .by(username)
                .at(at.toLocalDateTime())
                .build();
    }
}
