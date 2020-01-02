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
