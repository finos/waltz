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
package org.finos.waltz.web.json;



import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


import java.util.HashMap;
import java.util.Map;


@Value.Immutable
@JsonSerialize(as = ImmutableApiTypes.class)
@JsonDeserialize(as = ImmutableApiTypes.class)
public class ApiTypes {

    public final static String KEYCELL = "5";
    public final static String VALCELL = "10";
    private final Map<String, String> apiTypes;


    public ApiTypes() {
        Map<String, String> types = new HashMap<>();
        types.put(KEYCELL, "http://waltz.intranet.db.com/types/1/schema#id=KeyCell");
        types.put(VALCELL, "http://waltz.intranet.db.com/types/1/schema#id=CellValue");
        this.apiTypes = types;
    }

    public Map<String, String> getApiTypes() {
        return apiTypes;
    }
}
