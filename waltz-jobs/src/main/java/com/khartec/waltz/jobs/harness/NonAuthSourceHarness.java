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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.authoritativesource.NonAuthoritativeSource;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.function.Consumer;

import static com.khartec.waltz.model.EntityReference.mkRef;


public class NonAuthSourceHarness {



    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        AuthoritativeSourceService authoritativeSourceService = ctx.getBean(AuthoritativeSourceService.class);

        Consumer<NonAuthoritativeSource> dumpRow = r -> {
            System.out.println(String.format("%s | %d - %d",
                    r.sourceReference().name().orElse("?"),
                    r.dataTypeId(),
                    r.count()));
        };

//        authoritativeSourceService.findNonAuthSources(mkRef(EntityKind.ORG_UNIT, 200L)).forEach(dumpRow);
//        authoritativeSourceService.findNonAuthSources(mkRef(EntityKind.DATA_TYPE, 6000L)).forEach(dumpRow);

        IdSelectionOptions options = IdSelectionOptions.mkOpts(mkRef(EntityKind.APP_GROUP, 41), HierarchyQueryScope.EXACT);

        authoritativeSourceService.findNonAuthSources(options).forEach(dumpRow);
    }


}
