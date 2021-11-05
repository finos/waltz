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

import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.cost.CostService;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class AssetCostHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        CostService svc = ctx.getBean(CostService.class);

        IdSelectionOptions bill = mkOpts(mkRef(EntityKind.PERSON, 1234));
        IdSelectionOptions jennifer = mkOpts(mkRef(EntityKind.PERSON, 5678));
        IdSelectionOptions scott = mkOpts(mkRef(EntityKind.PERSON, 9876));
        IdSelectionOptions infra = mkOpts(mkRef(EntityKind.ORG_UNIT, 1234));

        long tco = 6L;
        long cirrus = 7L;
//        time("tco cost summary for infra", () -> svc.summariseByCostKindAndSelector(tco, infra, EntityKind.APPLICATION, 20));
//        time("cirrus cost summary for infra", () -> svc.summariseByCostKindAndSelector(cirrus, infra, EntityKind.APPLICATION, 20));
////        time("cost summary for bill", () -> svc.summariseByCostKindAndSelector(tco, bill, EntityKind.APPLICATION, 20));
//        time("tco cost summary for scott", () -> svc.summariseByCostKindAndSelector(tco, scott, EntityKind.APPLICATION, 20));
        time("cirrus cost summary for jennifer", () -> svc.summariseByCostKindAndSelector(cirrus, jennifer, EntityKind.APPLICATION, 20));
        time("cirrus cost summary for bill", () -> svc.summariseByCostKindAndSelector(cirrus, bill, EntityKind.APPLICATION, 20));
        time("cirrus cost summary for scott", () -> svc.summariseByCostKindAndSelector(cirrus, scott, EntityKind.APPLICATION, 20));


    }

}
