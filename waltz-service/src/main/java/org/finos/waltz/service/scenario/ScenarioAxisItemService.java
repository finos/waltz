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

package org.finos.waltz.service.scenario;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.data.scenario.ScenarioAxisItemDao;
import org.finos.waltz.model.AxisOrientation;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.scenario.ScenarioAxisItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static org.finos.waltz.service.scenario.ScenarioUtilities.mkBasicLogEntry;
import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class ScenarioAxisItemService {


    private final ScenarioAxisItemDao scenarioAxisItemDao;
    private final ChangeLogService changeLogService;

    
    @Autowired
    public ScenarioAxisItemService(ScenarioAxisItemDao scenarioAxisItemDao, ChangeLogService changeLogService) {
        checkNotNull(scenarioAxisItemDao, "scenarioAxisItemDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.scenarioAxisItemDao = scenarioAxisItemDao;
        this.changeLogService = changeLogService;
    }


    public Collection<ScenarioAxisItem> findForScenarioId(long scenarioId) {
        return scenarioAxisItemDao.findForScenarioId(scenarioId);
    }


    public Collection<ScenarioAxisItem> loadAxis(long scenarioId, AxisOrientation orientation) {
        return scenarioAxisItemDao.findForScenarioAndOrientation(
                scenarioId,
                orientation);
    }


    public Boolean addAxisItem(long scenarioId,
                               AxisOrientation orientation,
                               EntityReference domainItem,
                               Integer position,
                               String userId) {
        boolean result = scenarioAxisItemDao.add(
                scenarioId,
                orientation,
                domainItem,
                position);

        if (result) {
            String message = String.format(
                    "Added item to %s axis: %s}",
                    orientation.name(),
                    domainItem.toString());
            changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));
        }

        return result;
    }


    public Boolean removeAxisItem(long scenarioId,
                                  AxisOrientation orientation,
                                  EntityReference domainItem,
                                  String userId) {
        Boolean result = scenarioAxisItemDao.remove(
                scenarioId,
                orientation,
                domainItem);

        if (result) {
            String message = String.format(
                    "Removed item from %s axis: %s}",
                    orientation.name(),
                    domainItem.toString());
            changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));
        }

        return result;
    }


    public int[] reorderAxis(long scenarioId, AxisOrientation orientation, List<Long> orderedIds, String userId) {
        int[] result = scenarioAxisItemDao.reorder(
                scenarioId,
                orientation,
                orderedIds);

        String message = String.format(
                "Reordered axis: %s}",
                orientation.name());
        changeLogService.write(mkBasicLogEntry(scenarioId, message, userId));

        return result;
    }




}
