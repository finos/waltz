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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.overlay_diagram.AppCostWidgetDao;
import org.finos.waltz.data.overlay_diagram.AppCountWidgetDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.overlay_diagram.CostWidgetDatum;
import org.finos.waltz.model.overlay_diagram.CountWidgetDatum;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Set;

import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class OverlayDiagramHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        AppCountWidgetDao countWidgetDao = ctx.getBean(AppCountWidgetDao.class);
        AppCostWidgetDao costWidgetDao = ctx.getBean(AppCostWidgetDao.class);

        IdSelectionOptions appGroup = mkOpts(mkRef(EntityKind.APP_GROUP, 11785L));
        IdSelectionOptions ou = mkOpts(mkRef(EntityKind.ORG_UNIT, 95L));

        ApplicationIdSelectorFactory appSelector = new ApplicationIdSelectorFactory();

        Select<Record1<Long>> appIds = appSelector.apply(ou);

        LocalDate targetStateDate = DateTimeUtilities.nowUtc().toLocalDate().plusYears(1);

        Set<CountWidgetDatum> appCountWidgetData = time("countWidgetData", () -> countWidgetDao.findWidgetData(
                1,
                appIds,
                targetStateDate));

        Set<CostWidgetDatum> appCostWidgetData = time("costWidgetData", () -> costWidgetDao.findWidgetData(
                1,
                appIds,
                targetStateDate));

        System.out.println(appCountWidgetData);
        System.out.println(appCostWidgetData);


    }

}
