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

import org.finos.waltz.data.aggregate_overlay_diagram.AppCostWidgetDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.CostWidgetData;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.ImmutableAppCostWidgetParameters;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.aggregate_overlay_diagram.AggregateOverlayDiagramService;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class AggregateWidgetCostHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AppCostWidgetDao dao = ctx.getBean(AppCostWidgetDao.class);
        AggregateOverlayDiagramService svc = ctx.getBean(AggregateOverlayDiagramService.class);

        IdSelectionOptions selectionOptions = mkOpts(mkRef(EntityKind.APP_GROUP, 17618));
        long diagramId = 1L;
        long TCO = 6L;
        long allocationSchemeId = 1L;

        ApplicationIdSelectorFactory selectorFactory = new ApplicationIdSelectorFactory();
        Select<Record1<Long>> appIds = selectorFactory.apply(selectionOptions);

        CostWidgetData result = svc.getAppCostWidgetData(diagramId,
                                                                    Collections.emptySet(),
                                                                    selectionOptions,
                                                                    ImmutableAppCostWidgetParameters
                                                                            .builder()
                                                                            .addCostKindIds(TCO)
                                                                            .allocationSchemeId(allocationSchemeId)
                                                                            .build());

        System.out.println(result);

    }

}
