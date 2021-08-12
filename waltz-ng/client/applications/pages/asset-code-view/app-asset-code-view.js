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

import template from "./app-asset-code-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";


function controller($state,
                    $stateParams,
                    serviceBroker) {

    const goToApp = app => $state.go("main.app.view", { id: app.id }, { location: true });

    serviceBroker
        .loadViewData(
            CORE_API.ApplicationStore.findByAssetCode,
            [$stateParams.assetCode])
        .then(r => {
            const app = _.first(r.data);
            if (app) goToApp(app);
        });
}


controller.$inject = [
    "$state",
    "$stateParams",
    "ServiceBroker"
];


export default  {
    template,
    controller,
    controllerAs: "ctrl"
};

