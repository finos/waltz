/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
