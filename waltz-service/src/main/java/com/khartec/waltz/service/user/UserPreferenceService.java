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
