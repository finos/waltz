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

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.user.UserRoleService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.union;

public class BulkRoleAssign {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        UserRoleService userRoleService = ctx.getBean(UserRoleService.class);

        Set<String> defaultRoles = SetUtilities.asSet(
                SystemRole.BOOKMARK_EDITOR.name(),
                SystemRole.LOGICAL_DATA_FLOW_EDITOR.name(),
                SystemRole.LINEAGE_EDITOR.name());

        Set<String> mustHaveRoles = SetUtilities.asSet(
                SystemRole.TAXONOMY_EDITOR.name(),
                SystemRole.CAPABILITY_EDITOR.name(),
                SystemRole.RATING_EDITOR.name());

        InputStream inputStream = BulkRoleAssign.class.getClassLoader().getResourceAsStream("bulk-role-assign-example.txt");
        Set<Tuple2<String, Set<String>>> updates = IOUtilities
                .streamLines(inputStream)
                .map(d -> d.toLowerCase().trim())
                .map(d -> Tuple.tuple(d, userRoleService.getUserRoles(d)))
                .map(t -> t.map2(existingRoles -> union(existingRoles, defaultRoles, mustHaveRoles)))
                .collect(Collectors.toSet());

        System.out.printf("About to update: %d user-role mappings\n", updates.size());
        updates.forEach(t -> userRoleService.updateRoles("admin", t.v1, t.v2));
        System.out.println("Finished updating mappings");
    }
}
