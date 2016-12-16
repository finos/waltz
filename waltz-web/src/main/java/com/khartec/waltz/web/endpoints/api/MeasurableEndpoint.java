/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class MeasurableEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable");

    private final MeasurableService measurableService;


    @Autowired
    public MeasurableEndpoint(MeasurableService measurableService) {
        this.measurableService = measurableService;
    }


    @Override
    public void register() {

        String findAllPath = mkPath(BASE_URL, "all");

        ListRoute<Measurable> findAllRoute = (request, response)
                -> measurableService.findAll();

        getForList(findAllPath, findAllRoute);
    }

}
