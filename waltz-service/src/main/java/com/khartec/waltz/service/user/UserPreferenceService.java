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

package com.khartec.waltz.service.user;

import com.khartec.waltz.data.user.UserPreferenceDao;
import com.khartec.waltz.model.user.UserPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class UserPreferenceService {

    private static final Logger LOG = LoggerFactory.getLogger(UserPreferenceService.class);

    private final UserPreferenceDao userPreferenceDao;


    @Autowired
    public UserPreferenceService(UserPreferenceDao userPreferenceDao) {
        checkNotNull(userPreferenceDao, "userPreferenceDao cannot be null");

        this.userPreferenceDao = userPreferenceDao;
    }


    public List<UserPreference> getPreferences(String userName) {
        checkNotEmpty(userName, "userName cannot be empty");
        return userPreferenceDao.getPreferencesForUser(userName);
    }


    public List<UserPreference> savePreferences(String userName, List<UserPreference> preferences) {
        checkNotEmpty(userName, "userName cannot be empty");
        checkNotNull(preferences, "preferences cannot be null");

        userPreferenceDao.savePreferencesForUser(userName, preferences);
        return getPreferences(userName);
    }


    public List<UserPreference> savePreference(String userName, UserPreference preference) {
        checkNotEmpty(userName, "userName cannot be empty");
        checkNotNull(preference, "preference cannot be null");

        userPreferenceDao.savePreference(userName, preference);
        return getPreferences(userName);
    }


    public boolean clearPreferences(String userName) {
        checkNotEmpty(userName, "userName cannot be empty");

        userPreferenceDao.clearPreferencesForUser(userName);
        return true;
    }

}
