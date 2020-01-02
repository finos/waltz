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

package com.khartec.waltz.model.tally;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Simple class used for representing a simple count of a given id.
 *
 * Typically used for returning the results of a query similar to:
 * <pre>SELECT id, COUNT(something) FROM somewhere GROUP BY something</pre>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTally.class)
@JsonDeserialize(as = ImmutableTally.class)
public abstract class Tally<T> {

    public abstract T id();
    public abstract double count();

}
