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

package com.khartec.waltz.service.drill_grid;


import com.khartec.waltz.data.drill_grid.DrillGridDefinitionDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.drill_grid.DrillGridDefinition;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class DrillGridDefinitionService {

    private final DrillGridDefinitionDao drillGridDefinitionDao;
    private final ChangeLogService changeLogService;

    @Autowired
    public DrillGridDefinitionService(DrillGridDefinitionDao drillGridDefinitionDao, ChangeLogService changeLogService) {
        checkNotNull(drillGridDefinitionDao, "drillGridDefinitionDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.drillGridDefinitionDao = drillGridDefinitionDao;
        this.changeLogService = changeLogService;
    }


    public List<DrillGridDefinition> findAll() {
        return drillGridDefinitionDao.findAll();
    }


    public DrillGridDefinition updateDescription(String userId,
                                                 long id,
                                                 String description) {
        ChangeLog logEntry = ImmutableChangeLog.builder()
                .userId(userId)
                .operation(Operation.UPDATE)
                .parentReference(EntityReference.mkRef(EntityKind.DRILL_GRID_DEFINITION, id))
                .message("Updating description")
                .severity(Severity.INFORMATION)
                .build();
        changeLogService.write(logEntry);
        return drillGridDefinitionDao.updateDescription(id, description);
    }

}
