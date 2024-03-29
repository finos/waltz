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

import org.finos.waltz.common.OptionalUtilities;
import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.measurable.Measurable;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.measurable.MeasurableService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


public class MeasurableHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        MeasurableIdSelectorFactory factory = new MeasurableIdSelectorFactory();
        MeasurableService measurableService = ctx.getBean(MeasurableService.class);

        EntityReference ref = mkRef(
                EntityKind.PERSON,
                172272
        );

        IdSelectionOptions options = mkOpts(
                ref,
                HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> selector = factory.apply(options);

        System.out.println("--selector");
        System.out.println(selector);
        System.out.println("---");

        List<Measurable> measurables = measurableService.findByMeasurableIdSelector(options);

        measurables.forEach(System.out::println);

        System.out.println("-----");

        measurables
                .stream()
                .filter(m -> OptionalUtilities.contentsEqual(m.id(), 486L))
                .forEach(System.out::println);

        System.out.println(measurables.size());
    }

}
