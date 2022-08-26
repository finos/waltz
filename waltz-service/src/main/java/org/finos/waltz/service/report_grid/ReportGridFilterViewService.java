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
import org.finos.waltz.model.report_grid.ReportGridCell;
import org.finos.waltz.model.report_grid.ReportGridColumnDefinition;
import org.finos.waltz.model.report_grid.ReportGridDefinition;
import org.finos.waltz.model.report_grid.ReportGridInstance;
import org.finos.waltz.service.app_group.AppGroupService;
import org.finos.waltz.service.entity_named_note.EntityNamedNoteService;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;
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

        Set<EntityNamedNote> filterPresetNotes = entityNamedNoteService.findByNoteTypeExtId(REPORT_GRID_APP_GROUP_CREATION_NOTE_TYPE_EXT_ID);

        Set<ReportGridDefinition> grids = reportGridDao.findAll();

        Map<String, ReportGridDefinition> gridsByExternalId = indexBy(grids, d -> d.externalId().get());

        Set<Tuple4<Long, ReportGridDefinition, IdSelectionOptions, Set<Tuple2<Long, Set<String>>>>> gridInfoWithFilters = findGridInfoWithFilters(filterPresetNotes, gridsByExternalId);

        Set<Tuple2<Long, Set<AppGroupEntry>>> appGroupToEntries = gridInfoWithFilters
                .stream()
                .map(t -> {
                    Long appGroup = t.v1;
                    ReportGridDefinition gridDefinition = t.v2;
                    IdSelectionOptions opts = t.v3;

                    ReportGridInstance instance = reportGridService.mkInstance(gridDefinition.id().get(), opts, gridDefinition.subjectKind());
                    Set<ReportGridCell> cellData = instance.cellData();

                    Set<Long> subjectsPassingFilters = applyFilters(cellData, t.v4);

                    Set<AppGroupEntry> appGroupEntries = SetUtilities.map(subjectsPassingFilters, id -> ImmutableAppGroupEntry
                            .builder()
                            .id(id)
                            .kind(gridDefinition.subjectKind())
                            .isReadOnly(true)
                            .build());

                    System.out.println(subjectsPassingFilters.size());
                    return tuple(appGroup, appGroupEntries);
                })
                .collect(Collectors.toSet());

        appGroupService.updateGroups(appGroupToEntries);

        LOG.info("Finished updating filter groups");
    }

    private Set<Long> applyFilters(Set<ReportGridCell> cellData, Set<Tuple2<Long, Set<String>>> colDefIdToFilterValues) {

        Map<Long, Collection<ReportGridCell>> dataByCol = groupBy(cellData, ReportGridCell::columnDefinitionId);

        Set<Set<Long>> appIdsPassingFilters = colDefIdToFilterValues
                .stream()
                .map(d -> {
                    Collection<ReportGridCell> cellDataForColumn = dataByCol.getOrDefault(d.v1, emptySet());

                    return cellDataForColumn
                            .stream()
                            .filter(c -> d.v2.contains(c.optionCode()))
                            .map(ReportGridCell::subjectId)
                            .collect(Collectors.toSet());
                })
                .collect(Collectors.toSet());

        if (isEmpty(appIdsPassingFilters)) {
            return SetUtilities.map(cellData, ReportGridCell::subjectId);
        } else {
            return appIdsPassingFilters
                    .stream()
                    .reduce(first(appIdsPassingFilters), SetUtilities::intersection);
        }

    }

    private Set<Tuple4<Long, ReportGridDefinition, IdSelectionOptions, Set<Tuple2<Long, Set<String>>>>> findGridInfoWithFilters(Set<EntityNamedNote> filterPresetNotes, Map<String, ReportGridDefinition> gridsByExternalId) {

        List<Tuple2<Long, String>> appGroupIdToNoteText = map(filterPresetNotes, d -> tuple(d.entityReference().id(), d.noteText()));

        return appGroupIdToNoteText
                .stream()
                .map(t -> {
                    String noteText = t.v2;
                    Tuple2<ArrayList<List<String>>, ArrayList<List<String>>> headersAndFilters = parseNoteText(noteText);

                    if (headersAndFilters == null) {
                        return null;
                    }

                    List<String> gridInfo = headersAndFilters.v1.get(0);
                    ArrayList<List<String>> filterRows = headersAndFilters.v2;
                    String gridExtId = gridInfo.get(1);
                    ReportGridDefinition grid = gridsByExternalId.get(gridExtId);

                    if (grid == null) {
                        LOG.debug(format("Cannot identify grid '%s' from note", gridExtId));
                        return null;
                    }

                    String vantagePointKind = gridInfo.get(2);
                    String vantagePointId = gridInfo.get(3);
                    EntityReference vantagePoint = mkRef(EntityKind.valueOf(vantagePointKind), Long.parseLong(vantagePointId));

                    Map<String, Long> columnsDefinitionIdByName = indexBy(grid.columnDefinitions(),
                            r -> r.entityFieldReference() == null ? sanitizeString(r.columnName()) : sanitizeString(format("%s/%s", r.entityFieldReference().displayName(), r.columnName())),
                            ReportGridColumnDefinition::id);

                    Set<Tuple2<Long, Set<String>>> filterValues = filterRows
                            .stream()
                            .map(r -> {
                                String columnName = sanitizeString(r.get(0));
                                Long columnDefnId = columnsDefinitionIdByName.get(columnName);

                                if (columnDefnId == null) {
                                    LOG.info(format("Cannot find column '%s' on grid. Skipping this filter", columnName));
                                    return null;
                                } else {
                                    return tuple(columnDefnId, getFilterValues(r.get(1)));
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    IdSelectionOptions idSelectionOptions = modifySelectionOptionsForGrid(mkOpts(vantagePoint));

                    return tuple(t.v1, grid, idSelectionOptions, filterValues);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    }
}
