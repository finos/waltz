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

package com.khartec.waltz.service.entity_search;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.data.SearchUtilities;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.service.actor.ActorService;
import com.khartec.waltz.service.app_group.AppGroupService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.change_initiative.ChangeInitiativeService;
import com.khartec.waltz.service.data_type.DataTypeService;
import com.khartec.waltz.service.logical_data_element.LogicalDataElementService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import com.khartec.waltz.service.person.PersonService;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import com.khartec.waltz.service.roadmap.RoadmapService;
import com.khartec.waltz.service.server_information.ServerInformationService;
import com.khartec.waltz.service.software_catalog.SoftwareCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Unchecked.supplier;

@Service
public class EntitySearchService {

    private final DBExecutorPoolInterface dbExecutorPool;
    private final ActorService actorService;
    private final ApplicationService applicationService;
    private final AppGroupService appGroupService;
    private final ChangeInitiativeService changeInitiativeService;
    private final LogicalDataElementService logicalDataElementService;
    private final DataTypeService dataTypeService;
    private final MeasurableService measurableService;
    private final OrganisationalUnitService organisationalUnitService;
    private final PersonService personService;
    private final PhysicalSpecificationService physicalSpecificationService;
    private final RoadmapService roadmapService;
    private final ServerInformationService serverInformationService;
    private final SoftwareCatalogService softwareCatalogService;


    @Autowired
    public EntitySearchService(DBExecutorPoolInterface dbExecutorPool,
                               ActorService actorService,
                               ApplicationService applicationService,
                               AppGroupService appGroupService,
                               ChangeInitiativeService changeInitiativeService,
                               LogicalDataElementService logicalDataElementService,
                               DataTypeService dataTypeService,
                               MeasurableService measurableService,
                               OrganisationalUnitService organisationalUnitService,
                               PersonService personService,
                               PhysicalSpecificationService physicalSpecificationService,
                               RoadmapService roadmapService,
                               ServerInformationService serverInformationService,
                               SoftwareCatalogService softwareCatalogService) {
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(actorService, "actorService cannot be null");
        checkNotNull(applicationService, "applicationService cannot be null");
        checkNotNull(appGroupService, "appGroupService cannot be null");
        checkNotNull(changeInitiativeService, "changeInitiativeService cannot be null");
        checkNotNull(logicalDataElementService, "logicalDataElementService cannot be null");
        checkNotNull(dataTypeService, "dataTypeService cannot be null");
        checkNotNull(measurableService, "measurableService cannot be null");
        checkNotNull(organisationalUnitService, "organisationalUnitService cannot be null");
        checkNotNull(personService, "personService cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");
        checkNotNull(roadmapService, "roadmapService cannot be null");
        checkNotNull(serverInformationService, "serverInformationService cannot be null");
        checkNotNull(softwareCatalogService, "softwareCatalogService cannot be null");

        this.actorService = actorService;
        this.dbExecutorPool = dbExecutorPool;
        this.applicationService = applicationService;
        this.appGroupService = appGroupService;
        this.changeInitiativeService = changeInitiativeService;
        this.dataTypeService = dataTypeService;
        this.logicalDataElementService = logicalDataElementService;
        this.measurableService = measurableService;
        this.organisationalUnitService = organisationalUnitService;
        this.personService = personService;
        this.physicalSpecificationService = physicalSpecificationService;
        this.roadmapService = roadmapService;
        this.serverInformationService = serverInformationService;
        this.softwareCatalogService = softwareCatalogService;
    }


    public List<EntityReference> search(EntitySearchOptions options) {
        checkNotNull(options, "options cannot be null");

        if (StringUtilities.isEmpty(options.searchQuery())
                || SearchUtilities.mkTerms(options.searchQuery()).isEmpty()) {
            return Collections.emptyList();
        }

        List<Future<Collection<? extends WaltzEntity>>> futures = options
                .entityKinds()
                .stream()
                .map(ek -> dbExecutorPool.submit(mkCallable(ek, options)))
                .collect(toList());

        return futures
                .stream()
                .flatMap(f -> supplier(f::get).get().stream())
                .map(WaltzEntity::entityReference)
                .collect(toList());
    }


    private Callable<Collection<? extends WaltzEntity>> mkCallable(EntityKind entityKind,
                                                                   EntitySearchOptions options) {
        switch (entityKind) {
            case ACTOR:
                return () -> actorService.search(options);
            case APPLICATION:
                return () -> applicationService.search(options);
            case APP_GROUP:
                return () -> appGroupService.search(options);
            case CHANGE_INITIATIVE:
                return () -> changeInitiativeService.search(options);
            case DATA_TYPE:
                return () -> dataTypeService.search(options);
            case LOGICAL_DATA_ELEMENT:
                return () -> logicalDataElementService.search(options);
            case MEASURABLE:
                return () -> measurableService.search(options);
            case ORG_UNIT:
                return () -> organisationalUnitService.search(options);
            case PERSON:
                return () -> personService.search(options);
            case PHYSICAL_SPECIFICATION:
                return () -> physicalSpecificationService.search(options);
            case ROADMAP:
                return () -> roadmapService.search(options);
            case SERVER:
                return () -> serverInformationService.search(options);
            case SOFTWARE:
                return () -> softwareCatalogService.search(options);
            default:
                throw new UnsupportedOperationException("no search service available for: " + entityKind);
        }
    }
}
