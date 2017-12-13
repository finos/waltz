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

import com.khartec.waltz.model.appview.AppView;
import com.khartec.waltz.service.app_view.AppViewService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;

@Service
public class AppViewEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "app-view");

    private final AppViewService appViewService;


    @Autowired
    public AppViewEndpoint(AppViewService appViewService) {
        checkNotNull(appViewService, "appViewService must not be null");

        this.appViewService = appViewService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, ":id");

        DatumRoute<AppView> getByIdRoute = (request, response) -> appViewService.getAppView(getId(request));

        getForDatum(getByIdPath, getByIdRoute);
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AppViewEndpoint{");
        sb.append("appViewService=").append(appViewService);
        sb.append('}');
        return sb.toString();
    }
}
