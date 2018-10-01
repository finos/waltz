
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
