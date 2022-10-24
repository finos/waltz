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
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.report_grid.ReportGridDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.app_group.ImmutableAppGroupEntry;
import org.finos.waltz.model.entity_named_note.EntityNamedNote;
import org.finos.waltz.model.rating.RatingSchemeItem;
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
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.common.StringUtilities.notEmpty;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;
import static org.finos.waltz.service.report_grid.ReportGridUtilities.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Service
public class ReportGridFilterViewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportGridFilterViewService.class);

    private final String REPORT_GRID_APP_GROUP_CREATION_NOTE_TYPE_EXT_ID = "WALTZ_REPORT_GRID_FILTER_PRESET";
    private final String NOT_PROVIDED_OPTION_CODE = "NOT_PROVIDED";

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


    public int recalculateAppGroupFromNoteText(Long appGroupId) {

        Set<ReportGridDefinition> grids = reportGridDao.findAll();
        Map<String, ReportGridDefinition> gridsByExternalId = indexBy(grids, d -> d.externalId().get());

        Set<EntityNamedNote> filterNotesForGroup = entityNamedNoteService.findByNoteTypeExtIdAndEntityReference(
                REPORT_GRID_APP_GROUP_CREATION_NOTE_TYPE_EXT_ID,
                mkRef(EntityKind.APP_GROUP, appGroupId));

        if (isEmpty(filterNotesForGroup)) {
            throw new IllegalArgumentException(format(
                    "Cannot find Report Grid Filter Preset note for application group: %d",
                    appGroupId));
        }

        if (filterNotesForGroup.size() > 1) {
            throw new IllegalArgumentException("Cannot have more than one Report Grid Filter note per application group");
        }

        //should only be one note per group.
        EntityNamedNote note = first(filterNotesForGroup);
        ReportGridFilterInfo gridFilterInfo = getGridFilterInfo(gridsByExternalId, appGroupId, note.noteText());

        if (gridFilterInfo == null) {
            throw new IllegalArgumentException("Cannot get filter grid info from note text");
        } else {
            Tuple2<Long, Set<AppGroupEntry>> appGroupIdToEntries = determineApplicationsInGroup(gridFilterInfo);
            appGroupService.replaceGroupEntries(asSet(appGroupIdToEntries));
            return appGroupIdToEntries.v2.size();
        }
    }


    public void generateAppGroupsFromFilter() {
        LOG.info("Starting filter group population");

        LOG.info("Loading filter info from notes");
        Set<ReportGridFilterInfo> gridInfoWithFilters = findGridInfoWithFilters();

        Set<Tuple2<Long, Set<AppGroupEntry>>> appGroupToEntries = determineAppGroupEntries(gridInfoWithFilters);

        LOG.info("Populating application groups from filters");
        appGroupService.replaceGroupEntries(appGroupToEntries);

        LOG.info("Finished updating filter groups");
    }


    private Set<Tuple2<Long, Set<AppGroupEntry>>> determineAppGroupEntries(Set<ReportGridFilterInfo> gridInfoWithFilters) {
        return gridInfoWithFilters
                .stream()
                .map(this::determineApplicationsInGroup)
                .collect(Collectors.toSet());
    }

    private Tuple2<Long, Set<AppGroupEntry>> determineApplicationsInGroup(ReportGridFilterInfo d) {
        EntityKind subjectKind = d.gridDefinition().subjectKind();

        ReportGridInstance instance = reportGridService.mkInstance(
                d.gridDefinition().id().get(),
                d.idSelectionOptions(),
                subjectKind);

        Set<ReportGridCell> cellData = instance.cellData();

        Set<Long> subjectIds = SetUtilities.map(instance.subjects(), s -> s.entityReference().id());

        Set<Long> subjectsPassingFilters = applyFilters(
                cellData,
                d.gridFilters(),
                subjectIds,
                instance.ratingSchemeItems());

        Set<AppGroupEntry> appGroupEntries = SetUtilities.map(
                subjectsPassingFilters,
                id -> ImmutableAppGroupEntry
                        .builder()
                        .id(id)
                        .kind(subjectKind)
                        .isReadOnly(true)
                        .build());

        return tuple(d.appGroupId(), appGroupEntries);
    }


    private Set<Long> applyFilters(Set<ReportGridCell> cellData,
                                   Set<GridFilter> gridFilters,
                                   Set<Long> subjectIds,
                                   Set<RatingSchemeItem> ratingSchemeItems) {

        if (isEmpty(gridFilters)) {
            //If there are no filters all the apps should populate the group
            return SetUtilities.map(cellData, ReportGridCell::subjectId);
        } else {
            Map<Long, RatingSchemeItem> ratingSchemeItemByIdMap = indexBy(ratingSchemeItems, d -> d.id().get());

            Map<Long, Collection<ReportGridCell>> dataByCol = groupBy(cellData, ReportGridCell::columnDefinitionId);

            Set<Set<Long>> appIdsPassingFilters = gridFilters
                    .stream()
                    .map(filter -> {
                        Collection<ReportGridCell> cellDataForColumn = dataByCol.getOrDefault(filter.columnDefinitionId(), emptySet());

                        if (filter.filterOperator().equals(FilterOperator.CONTAINS_ANY_OPTION)) {
                            return determineAppsPassingContainsOperatorFilter(subjectIds, ratingSchemeItemByIdMap, filter, cellDataForColumn);
                        } else if (filter.filterOperator().equals(FilterOperator.CONTAINS_ANY_STRING)) {
                            return determineAppsPassingContainsStringFilter(filter, cellDataForColumn);
                        } else {
                            return subjectIds; // return all apps if filter operator not supported to support intersection
                        }
                    })
                    .collect(Collectors.toSet());

            return appIdsPassingFilters
                    .stream()
                    .reduce(first(appIdsPassingFilters), SetUtilities::intersection);
        }
    }


    private Set<Long> determineAppsPassingContainsStringFilter(GridFilter filter,
                                                               Collection<ReportGridCell> cellDataForColumn) {
        return cellDataForColumn
                .stream()
                .filter(c -> notEmpty(c.textValue()) && containsAny(filter.filterValues(), c.textValue()))
                .map(ReportGridCell::subjectId)
                .collect(Collectors.toSet());
    }


    private boolean containsAny(Set<String> searchStrings, String lookupString) {
        for (String text : searchStrings) {
            if (lookupString.contains(text)) {
                return true;
            }
        }
        return false;
    }


    private Set<Long> determineAppsPassingContainsOperatorFilter(Set<Long> subjectIds,
                                                                 Map<Long, RatingSchemeItem> ratingSchemeItemByIdMap,
                                                                 GridFilter filter,
                                                                 Collection<ReportGridCell> cellDataForColumn) {
        Set<Long> appsPassingFilter = cellDataForColumn
                .stream()
                .filter(c -> {
                    // rating cells may want to look up on rating id / code / external id
                    if (c.ratingIdValue() != null) {
                        RatingSchemeItem rating = ratingSchemeItemByIdMap.get(c.ratingIdValue());
                        Set<String> ratingIdentifiers = asSet(c.optionCode(), String.valueOf(rating.rating()), rating.name(), rating.externalId().orElse(null));
                        return CollectionUtilities.notEmpty(intersection(filter.filterValues(), ratingIdentifiers));
                    } else {
                        return filter.filterValues().contains(c.optionCode());
                    }
                })
                .map(ReportGridCell::subjectId)
                .collect(Collectors.toSet());

        if (filter.filterValues().contains(NOT_PROVIDED_OPTION_CODE)) {
            Set<Long> subjectIdsWithValues = SetUtilities.map(cellDataForColumn, ReportGridCell::subjectId);
            Set<Long> subjectIdsWithoutValue = minus(subjectIds, subjectIdsWithValues);

            return union(appsPassingFilter, subjectIdsWithoutValue);
        } else {
            return appsPassingFilter;
        }
    }


    private Set<ReportGridFilterInfo> findGridInfoWithFilters() {

        Set<EntityNamedNote> filterPresetNotes = entityNamedNoteService.findByNoteTypeExtId(REPORT_GRID_APP_GROUP_CREATION_NOTE_TYPE_EXT_ID);

        Set<ReportGridDefinition> grids = reportGridDao.findAll();
        Map<String, ReportGridDefinition> gridsByExternalId = indexBy(grids, d -> d.externalId().get());

        List<Tuple2<Long, String>> appGroupIdToNoteText = map(filterPresetNotes, d -> tuple(d.entityReference().id(), d.noteText()));

        return appGroupIdToNoteText
                .stream()
                .map(t -> getGridFilterInfo(gridsByExternalId, t.v1, t.v2))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    }


    private ReportGridFilterInfo getGridFilterInfo(Map<String, ReportGridDefinition> gridsByExternalId,
                                                   Long appGroupId,
                                                   String noteText) {

        Tuple2<List<String>, List<List<String>>> gridInfoAndFilters;

        try {
            gridInfoAndFilters = parseGridFilterNoteText(noteText);
        } catch (IllegalArgumentException e) {
            LOG.debug("Could not parse note text. " + e.getMessage());
            return null;
        }

        if (gridInfoAndFilters == null) {
            return null;
        }

        //Should only be one row for grid information
        List<String> gridInfo = gridInfoAndFilters.v1;
        List<List<String>> filterRows = gridInfoAndFilters.v2;
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
                .appGroupId(appGroupId)
                .idSelectionOptions(idSelectionOptions)
                .gridDefinition(grid)
                .gridFilters(filterValues)
                .build();
    }
}
