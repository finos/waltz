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

package com.khartec.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Set;


@Value.Immutable
@JsonSerialize(as = ImmutableApplicationIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableApplicationIdSelectionOptions.class)
@Deprecated
public abstract class ApplicationIdSelectionOptions extends IdSelectionOptions {

    public static ApplicationIdSelectionOptions mkOpts(EntityReference ref) {
        return mkOpts(
                ref,
                determineDefaultScope(ref.kind()));
    }


    public static ApplicationIdSelectionOptions mkOpts(EntityReference ref,
                                                       HierarchyQueryScope scope) {
        return mkOpts(ref, scope, null, null);
    }


    public static ApplicationIdSelectionOptions mkOpts(EntityReference ref,
                                                       HierarchyQueryScope scope,
                                                       Set<EntityLifecycleStatus> entityLifecycleStatuses,
                                                       Set<ApplicationKind> applicationKinds) {
        ImmutableApplicationIdSelectionOptions.Builder builder = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope);

        if(entityLifecycleStatuses != null) {
            builder.entityLifecycleStatuses(entityLifecycleStatuses);
        }

        if(applicationKinds != null) {
            builder.applicationKinds(applicationKinds);
        }
        return builder.build();
    }


    public static ApplicationIdSelectionOptions mkOpts(IdSelectionOptions options,
                                                       Set<ApplicationKind> applicationKinds) {
        return mkOpts(options.entityReference(), options.scope(), options.entityLifecycleStatuses(), applicationKinds);
    }


    /**
     * Attempts to cast IdSelectionOptions into an ApplicationIdSelectionOptions.
     * If the input options has a runtime type of ApplicationIdSelectionOptions a cast is made, else a new set of
     * options is built with defaults.
     *
     * This is to support instances where options can be of either type at runtime.
     * @param options
     * @return
     */
    public static ApplicationIdSelectionOptions mkOpts(IdSelectionOptions options) {
        ApplicationIdSelectionOptions appOptions = null;
        if(options instanceof ApplicationIdSelectionOptions) {
            appOptions = (ApplicationIdSelectionOptions) options;
        } else {
            appOptions = mkOpts(options, SetUtilities.fromArray(ApplicationKind.values()));
        }
        return appOptions;
    }


    @Value.Default
    public Set<ApplicationKind> applicationKinds() {
        return SetUtilities.fromArray(ApplicationKind.values());
    }
}
