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

import template from "./change-initiative-external-id-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";


function controller($state,
                    $stateParams,
                    serviceBroker) {

    const goToCI = ci => $state.go(
        "main.change-initiative.view",
        { id: ci.id });

    serviceBroker
        .loadViewData(CORE_API.ChangeInitiativeStore.findByExternalId, [ $stateParams.externalId ])
        .then(r => r.data)
        .then(xs => {
            if (xs.length === 1) {
                goToCI(xs[0]);
            }
        });


    // if single app for asset code, navigate to the app now

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

