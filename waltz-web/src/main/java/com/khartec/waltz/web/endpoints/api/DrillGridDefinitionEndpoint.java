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

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.drill_grid.DrillGridDefinitionService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class DrillGridDefinitionEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "drill-grid-definition");

    private final DrillGridDefinitionService drillGridDefinitionService;


    @Autowired
    public DrillGridDefinitionEndpoint(DrillGridDefinitionService drillGridDefinitionService) {
        checkNotNull(drillGridDefinitionService, "drillGridDefinitionService cannot be null");
        this.drillGridDefinitionService = drillGridDefinitionService;
    }


    @Override
    public void register() {
        getForList(BASE_URL, (req, resp) -> drillGridDefinitionService.findAll());
    }
}
