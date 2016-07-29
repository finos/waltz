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

import com.khartec.waltz.model.user.UserPreference;
import com.khartec.waltz.service.user.UserPreferenceService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class UserPreferenceEndpoint implements Endpoint {


    @Autowired
    public UserPreferenceEndpoint(UserPreferenceService userPreferenceService) {
        checkNotNull(userPreferenceService, "userPreferenceService cannot be null");


        this.userPreferenceService = userPreferenceService;
    }

    private static final String BASE_URL = mkPath("api", "user-preference");

    private final UserPreferenceService userPreferenceService;


    @Override
    public void register() {


        // -- paths
        String findAllForUserPath = mkPath(BASE_URL, ":userId");
        String saveForUserPath = mkPath(BASE_URL, "save", ":userId");
        String deleteForUserPath = mkPath(BASE_URL, "clear", ":userId");


        // -- routes
        ListRoute<UserPreference> findAllForUserRoute = (request, response) -> {
            String userId = request.params("userId");
            return userPreferenceService.getPreferences(userId);
        };


        DatumRoute<Boolean> saveForUserRoute = (request, response) -> {
            String userId = request.params("userId");
            List<UserPreference> preferences = readBody(request, List.class);
            return userPreferenceService.savePreferences(userId, preferences);
        };


        DatumRoute<Boolean> deleteForUserRoute = (request, response) -> {
            String userId = request.params("userId");
            return userPreferenceService.clearPreferences(userId);
        };


        // --- register
        getForList(findAllForUserPath, findAllForUserRoute);
        postForDatum(saveForUserPath, saveForUserRoute);
        getForDatum(deleteForUserPath, deleteForUserRoute);
    }
}
