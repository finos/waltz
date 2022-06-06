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

package org.finos.waltz.model;

import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.mkPath;

public class EntityLinkUtilities {

    public static String mkIdLink(String baseUrl, EntityKind kind, Long id) {
        checkNotNull(baseUrl, "baseUrl cannot be null");
        checkNotNull(kind, "kind cannot be null");
        checkNotNull(id, "id cannot be null");

        return mkPath(baseUrl, "entity", kind.name(), "id", Long.toString(id));
    }


    public static String mkExternalIdLink(String baseUrl, EntityKind kind, String externalId) {
        checkNotNull(baseUrl, "baseUrl cannot be null");
        checkNotNull(kind, "kind cannot be null");
        checkNotEmpty(externalId, "externalId cannot be null");

        return mkPath(baseUrl, "entity", kind.name(), "external-id", externalId);
    }

}
