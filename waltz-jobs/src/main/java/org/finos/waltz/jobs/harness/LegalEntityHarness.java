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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.legal_entity.LegalEntityRelKindStat;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipKindService;
import org.finos.waltz.service.legal_entity.LegalEntityRelationshipService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class LegalEntityHarness {

    public static void main(String[] args) throws InterruptedException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LegalEntityRelationshipService service = ctx.getBean(LegalEntityRelationshipService.class);
        LegalEntityRelationshipKindService service2 = ctx.getBean(LegalEntityRelationshipKindService.class);

        IdSelectionOptions relOpts = mkOpts(mkRef(EntityKind.LEGAL_ENTITY_RELATIONSHIP_KIND, 2L));


        LegalEntityRelKindStat viewByRelKindAndSelector = service2.getUsageStatsByKindAndSelector(2L, relOpts);


        System.out.println(viewByRelKindAndSelector.relationshipCount());

    }

}
