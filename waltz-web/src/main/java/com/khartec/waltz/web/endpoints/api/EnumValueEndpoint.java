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

import com.khartec.waltz.model.EnumValue;
import com.khartec.waltz.service.enum_value.EnumValueService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class EnumValueEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "enum-value");
    private final EnumValueService enumValueService;


    @Autowired
    public EnumValueEndpoint(EnumValueService enumValueService) {
        checkNotNull(enumValueService, "enumValueService cannot be null");
        this.enumValueService = enumValueService;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;

        ListRoute<EnumValue> findAllRoute = (req, resp) -> enumValueService.findAll();

        getForList(findAllPath, findAllRoute);
    }
}
