/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Set;


@Value.Immutable
@JsonSerialize(as = ImmutableApplicationIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableApplicationIdSelectionOptions.class)
public abstract class ApplicationIdSelectionOptions extends IdSelectionOptions {

    public static ApplicationIdSelectionOptions mkOpts(EntityReference ref) {
        return mkOpts(
                ref,
                determineDefaultScope(ref.kind()));
    }

    public static ApplicationIdSelectionOptions mkOpts(EntityReference ref,
                                                       HierarchyQueryScope scope) {
        return ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();
    }


    public static ApplicationIdSelectionOptions mkOpts(EntityReference ref,
                                                       HierarchyQueryScope scope,
                                                       Set<ApplicationKind> applicationKinds) {
        return ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .applicationKinds(applicationKinds)
                .build();
    }


    public static ApplicationIdSelectionOptions mkOpts(IdSelectionOptions options,
                                                       Set<ApplicationKind> applicationKinds) {
        return mkOpts(options.entityReference(), options.scope(), applicationKinds);
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
            appOptions = mkOpts(options);
        }
        return appOptions;
    }


    @Value.Default
    public Set<ApplicationKind> applicationKinds() {
        return SetUtilities.fromArray(ApplicationKind.values());
    }
}
