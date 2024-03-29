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

package org.finos.waltz.web.action;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

import static java.lang.String.format;


@Value.Immutable
@JsonSerialize(as = ImmutableFieldChange.class)
@JsonDeserialize(as = ImmutableFieldChange.class)
@Deprecated
public abstract class FieldChange {

    private static String describeChange(FieldChange c) {
        return format(
                "Updated: %s, from: '%s' to: '%s'",
                c.name(),
                c.original().orElse("(empty)"),
                c.current().orElse("(empty)"));
    }

    public abstract String name();
    public abstract String key();
    public abstract boolean dirty();
    public abstract Optional<String> original();
    public abstract Optional<String> current();


    public String toDescription() {
        return describeChange(this);
    }
}
