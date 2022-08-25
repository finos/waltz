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
import namedSettings from "../named-settings";
import {CORE_API} from "../../common/services/core-api-utils";


function service(serviceBroker) {

    const findAll = (force = false) => {
        return serviceBroker
            .loadAppData(
                CORE_API.SettingsStore.findAll,
                [],
                { force })
            .then(r => r.data);
    };


    const findOrDefault = (name = "", dflt = null) => {
        return findAll()
            .then(settings => {
                const found = _.find(settings, { name });
                return found ? found.value : dflt;
            });
    };

    const findOrDie = (name, msg) => {
        const defaultMsg = `Cannot find setting: [${name}] in settings table. Please see docs/features/configuration/settings.md for more details about this table.`;

        return findOrDefault(name, null)
            .then(r => {
                if (_.isNull(r)) {
                    throw (msg || defaultMsg);
                } else {
                    return r;
                }
            })
    }


    const isDevModeEnabled = () => {
        return findOrDefault(namedSettings.devExtEnabled, false)
            .then(devModeEnabled => "true" === devModeEnabled );
    };


    return {
        findAll,
        findOrDefault,
        findOrDie,
        isDevModeEnabled
    };
}

service.$inject = [
    "ServiceBroker"
];


export default service;
