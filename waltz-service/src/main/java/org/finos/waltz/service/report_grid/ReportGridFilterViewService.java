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

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.report_grid.ReportGridDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.app_group.ImmutableAppGroupEntry;
import org.finos.waltz.model.entity_named_note.EntityNamedNote;
import org.finos.waltz.model.report_grid.*;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.entity_named_note.EntityNamedNoteService;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.service.report_grid.ReportGridUtilities.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class ReportGridFilterViewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGridFilterViewService.class);

    private final String REPORT_GRID_APP_GROUP_CREATION_NOTE_TYPE_EXT_ID = "WALTZ_REPORT_GRID_FILTER_PRESET";

    private final ReportGridDao reportGridDao;
    private final ReportGridService reportGridService;
    private final EntityNamedNoteService entityNamedNoteService;
    private final AppGroupService appGroupService;

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public ReportGridFilterViewService(ReportGridDao reportGridDao,
                                       ReportGridService reportGridService,
                                       EntityNamedNoteService entityNamedNoteService,
                                       AppGroupService appGroupService) {

        checkNotNull(reportGridDao, "reportGridDao cannot be null");
        checkNotNull(entityNamedNoteService, "entityNamedNoteService cannot be null");
        checkNotNull(appGroupService, "appGroupService cannot be null");
        checkNotNull(entityNamedNoteService, "entityNamedNoteService cannot be null");

        this.entityNamedNoteService = entityNamedNoteService;
        this.reportGridService = reportGridService;
        this.reportGridDao = reportGridDao;
        this.appGroupService = appGroupService;
    }


    public void generateAppGroupsFromFilter() {

        LOG.info("Populating application groups from filters");

        Set<EntityNamedNote> filterPresetNotes = entityNamedNoteService.findByNoteTypeExtId(REPORT_GRID_APP_GROUP_CREATION_NOTE_TYPE_EXT_ID);

        Set<ReportGridDefinition> grids = reportGridDao.findAll();

        Map<String, ReportGridDefinition> gridsByExternalId = indexBy(grids, d -> d.externalId().get());

        Set<ReportGridFilterInfo> gridInfoWithFilters = findGridInfoWithFilters(filterPresetNotes, gridsByExternalId);

        Set<Tuple2<Long, Set<AppGroupEntry>>> appGroupToEntries = determineAppGroupEntries(gridInfoWithFilters);

        appGroupService.updateGroups(appGroupToEntries);

        LOG.info("Finished updating filter groups");
    }

    private Set<Tuple2<Long, Set<AppGroupEntry>>> determineAppGroupEntries(Set<ReportGridFilterInfo> gridInfoWithFilters) {
        return gridInfoWithFilters
                .stream()
                .map(d -> {

                    EntityKind subjectKind = d.gridDefinition().subjectKind();

                    ReportGridInstance instance = reportGridService.mkInstance(
                            d.gridDefinition().id().get(),
                            d.idSelectionOptions(),
                            subjectKind);

                    Set<ReportGridCell> cellData = instance.cellData();

                    Set<Long> subjectsPassingFilters = applyFilters(cellData, d.gridFilters());

                    Set<AppGroupEntry> appGroupEntries = SetUtilities.map(
                            subjectsPassingFilters,
                            id -> ImmutableAppGroupEntry
                                    .builder()
                                    .id(id)
                                    .kind(subjectKind)
                                    .isReadOnly(true)
                                    .build());

                    return tuple(d.appGroupId(), appGroupEntries);
                })
                .collect(Collectors.toSet());
    }

    private Set<Long> applyFilters(Set<ReportGridCell> cellData, Set<GridFilter> colDefIdToFilterValues) {

        Map<Long, Collection<ReportGridCell>> dataByCol = groupBy(cellData, ReportGridCell::columnDefinitionId);

        Set<Set<Long>> appIdsPassingFilters = colDefIdToFilterValues
                .stream()
                .map(d -> {
                    Collection<ReportGridCell> cellDataForColumn = dataByCol.getOrDefault(d.columnDefinitionId(), emptySet());

                    return cellDataForColumn
                            .stream()
                            .filter(c -> d.optionCodes().contains(c.optionCode()))
                            .map(ReportGridCell::subjectId)
                            .collect(Collectors.toSet());
                })
                .collect(Collectors.toSet());

        if (isEmpty(appIdsPassingFilters)) {
            //If there are no filters all the apps should populate the group
            return SetUtilities.map(cellData, ReportGridCell::subjectId);
        } else {
            return appIdsPassingFilters
                    .stream()
                    .reduce(first(appIdsPassingFilters), SetUtilities::intersection);
        }

    }

    private Set<ReportGridFilterInfo> findGridInfoWithFilters(Set<EntityNamedNote> filterPresetNotes, Map<String, ReportGridDefinition> gridsByExternalId) {

        List<Tuple2<Long, String>> appGroupIdToNoteText = map(filterPresetNotes, d -> tuple(d.entityReference().id(), d.noteText()));

        return appGroupIdToNoteText
                .stream()
                .map(t -> {
                    String noteText = t.v2;
                    Tuple2<ArrayList<List<String>>, ArrayList<List<String>>> gridInfoAndFilters = parseNoteText(noteText);

                    if (gridInfoAndFilters == null) {
                        return null;
                    }

                    //Should only be one row for grid information
                    List<String> gridInfo = gridInfoAndFilters.v1.get(0);
                    ArrayList<List<String>> filterRows = gridInfoAndFilters.v2;
                    String gridExtId = gridInfo.get(1);

                    ReportGridDefinition grid = gridsByExternalId.get(gridExtId);

                    if (grid == null) {
                        LOG.debug(format("Cannot identify grid '%s' from note", gridExtId));
                        return null;
                    }

                    String vantagePointKind = gridInfo.get(2);
                    String vantagePointId = gridInfo.get(3);
                    EntityReference vantagePoint = mkRef(EntityKind.valueOf(vantagePointKind), Long.parseLong(vantagePointId));

                    Set<GridFilter> filterValues = getGridFilters(filterRows, grid);

                    IdSelectionOptions idSelectionOptions = modifySelectionOptionsForGrid(mkOpts(vantagePoint));

                    return ImmutableReportGridFilterInfo.builder()
                            .appGroupId(t.v1)
                            .idSelectionOptions(idSelectionOptions)
                            .gridDefinition(grid)
                            .gridFilters(filterValues)
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    }
}
