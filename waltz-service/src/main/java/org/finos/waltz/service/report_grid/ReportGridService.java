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
import org.finos.waltz.model.*;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.report_grid.ReportGridDao;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.report_grid.*;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.map;

@Service
public class ReportGridService {

    private final RatingSchemeService ratingSchemeService;
    private final ApplicationDao applicationDao;
    private final ReportGridDao reportGridDao;
    private final ReportGridMemberService reportGridMemberService;
    private final ChangeLogService changeLogService;

    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ReportGridService(ReportGridDao reportGridDao,
                             ApplicationDao applicationDao,
                             RatingSchemeService ratingSchemeService,
                             ReportGridMemberService reportGridMemberService,
                             ChangeLogService changeLogService) {
        checkNotNull(reportGridDao, "reportGridDao cannot be null");
        checkNotNull(reportGridMemberService, "reportGridMemberService cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.reportGridDao = reportGridDao;
        this.reportGridMemberService = reportGridMemberService;
        this.applicationDao = applicationDao;
        this.ratingSchemeService = ratingSchemeService;
        this.changeLogService = changeLogService;
    }


    public Set<ReportGridDefinition> findAll(){
        return reportGridDao.findAll();
    }


    public Set<ReportGridDefinition> findForUser(String username){
        return reportGridDao.findForUser(username);
    }


    public ReportGrid getByIdAndSelectionOptions(
            long id,
            IdSelectionOptions idSelectionOptions) {

        // WARNING:  The grid computation is very slow if given a large person tree.
        //    Therefore we restrict it to EXACT only behaviour.
        //    If you are changing this please ensure you have tested with realistic test data.

        IdSelectionOptions opts = idSelectionOptions.entityReference().kind() == EntityKind.PERSON
                ? ImmutableIdSelectionOptions
                    .copyOf(idSelectionOptions)
                    .withScope(HierarchyQueryScope.EXACT)
                : idSelectionOptions;

        ReportGridDefinition definition = reportGridDao.getGridDefinitionById(id);

        if(definition == null){
            return null;
        }

        ReportGridInstance instance = mkInstance(id, opts);

        return ImmutableReportGrid
                .builder()
                .definition(definition)
                .instance(instance)
                .build();
    }


    private ReportGridInstance mkInstance(long id, IdSelectionOptions idSelectionOptions) {
        Select<Record1<Long>> appSelector = applicationIdSelectorFactory.apply(idSelectionOptions);
        Set<ReportGridCell> cellData = reportGridDao.findCellDataByGridId(id, appSelector);
        List<Application> apps = applicationDao.findByAppIdSelector(appSelector);

        Set<RatingSchemeItem> ratingSchemeItems = ratingSchemeService.findRatingSchemeItemsByIds(map(
                cellData,
                ReportGridCell::ratingId));

        ReportGridInstance instance = ImmutableReportGridInstance
                .builder()
                .applications(apps)
                .cellData(cellData)
                .ratingSchemeItems(ratingSchemeItems)
                .build();
        return instance;
    }


    public ReportGridDefinition updateColumnDefinitions(long reportGridId,
                                                        ReportGridColumnDefinitionsUpdateCommand updateCommand,
                                                        String username) throws InsufficientPrivelegeException {
        checkIsOwner(reportGridId, username);
        int newColumnCount = reportGridDao.updateColumnDefinitions(reportGridId, updateCommand.columnDefinitions());
        return reportGridDao.getGridDefinitionById(reportGridId);
    }


    public ReportGridDefinition create(ReportGridCreateCommand createCommand, String username){
        long gridId = reportGridDao.create(createCommand, username);
        reportGridMemberService.register(gridId, username, ReportGridMemberRole.OWNER);
        return reportGridDao.getGridDefinitionById(gridId);
    }


    public ReportGridDefinition update(long id, ReportGridUpdateCommand updateCommand, String username) throws InsufficientPrivelegeException {
        checkIsOwner(id, username);
        long gridId = reportGridDao.update(id, updateCommand, username);
        return reportGridDao.getGridDefinitionById(id);
    }


    private void checkIsOwner(long reportGridId, String username) throws InsufficientPrivelegeException {
        reportGridMemberService.checkIsOwner(reportGridId, username);
    }

    public Set<ReportGridDefinition> findForOwner(String username) {
        return reportGridDao.findForOwner(username);
    }
}
