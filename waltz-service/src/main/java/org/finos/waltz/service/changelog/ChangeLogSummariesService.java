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

package org.finos.waltz.service.changelog;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.changelog.ChangeLogSummariesDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.tally.ChangeLogTally;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class ChangeLogSummariesService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeLogSummariesService.class);
    private final ChangeLogSummariesDao changeLogSummariesDao;

    GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();

    @Autowired
    public ChangeLogSummariesService(ChangeLogSummariesDao changeLogSummariesDao) {
        checkNotNull(changeLogSummariesDao, "changeLogSummariesDao must not be null");

        this.changeLogSummariesDao = changeLogSummariesDao;
    }

    public List<ChangeLogTally> findCountByParentAndChildKindForDateRangeBySelector(EntityKind targetKind,
                                                                                    IdSelectionOptions options,
                                                                                    Date startDate,
                                                                                    Date endDate,
                                                                                    Optional<Integer> limit) {

        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, options);

        return changeLogSummariesDao.findCountByParentAndChildKindForDateRangeBySelector(
                genericSelector,
                startDate,
                endDate,
                limit);
    }

    public Map<Integer, Long> findYearOnYearChanges(EntityKind parentEntityKind, EntityKind childEntityKind) {
        return changeLogSummariesDao.findYearOnYearChanges(parentEntityKind, childEntityKind);
    }

    public List<String> findChangeLogParentEntities() {
        return changeLogSummariesDao.findChangeLogParentEntities();
    }

    public List<Integer> findChangeLogYears() {
        return changeLogSummariesDao.findChangeLogYears();
    }

    public Map<Integer, Long> findMonthOnMonthChanges(EntityKind parentEntityKind, EntityKind childEntityKind, Integer currentYear) {
        return changeLogSummariesDao.findMonthOnMonthChanges(parentEntityKind, childEntityKind, currentYear);
    }

    public Map<String, Map<String, Long>> findChangesByPeriod(EntityKind parentEntityKind, EntityKind childEntityKind, LocalDate startDate, LocalDate endDate, String freq) {
        return changeLogSummariesDao.findChangesByPeriod(parentEntityKind, childEntityKind, startDate, endDate, freq);
    }

    public Map<String, Long> findChangesBySeverity(String startDate, String endDate) {
        return changeLogSummariesDao.findChangesBySeverity(startDate, endDate);
    }

    public Map<String, Long> findChangesByEntityKind(String startDate, String endDate, int limit) {
        return changeLogSummariesDao.findChangesByEntityKind(startDate, endDate, limit);
    }

    public Map<String, Long> findTopContributors(String startDate, String endDate, int limit) {
        return changeLogSummariesDao.findTopContributors(startDate, endDate, limit);
    }

    public Map<String, Map<String, Long>> findTopContributorsByPeriod(String startDate, String endDate, String freq, int limit) {
        return changeLogSummariesDao.findTopContributorsByPeriod(startDate, endDate, freq, limit);
    }

    public Map<Integer, Long> findChangesByHourOfDay(String startDate, String endDate) {
        return changeLogSummariesDao.findChangesByHourOfDay(startDate, endDate);
    }

    public Map<Integer, Long> findChangesByDayOfWeek(String startDate, String endDate) {
        return changeLogSummariesDao.findChangesByDayOfWeek(startDate, endDate);
    }

    public Map<String, Long> findChangesByOperation(String startDate, String endDate) {
        return changeLogSummariesDao.findChangesByOperation(startDate, endDate);
    }

    public Map<String, Long> findChangesByChildKind(String startDate, String endDate, int limit) {
        return changeLogSummariesDao.findChangesByChildKind(startDate, endDate, limit);
    }

    public Map<String, Map<String, Long>> findOperationTrends(String startDate, String endDate, String freq) {
        return changeLogSummariesDao.findOperationTrends(startDate, endDate, freq);
    }
}
