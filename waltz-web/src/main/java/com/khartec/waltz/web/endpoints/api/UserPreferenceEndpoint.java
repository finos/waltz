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
        String findAllForUserPath = mkPath(BASE_URL, ":userName");
        String saveAllForUserPath = mkPath(BASE_URL, ":userName", "save-all");
        String saveForUserPath = mkPath(BASE_URL, ":userName", "save");
        String deleteForUserPath = mkPath(BASE_URL, ":userName", "clear");


        // -- routes
        ListRoute<UserPreference> findAllForUserRoute = (request, response) -> {
            String userName = request.params("userName");
            return userPreferenceService.getPreferences(userName);
        };


        ListRoute<UserPreference> saveAllForUserRoute = (request, response) -> {
            String userName = request.params("userName");
            List<UserPreference> preferences = readBody(request, List.class);
            return userPreferenceService.savePreferences(userName, preferences);
        };


        ListRoute<UserPreference> saveForUserRoute = (request, response) -> {
            UserPreference preference = readBody(request, UserPreference.class);
            return userPreferenceService.savePreference(preference);
        };


        DatumRoute<Boolean> deleteForUserRoute = (request, response) -> {
            String userName = request.params("userName");
            return userPreferenceService.clearPreferences(userName);
        };


        // --- register
        getForList(findAllForUserPath, findAllForUserRoute);
        postForList(saveAllForUserPath, saveAllForUserRoute);
        postForList(saveForUserPath, saveForUserRoute);
        deleteForDatum(deleteForUserPath, deleteForUserRoute);
    }
}
