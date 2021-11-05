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

import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.attestation.AttestationInstanceDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.attestation.*;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.attestation.AttestationInstanceService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class AttestationInstanceHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AttestationInstanceDao attestationInstanceDao = ctx.getBean(AttestationInstanceDao.class);
        AttestationInstanceService svc = ctx.getBean(AttestationInstanceService.class);

        IdSelectionOptions opts = mkOpts(mkRef(EntityKind.APPLICATION, 20506L));
        IdSelectionOptions group = mkOpts(mkRef(EntityKind.APP_GROUP, 433L));
        IdSelectionOptions org = mkOpts(mkRef(EntityKind.ORG_UNIT, 6811));
        ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
        Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(opts);

        long st = System.currentTimeMillis();
        System.out.println("-- start");

        ApplicationAttestationSummaryFilters filters = ImmutableApplicationAttestationSummaryFilters.builder()
                .build();

        ImmutableApplicationAttestationInstanceInfo info = ImmutableApplicationAttestationInstanceInfo.builder()
                .filters(filters)
                .selectionOptions(org)
                .build();

        Set<ApplicationAttestationInstanceSummary> sumaries = time("instacnes", () -> svc.findApplicationAttestationInstancesForKindAndSelector(
                EntityKind.LOGICAL_DATA_FLOW,
                null,
                info));

        System.out.println(sumaries);

        Set<ApplicationAttestationSummaryCounts> summary = time("summary", () -> svc
                .findAttestationInstanceSummaryForSelector(info));

        System.out.println(summary);

    }




}
