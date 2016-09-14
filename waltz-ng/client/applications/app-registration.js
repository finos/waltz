
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
import {
    nameField,
    aliasesField,
    assetCodeField,
    descriptionField,
    orgUnitField,
    parentAssetCodeField,
    tagsField,
    typeField,
    lifecyclePhaseField,
    overallRatingField,
    businessCriticalityField
} from "./formly/fields";


// ----- CONTROLLER -----

const controller = function(applicationStore,
                            notification,
                            orgUnitStore,
                            waltzDisplayNameService ) {

    let allOrgUnits = [];

    orgUnitStore
        .findAll()
        .then(units => {
            allOrgUnits = units;
            orgUnitField.templateOptions.options = _.map(units, (u) => ({ name: u.name, code: u.id}));
        });


    typeField.templateOptions.options = waltzDisplayNameService.toOptions('applicationKind');
    lifecyclePhaseField.templateOptions.options = waltzDisplayNameService.toOptions('lifecyclePhase');


    const model = {
        lifecyclePhase: 'PRODUCTION',
        kind: 'IN_HOUSE'
    };


    const fields = [
        {
            className: 'row',
            fieldGroup: [
                { className: 'col-xs-8', fieldGroup: [nameField, aliasesField, tagsField] },
                { className: 'col-xs-4', fieldGroup: [assetCodeField, parentAssetCodeField, orgUnitField] }
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
            const { name, organisationalUnitId, kind, lifecyclePhase } = originalRequest;

            registrations.push({
                success: registered,
                message,
                app: {
                    id,
                    name,
                    kind,
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

        const newApp = {
            ...model,
            aliases: _.map(model.aliases, 'text'),
            tags: _.map(model.tags, 'text')
        };


        applicationStore
            .registerNewApp(newApp)
            .then(onSuccess, onFailure);
    }

    // -- exposed

    this.fields = fields;
    this.model = model;
    this.onSubmit = onSubmit;
    this.registrations = registrations;
};

controller.$inject = [
    'ApplicationStore',
    'Notification',
    'OrgUnitStore',
    'WaltzDisplayNameService',
];

export default {
    template: require('./app-registration.html'),
    controller,
    controllerAs: 'ctrl'
};
