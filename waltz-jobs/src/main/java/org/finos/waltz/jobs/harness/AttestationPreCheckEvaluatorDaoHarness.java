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

import org.finos.waltz.data.attestation.AttestationPreCheckDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.attestation.AttestationPreCheckService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.model.EntityReference.mkRef;


public class AttestationPreCheckEvaluatorDaoHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AttestationPreCheckDao dao = ctx.getBean(AttestationPreCheckDao.class);
        AttestationPreCheckService svc = ctx.getBean(AttestationPreCheckService.class);

        for (int i = 0; i < 10; i++) {
            time("dao ["+i+"]", () -> System.out.println(dao.calcLogicalFlowAttestationPreChecks(
                mkRef(
                    EntityKind.APPLICATION,
                    17168L))));
            time("svc ["+i+"]", () -> System.out.println(svc.calcLogicalFlowPreCheckFailures(
                mkRef(
                    EntityKind.APPLICATION,
                    17168L))));
        }
    }




}
