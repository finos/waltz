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

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.report_grid.ReportGridDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.finos.waltz.service.DIBaseConfiguration;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.report_grid.ReportGridFilterViewService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.text.html.parser.Entity;
import java.util.Set;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class ReportGridHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ReportGridFilterViewService svc = ctx.getBean(ReportGridFilterViewService.class);

        svc.generateAppGroupsFromFilter();

//        System.out.println("Starting....");
//
//        EntityReference cib = mkRef(EntityKind.APP_GROUP, 11261);
//        EntityReference infra = mkRef(EntityKind.ORG_UNIT, 6811);
//        EntityReference justWaltz = mkRef(EntityKind.APPLICATION, 20506);
//        EntityReference justWaltzGroup = mkRef(EntityKind.APP_GROUP, 433);
//        EntityReference orgUnit = mkRef(EntityKind.ORG_UNIT, 95);
//        EntityReference everythingGroup = mkRef(EntityKind.APP_GROUP, 20827);
//        EntityReference flowDiagram = mkRef(EntityKind.FLOW_DIAGRAM, 1);
//        EntityReference mgr = mkRef(EntityKind.PERSON, 1);
//
//        GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
//        GenericSelector selector = genericSelectorFactory.applyForKind(EntityKind.APPLICATION, mkOpts(orgUnit));
//
//        System.out.println("Made selector");
//
//        Set<ReportGridCell> a = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(3, selector));
//        Set<ReportGridCell> b = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(3, selector));
//        Set<ReportGridCell> c = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(3, selector));
//        Set<ReportGridCell> data = FunctionUtilities.time("getCellData", () -> dao.findCellDataByGridId(3, selector));
//        System.out.println(data.size());
//        System.out.println(first(data));
    }




}
