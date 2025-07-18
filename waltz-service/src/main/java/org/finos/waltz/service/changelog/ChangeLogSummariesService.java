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
}
