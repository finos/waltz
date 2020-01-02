
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
import {
    nameField,
    assetCodeField,
    descriptionField,
    orgUnitField,
    parentAssetCodeField,
    typeField,
    lifecyclePhaseField,
    overallRatingField,
    businessCriticalityField
} from "../../formly/fields";


import template from "./app-registration.html";

// ----- CONTROLLER -----

const controller = function(applicationStore,
                            notification,
                            orgUnitStore) {
    const vm = this;

    let allOrgUnits = [];

    orgUnitStore
        .findAll()
        .then(units => {
            allOrgUnits = units;
            orgUnitField.templateOptions.options = _.map(units, (u) => ({ name: u.name, code: u.id}));
        });


    const model = {
        lifecyclePhase: 'PRODUCTION',
        applicationKind: 'IN_HOUSE'
    };


    const fields = [
        {
            className: 'row',
            fieldGroup: [
                { className: 'col-xs-8', fieldGroup: [nameField, orgUnitField] },
                { className: 'col-xs-4', fieldGroup: [assetCodeField, parentAssetCodeField] }
            ]
        }, {
            className: 'row',
            fieldGroup: [
                { className: 'col-xs-8', fieldGroup: [descriptionField] },
                { className: 'col-xs-4', fieldGroup: [overallRatingField, typeField, lifecyclePhaseField, businessCriticalityField] }
            ]
        }
    ];


    const registrations = [];


    function onSubmit() {
        const onSuccess = (result) => {
            notification.success('New Application registered');
            const { registered, message, id, originalRequest } = result;
            const { name, organisationalUnitId, applicationKind, lifecyclePhase } = originalRequest;

            registrations.push({
                success: registered,
                message,
                app: {
                    id,
                    name,
                    applicationKind,
                    lifecyclePhase,
                    organisationalUnit: _.find(allOrgUnits, {id: organisationalUnitId})
                }
            });
        };

        const onFailure = (result) => {
            notification.success('Failed to register application, see below');
            registrations.push({
                success: false,
                message: result.data.message,
                app: {
                    name: result.config.data.name
                }
            });
        };

        const newApp = Object.assign(
            {},
            model,
            {
                aliases: _.map(model.aliases, 'text'),
                tags: _.map(model.tags, 'text')
            });


        applicationStore
            .registerNewApp(newApp)
            .then(onSuccess, onFailure);
    }

    // -- INIT --
    vm.$onInit = () => {
        nameField.model = model;
        assetCodeField.model = model;
        descriptionField.model = model;
        orgUnitField.model = model;
        orgUnitField.templateOptions.ouRef = null;
        parentAssetCodeField.model = model;
        typeField.model = model;
        lifecyclePhaseField.model = model;
        overallRatingField.model = model;
        businessCriticalityField.model = model;

        vm.fields = fields;
        vm.model = model;
        vm.onSubmit = onSubmit;
        vm.registrations = registrations;
    };

};

controller.$inject = [
    'ApplicationStore',
    'Notification',
    'OrgUnitStore'
];

export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
