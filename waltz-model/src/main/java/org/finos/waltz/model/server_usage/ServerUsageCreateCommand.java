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

package org.finos.waltz.model.server_usage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;


/**
 * Describes a request to link an existing server asset to an entity (e.g. an application).
 * The target entity is supplied out of band (typically via the request path); only the
 * server and the environment it is used in are carried here.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerUsageCreateCommand.class)
@JsonDeserialize(as = ImmutableServerUsageCreateCommand.class)
public abstract class ServerUsageCreateCommand implements Command {

    public abstract long serverId();

    @Value.Default
    public String environment() {
        return "PROD";
    }
}
