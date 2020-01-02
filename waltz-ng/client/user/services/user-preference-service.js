
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

import _ from "lodash";


export const lastViewedMeasurableCategoryKey = "main.measurable-category.list.lastCategory";


let preferencePromise = null;

function service($q, userPreferenceStore) {

    const loadPreferences = (force = false) => {
        if (force || (preferencePromise == null)) {
            preferencePromise = userPreferenceStore.findAllForUser();
        }
        return preferencePromise
            .then(preferences => _.keyBy(preferences, "key"));
    };


    /**
     * ( key , value) -> { prefs... }
     * @param key
     * @param value
     * @returns {*}
     */
    const savePreference = (key, value) => {
        if(!preferencePromise || value === undefined || value === null) return;

        return loadPreferences()
            .then(preferencesByKey => {
                const isNewPreference = !preferencesByKey[key];
                const hasPreferenceChanged = !isNewPreference && preferencesByKey[key].value !== value;

                if(isNewPreference || hasPreferenceChanged) {
                    const preference = {
                        key,
                        value
                    };

                    return userPreferenceStore
                        .saveForUser(preference)
                        .then(updatedPrefs => preferencePromise = $q.defer().resolve(updatedPrefs));
                }
            })
            .then(() => loadPreferences());
    };


    loadPreferences();

    return {
        loadPreferences,
        savePreference
    };
}


service.$inject = [
    "$q",
    "UserPreferenceStore"
];


export default service;
