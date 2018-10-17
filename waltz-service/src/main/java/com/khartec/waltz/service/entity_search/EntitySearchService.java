/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.entity_search;

import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.WaltzEntity;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
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
                               RoadmapService roadmapService) {
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
    }


    public List<EntityReference> search(String terms, EntitySearchOptions options) {
        checkNotNull(terms, "terms cannot be null");
        checkNotNull(options, "options cannot be null");

        List<Future<Collection<? extends WaltzEntity>>> futures = options.entityKinds().stream()
                .map(ek -> dbExecutorPool.submit(mkCallable(ek, terms, options)))
                .collect(toList());

        return futures.stream()
                .flatMap(f -> supplier(f::get).get().stream())
                .map(WaltzEntity::entityReference)
                .collect(toList());
    }


    private Callable<Collection<? extends WaltzEntity>> mkCallable(EntityKind entityKind,
                                                                   String terms,
                                                                   EntitySearchOptions options) {
        switch (entityKind) {
            case ACTOR:
                return () -> actorService.search(terms, options);
            case APPLICATION:
                return () -> applicationService.search(terms, options);
            case APP_GROUP:
                return () -> appGroupService.search(terms, options);
            case CHANGE_INITIATIVE:
                return () -> changeInitiativeService.search(terms, options);
            case DATA_TYPE:
                return () -> dataTypeService.search(terms);
            case LOGICAL_DATA_ELEMENT:
                return () -> logicalDataElementService.search(terms, options);
            case MEASURABLE:
                return () -> measurableService.search(terms, options);
            case ORG_UNIT:
                return () -> organisationalUnitService.search(terms, options);
            case PERSON:
                return () -> personService.search(terms, options);
            case PHYSICAL_SPECIFICATION:
                return () -> physicalSpecificationService.search(terms, options);
            case ROADMAP:
                return () -> roadmapService.search(terms, options);
            default:
                throw new UnsupportedOperationException("no search service available for: " + entityKind);
        }
    }
}
