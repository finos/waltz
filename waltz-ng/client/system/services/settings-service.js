/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import namedSettings from "../named-settings";


let settingsPromise = null;

function service(settingsStore) {

    const findAll = (force = false) => {
        if (force || settingsPromise == null) {
            settingsPromise = settingsStore.findAll();
        }
        return settingsPromise;
    };


    const findOrDefault = (name = "", dflt = null) => {
        return findAll()
            .then(settings => {
                const found = _.find(settings, { name });
                return found ? found.value : dflt;
        });
    };


    const isDevModeEnabled = () => {
        return findOrDefault(namedSettings.devExtEnabled, false)
            .then(devModeEnabled => 'true' === devModeEnabled );
    };


    return {
        findAll,
        findOrDefault,
        isDevModeEnabled
    };
}

service.$inject = [
    'SettingsStore'
];


export default service;
