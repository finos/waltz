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

import template from "./outstanding-actions-notification-panel.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {entity} from "../../../common/services/enums/entity";


const bindings = {

};


const initialState = {
    summaries: []
};


const stateNameByKind = {
    SURVEY_INSTANCE: "main.survey.instance.user",
    ATTESTATION: "main.attestation.instance.user"
};


const labelsByKind = {
    SURVEY_INSTANCE: "Surveys",
    ATTESTATION: "Attestations"
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.NotificationStore.findAll,
                [],
                {
                    force: true
                })
            .then(r => {
                vm.summaries = _
                    .chain(r.data)
                    .map(d => {
                        const attrs = {
                            label: labelsByKind[d.kind],  // don't use the ones from `entity` as need plurals
                            icon: entity[d.kind].icon,
                            uiStateName: stateNameByKind[d.kind] || "main.home",
                            count: d.count
                        }
                        return Object.assign({}, d, attrs)
                    })
                    .value();
            });
    }
}

controller.$inject = [
    "ServiceBroker"
];


export default {
    id: "waltzOutstandingActionsNotificationPanel",
    component: {
        controller,
        bindings,
        template
    }
};
