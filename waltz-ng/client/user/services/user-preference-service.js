
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from "lodash";
import {stringToBoolean} from "../../common";

let preferencePromise = null;

function service($q, userPreferenceStore) {

    const loadPreferences = (force = false) => {
        if (force || (preferencePromise == null)) {
            preferencePromise = userPreferenceStore.findAllForUser();
        }
        return preferencePromise
            .then(preferences => _.keyBy(preferences, 'key'));
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
                const hasPreferenceChanged = !isNewPreference && stringToBoolean(preferencesByKey[key].value) != value;

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
    '$q',
    'UserPreferenceStore'
];


export default service;
