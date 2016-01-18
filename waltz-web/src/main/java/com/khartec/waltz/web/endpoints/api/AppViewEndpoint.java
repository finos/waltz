/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.appview.AppView;
import com.khartec.waltz.service.app_view.AppViewService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class AppViewEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "app-view");

    private final AppViewService appViewService;


    @Autowired
    public AppViewEndpoint(AppViewService appViewService) {
        checkNotNull(appViewService, "appViewService must not be null");

        this.appViewService = appViewService;
    }


    @Override
    public void register() {
        DatumRoute<AppView> getById = (request, response) -> appViewService.getAppView(WebUtilities.getId(request));

        EndpointUtilities.getForDatum(WebUtilities.mkPath(BASE_URL, ":id"), getById);
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AppViewEndpoint{");
        sb.append("appViewService=").append(appViewService);
        sb.append('}');
        return sb.toString();
    }
}
