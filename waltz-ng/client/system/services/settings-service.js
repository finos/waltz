/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import namedSettings from "../named-settings";
import {CORE_API} from "../../common/services/core-api-utils";


let settingsPromise = null;

function service(serviceBroker) {

    const findAll = (force = false) => {
        return serviceBroker
            .loadAppData(
                CORE_API.SettingsStore.findAll,
                [],
                { force })
            .then(r => r.data)
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
    'ServiceBroker'
];


export default service;
