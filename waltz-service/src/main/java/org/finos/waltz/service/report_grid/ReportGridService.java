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

package org.finos.waltz.service.report_grid;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.data.report_grid.ReportGridDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.ImmutableIdSelectionOptions;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.user.UserRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.report_grid.ReportGridUtilities.modifySelectionOptionsForGrid;

@Service
public class ReportGridService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGridService.class);

    private final RatingSchemeService ratingSchemeService;
    private final ApplicationDao applicationDao;
    private final ChangeInitiativeDao changeInititativeDao;
    private final ReportGridDao reportGridDao;
    private final ReportGridMemberService reportGridMemberService;
    private final UserRoleService userRoleService;

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public ReportGridService(ReportGridDao reportGridDao,
                             ApplicationDao applicationDao,
                             RatingSchemeService ratingSchemeService,
                             ReportGridMemberService reportGridMemberService,
                             UserRoleService userRoleService,
                             ChangeInitiativeDao changeInitiativeDao) {
        checkNotNull(reportGridDao, "reportGridDao cannot be null");
        checkNotNull(reportGridMemberService, "reportGridMemberService cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.reportGridDao = reportGridDao;
        this.reportGridMemberService = reportGridMemberService;
        this.applicationDao = applicationDao;
        this.ratingSchemeService = ratingSchemeService;
        this.changeInititativeDao = changeInitiativeDao;
        this.userRoleService = userRoleService;
    }


    public Set<ReportGridDefinition> findAll(){
        return reportGridDao.findAll();
    }

    public Optional<ReportGridDefinition> findByExternalId(String externalId){
        return Optional.ofNullable(reportGridDao.getGridDefinitionByExternalId(externalId));
    }

    public Set<ReportGridDefinition> findForUser(String username){
        return reportGridDao.findForUser(username);
    }


    public Optional<ReportGrid> getByIdAndSelectionOptions(
            long id,
            IdSelectionOptions idSelectionOptions) {

        // WARNING:  The grid computation is very slow if given a large person tree.
        //    Therefore we restrict it to EXACT only behaviour.
        //    If you are changing this please ensure you have tested with realistic test data.
        IdSelectionOptions opts = modifySelectionOptionsForGrid(idSelectionOptions);

        LOG.info("ReportGrid - getting by ID={} SelectionOptions={}",id,idSelectionOptions);

        ReportGridDefinition definition = reportGridDao.getGridDefinitionById(id);

        if (definition == null) {
            LOG.warn("No Report Grid Definition found for ID={}", id);
            return Optional.empty();
        }

        EntityKind targetKind = definition.subjectKind();

        ReportGridInstance instance = mkInstance(id, opts, targetKind);

        return Optional.of(ImmutableReportGrid
                .builder()
                .definition(definition)
                .instance(instance)
                .build());
    }


    public ReportGridInstance mkInstance(long id, IdSelectionOptions idSelectionOptions, EntityKind targetKind) {

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, idSelectionOptions);
        Set<ReportGridCell> cellData = reportGridDao.findCellDataByGridId(id, genericSelector);
        Set<ReportSubject> subjects = getReportSubjects(genericSelector);

        Set<RatingSchemeItem> ratingSchemeItems = ratingSchemeService.findRatingSchemeItemsByIds(map(
                cellData,
                ReportGridCell::ratingIdValue));

        return ImmutableReportGridInstance
                .builder()
                .subjects(subjects)
                .cellData(cellData)
                .ratingSchemeItems(ratingSchemeItems)
                .build();
    }


    private Set<ReportSubject> getReportSubjects(GenericSelector genericSelector) {

        if (genericSelector.kind().equals(EntityKind.APPLICATION)) {
            List<Application> apps = applicationDao.findByAppIdSelector(genericSelector.selector());

            return map(apps, d -> ImmutableReportSubject
                    .builder()
                    .entityReference(mkRef(
                            EntityKind.APPLICATION,
                            d.entityReference().id(),
                            d.name(),
                            d.description(),
                            d.externalId().get()))
                    .lifecyclePhase(d.lifecyclePhase())
                    .build());

        } else if (genericSelector.kind().equals(EntityKind.CHANGE_INITIATIVE)) {
            Collection<ChangeInitiative> changeInitiatives = changeInititativeDao.findForSelector(genericSelector.selector());

            return map(changeInitiatives, d -> ImmutableReportSubject
                    .builder()
                    .entityReference(mkRef(
                            EntityKind.CHANGE_INITIATIVE,
                            d.entityReference().id(),
                            d.name(),
                            d.description(),
                            d.externalId().get()))
                    .lifecyclePhase(d.lifecyclePhase())
                    .build());
        } else {
            return Collections.emptySet();
        }
    }


    public ReportGridDefinition updateColumnDefinitions(long reportGridId,
                                                        ReportGridColumnDefinitionsUpdateCommand updateCommand,
                                                        String username) throws InsufficientPrivelegeException {
        checkIsOwner(reportGridId, username);
        reportGridDao.updateColumnDefinitions(reportGridId, updateCommand.columnDefinitions());
        return reportGridDao.getGridDefinitionById(reportGridId);
    }


    public ReportGridDefinition create(ReportGridCreateCommand createCommand,
                                       String username){
        long gridId = reportGridDao.create(createCommand, username);
        reportGridMemberService.register(gridId, username, ReportGridMemberRole.OWNER);
        return reportGridDao.getGridDefinitionById(gridId);
    }


    public ReportGridDefinition update(long id,
                                       ReportGridUpdateCommand updateCommand,
                                       String username) throws InsufficientPrivelegeException {
        checkIsOwner(id, username);
        ReportGridDefinition defn = reportGridDao.getGridDefinitionById(id);

        if (defn.kind() != updateCommand.kind()) {
            checkTrue(userRoleService.hasRole(username, SystemRole.REPORT_GRID_ADMIN),
                    "You do not have permission to change the kind of a report grid");
        }

        reportGridDao.update(id, updateCommand, username);
        return reportGridDao.getGridDefinitionById(id);
    }


    private void checkIsOwner(long reportGridId,
                              String username) throws InsufficientPrivelegeException {
        reportGridMemberService.checkIsOwner(reportGridId, username);
    }


    public Set<ReportGridDefinition> findForOwner(String username) {
        return reportGridDao.findForOwner(username);
    }


    public boolean remove(long gridId,
                          String username) throws InsufficientPrivelegeException {
        ReportGridDefinition gridDef = reportGridDao.getGridDefinitionById(gridId);
        if (gridDef == null) {
            throw new NotFoundException(
                    "REPORT_GRID_NOT_FOUND",
                    format("Grid def: %d not found", gridId));
        }
        reportGridMemberService.checkIsOwner(gridId, username);

        return reportGridDao.remove(gridId);
    }


    public ReportGridDefinition getGridDefinitionById(long gridId) {
        return reportGridDao.getGridDefinitionById(gridId);
    }
}
