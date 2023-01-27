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

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.common.exception.NotFoundException;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.data.report_grid.ReportGridDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
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
import java.util.stream.Collectors;

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

        LOG.info("ReportGrid - getting by ID={} SelectionOptions={}", id, idSelectionOptions);
        ReportGridDefinition definition = reportGridDao.getGridDefinitionById(id);

        if (definition == null) {
            LOG.warn("No Report Grid Definition found for ID={}", id);
            return Optional.empty();
        }

        EntityKind targetKind = definition.subjectKind();

        ReportGridInstance instance = mkInstance(id, opts, targetKind);

        if (!definition.derivedColumnDefinitions().isEmpty()) {
            Set<ReportGridCell> calculatedCells = ReportGridColumnCalculator.calculate(instance, definition);

            return Optional.of(ImmutableReportGrid
                    .builder()
                    .definition(definition)
                    .instance(ImmutableReportGridInstance
                            .copyOf(instance)
                            .withCellData(SetUtilities.union(instance.cellData(), calculatedCells)))
                    .build());
        }

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

        Set<RatingSchemeItem> ratingSchemeItems = ratingSchemeService.findRatingSchemeItemsByIds(
                cellData.stream().flatMap(d -> d.ratingIdValues().stream()).collect(Collectors.toSet()));

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
        reportGridDao.updateColumnDefinitions(reportGridId, updateCommand);
        return reportGridDao.getGridDefinitionById(reportGridId);
    }


    /**
     * Returns a set of column ids for those columns which may contain comments.
     * This includes:
     * <ul>
     *     <li>Assessment columns</li>
     *     <li>Survey question with the allow_comment field set to true</li>
     * </ul>
     *
     * The primary usage for this method is to determine which grid columns need to have
     * an additional comment column when doing a tabular export
     *
     * @param gridId
     * @return Set of column ids
     */
    public Set<Long> findCommentSupportingColumnIdsForGrid(long gridId) {
        return reportGridDao.findCommentSupportingColumnIdsForGrid(gridId);
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


    public Set<AdditionalColumnOptions> findAdditionalColumnOptionsForKind(EntityKind kind) {
        return AdditionalColumnOptions.findAllowedKinds(kind);
    }


    public ReportGridDefinition getGridDefinitionByExtId(String gridExtId) {
        return reportGridDao.getGridDefinitionByExternalId(gridExtId);
    }

    public ReportGridDefinition clone(long id, ReportGridUpdateCommand updateCommand, String username) {

        ReportGridDefinition gridToClone = reportGridDao.getGridDefinitionById(id);

        if (gridToClone == null) {
            throw new NotFoundException("REPORT_GRID_NOT_FOUND", format("Cannot find grid with id: %d to clone", id));
        }

        ImmutableReportGridCreateCommand newGridCreateCommand = ImmutableReportGridCreateCommand.builder()
                .name(updateCommand.name())
                .description(updateCommand.description())
                .subjectKind(gridToClone.subjectKind())
                .build();

        ReportGridDefinition newGrid = create(newGridCreateCommand, username);

        ImmutableReportGridColumnDefinitionsUpdateCommand updateColsCmd = ImmutableReportGridColumnDefinitionsUpdateCommand.builder()
                .fixedColumnDefinitions(gridToClone.fixedColumnDefinitions())
                .derivedColumnDefinitions(gridToClone.derivedColumnDefinitions())
                .build();

        newGrid.id().ifPresent(newGridId -> reportGridDao.updateColumnDefinitions(newGridId, updateColsCmd));

        return newGrid;
    }
}
