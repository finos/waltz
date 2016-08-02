
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
let preferences = {};

function service($q, userPreferenceStore, userService) {

    const loadPreferences = (force = false) => {
        if (force || (preferencePromise == null)) {
            preferencePromise = userService
                .whoami()
                .then(user => userPreferenceStore.findAll(user.userName))
                .then(preferences => _.keyBy(preferences, 'key'))
                .then(p => preferences = p);
        }
        return preferencePromise;
    };


    const savePreference = (key, value) => {
        if(!preferencePromise || value === undefined || value === null) return;

        const deferred = $q.defer();

        if(!preferences[key] || stringToBoolean(preferences[key].value) != value) {
            userService
                .whoami()
                .then(user => {
                    const preference = {
                        userName: user.userName,
                        key,
                        value
                    };

                    return userPreferenceStore.save(preference)
                        .then(preferences => _.keyBy(preferences, 'key'))
                        .then(p => preferences = p);

                })
                .then(p => deferred.resolve(preferences));
        }
        else {
            deferred.resolve(preferences)
        }
    };


    loadPreferences();

    return {
        loadPreferences,
        savePreference
    };
}


service.$inject = [
    '$q',
    'UserPreferenceStore',
    'UserService',
];


export default service;
