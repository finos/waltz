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

package org.finos.waltz.model.taxonomy_management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.StringUtilities.isEmpty;

@Value.Immutable
@JsonSerialize(as =  ImmutableTaxonomyChangeCommand.class)
@JsonDeserialize(as =  ImmutableTaxonomyChangeCommand.class)
public abstract class TaxonomyChangeCommand implements
        Command,
        IdProvider,
        CreatedProvider,
        LastUpdatedProvider {

    public abstract TaxonomyChangeType changeType();

    @Value.Default
    public TaxonomyChangeLifecycleStatus status() {
        return TaxonomyChangeLifecycleStatus.DRAFT;
    }

    public abstract EntityReference changeDomain();

    public abstract EntityReference primaryReference();

    @Nullable
    public abstract Map<String, String> params();


    /**
     * Ensures that the `domain` matches `a` and (potentially) `b`
     */
    public void validate() {
        checkDomain(changeDomain().kind());
        checkRefKind(primaryReference().kind(), "a");
    }


    private void checkDomain(EntityKind domainKind) {
        checkTrue(
                domainKind == EntityKind.MEASURABLE_CATEGORY || domainKind == EntityKind.DATA_TYPE,
                "Taxonomy change command must be either a [MEASURABLE_CATEGORY] or a [DATA_TYPE], instead is is a [%s]",
                domainKind);
    }


    private void checkRefKind(EntityKind kind, String label) {
        switch (changeDomain().kind()) {
            case MEASURABLE_CATEGORY:
                checkTrue(
                        kind == EntityKind.MEASURABLE,
                        "If domain is [MEASURABLE_CATEGORY] then '%s' must also be a [MEASURABLE], instead it is a [%s]",
                        label,
                        kind);
                break;
            case DATA_TYPE:
                checkTrue(
                        primaryReference().kind() == EntityKind.DATA_TYPE,
                        "If domain is [DATA_TYPE] then 'a' must also be a [DATA_TYPE], instead it is a [%s]",
                        primaryReference().kind());
                break;
        }
    }


    public boolean paramAsBoolean(String key, boolean dflt) {
        return Boolean.valueOf(params().getOrDefault(key, Boolean.toString(dflt)));
    }


    public Long paramAsLong(String key, Long dflt) {
        String strVal = params().get(key);
        return isEmpty(strVal) ? dflt : Long.valueOf(strVal);
    }


    public List<Long> paramAsLongList(String key) throws JsonProcessingException {
        String strVal = params().get(key);
        List<?> list = getJsonMapper().readValue(strVal, List.class);

        return list
                .stream()
                .map(l -> Long.parseLong(l.toString()))
                .collect(Collectors.toList());
    }


    public String param(String key) {
        return params().get(key);
    }
}

