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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.application.search.SqlServerAppSearch;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.entity_search.ImmutableEntitySearchOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

public class MsSqlSearchHarness {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        SqlServerAppSearch appSearch = new SqlServerAppSearch();

        EntitySearchOptions searchOptions = ImmutableEntitySearchOptions.builder()
                .addEntityKinds(EntityKind.APPLICATION)
                .userId("admin")
                .limit(EntitySearchOptions.DEFAULT_SEARCH_RESULTS_LIMIT)
                .searchQuery("sap")
                .build();

        List<Application> results = appSearch.searchFullText(
                dsl,
                searchOptions);

        results.stream()
            .filter(a -> a.entityLifecycleStatus() != EntityLifecycleStatus.REMOVED)
            .forEach(a -> System.out.println(a.name() + " - " + a.lifecyclePhase()));
    }


}
