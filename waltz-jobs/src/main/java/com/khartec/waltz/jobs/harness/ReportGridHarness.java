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

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.report_grid.ReportGridDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.report_grid.ReportGridRatingCell;
import com.khartec.waltz.service.DIBaseConfiguration;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Set;

import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class ReportGridHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);
        ReportGridDao dao = ctx.getBean(ReportGridDao.class);

        System.out.println("Starting....");

        EntityReference cib = mkRef(EntityKind.APP_GROUP, 11261);
        EntityReference justWaltz = mkRef(EntityKind.APPLICATION, 20506);
        EntityReference justWaltzGroup = mkRef(EntityKind.APP_GROUP, 433);
        EntityReference orgUnit = mkRef(EntityKind.ORG_UNIT, 95);
        EntityReference flowDiagram = mkRef(EntityKind.FLOW_DIAGRAM, 1);
        EntityReference mgr = mkRef(EntityKind.PERSON, 1);

        Select<Record1<Long>> selector = new ApplicationIdSelectorFactory().apply(mkOpts(mgr));

        System.out.println("Made selector");

        Set<ReportGridRatingCell> a = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(5, selector));
        Set<ReportGridRatingCell> b = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(5, selector));
        Set<ReportGridRatingCell> c = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(5, selector));
        Set<ReportGridRatingCell> data = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(5, selector));
        System.out.println(data.size());
        System.out.println(first(data));
    }




}
