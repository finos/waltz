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

package org.finos.waltz.data.process_diagram;

import org.finos.waltz.data.IdSelectorFactory;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.measurable.MeasurableIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;

import static org.finos.waltz.schema.Tables.MEASURABLE_RATING;
import static org.finos.waltz.schema.Tables.PROCESS_DIAGRAM_ENTITY;
import static org.finos.waltz.common.Checks.checkNotNull;


public class ProcessDiagramIdSelectorFactory implements IdSelectorFactory {

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();

    @Override
    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        switch(options.entityReference().kind()) {
            case ACTOR:
            case END_USER_APPLICATION:
                return mkForSpecificEntity(options);
            case ALL:
            case APPLICATION:
            case CHANGE_INITIATIVE:
            case DATA_TYPE:
            case LOGICAL_DATA_FLOW:
            case PHYSICAL_FLOW:
            case PHYSICAL_SPECIFICATION:
                return mkViaApplication(options);
            case MEASURABLE:
                return mkForMeasurable(options);
            default:
                throw new UnsupportedOperationException("Cannot create process diagram selector from options: "+options);
        }
    }


    private Select<Record1<Long>> mkForSpecificEntity(IdSelectionOptions options) {

        return DSL
                .select(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID)
                .from(PROCESS_DIAGRAM_ENTITY)
                .innerJoin(MEASURABLE_RATING).on(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.eq(MEASURABLE_RATING.MEASURABLE_ID)
                        .and(PROCESS_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .where(MEASURABLE_RATING.ENTITY_ID.eq(options.entityReference().id())
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(options.entityReference().kind().name())));
    }


    private Select<Record1<Long>> mkViaApplication(IdSelectionOptions options) {

        Select<Record1<Long>> relatedApplications = applicationIdSelectorFactory.apply(options);

        return DSL
                .select(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID)
                .from(PROCESS_DIAGRAM_ENTITY)
                .innerJoin(MEASURABLE_RATING).on(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.eq(MEASURABLE_RATING.MEASURABLE_ID)
                        .and(PROCESS_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())))
                .where(MEASURABLE_RATING.ENTITY_ID.in(relatedApplications)
                        .and(MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
    }


    private Select<Record1<Long>> mkForMeasurable(IdSelectionOptions options) {
        Select<Record1<Long>> measurableIds = measurableIdSelectorFactory.apply(options);
        return DSL
                .select(PROCESS_DIAGRAM_ENTITY.DIAGRAM_ID)
                .from(PROCESS_DIAGRAM_ENTITY)
                .where(PROCESS_DIAGRAM_ENTITY.ENTITY_ID.in(measurableIds)
                .and(PROCESS_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.MEASURABLE.name())));
    }

}
