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

package com.khartec.waltz.service.scenario;

import com.khartec.waltz.data.scenario.ScenarioRatingItemDao;
import com.khartec.waltz.model.scenario.ScenarioRatingItem;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.service.scenario.ScenarioUtilities.mkBasicLogEntry;

@Service
public class ScenarioRatingItemService {

    private final ScenarioRatingItemDao scenarioRatingItemDao;
    private final ChangeLogService changeLogService;



    @Autowired
    public ScenarioRatingItemService(ScenarioRatingItemDao scenarioRatingItemDao,
                                     ChangeLogService changeLogService) {
        checkNotNull(scenarioRatingItemDao, "scenarioRatingItemDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.scenarioRatingItemDao = scenarioRatingItemDao;
        this.changeLogService = changeLogService;
    }


    public Collection<ScenarioRatingItem> findForScenarioId(long scenarioId) {
        return scenarioRatingItemDao.findForScenarioId(scenarioId);
    }


    public boolean remove(long scenarioId, long appId, long columnId, long rowId, String userId) {
        boolean result = scenarioRatingItemDao.remove(scenarioId, appId, columnId, rowId, userId);

        if (result) {
            String message = String.format(
                    "Removed app %d from colId: %d, rowId: %d",
                    appId,
                    columnId,
                    rowId);
            changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));
        }

        return result;
    }


    public boolean add(long scenarioId, long appId, long columnId, long rowId, char rating, String userId) {
        boolean result = scenarioRatingItemDao.add(scenarioId, appId, columnId, rowId, rating, userId);
        if (result) {
            String message = String.format(
                    "Added app %d to colId: %d, rowId: %d",
                    appId,
                    columnId,
                    rowId);
            changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));
        }

        return result;
    }


    public boolean updateRating(long scenarioId, long appId, long columnId, long rowId, char rating, String comment, String userId) {
        boolean result = scenarioRatingItemDao.updateRating(scenarioId, appId, columnId, rowId, rating, comment, userId);
        if (result) {
            String message = String.format(
                    "Updated rating/description for app %d in colId: %d, rowId: %d",
                    appId,
                    columnId,
                    rowId);
            changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));
        }

        return result;
    }
}
