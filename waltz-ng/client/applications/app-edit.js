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
import * as fields from "./formly/fields";


function setupTagAutoComplete(appStore) {
    appStore.findAllTags().then(tags => {
        fields.tagsField.templateOptions.autoCompleteLoader = (query) =>
            _.filter(tags, t => t.toLowerCase().indexOf(query.toLowerCase()) > -1);
    });
}


function setupDropDowns(orgUnits) {
    fields.orgUnitField.templateOptions.options = _.map(orgUnits, (u) => ({ name: u.name, code: u.id}));
}


const fieldLayout = [
    {
        className: 'row',
        fieldGroup: [
            {className: 'col-xs-8', fieldGroup: [ fields.nameField, fields.aliasesField, fields.tagsField ]},
            {className: 'col-xs-4', fieldGroup: [ fields.assetCodeField, fields.parentAssetCodeField, fields.orgUnitField ]}
        ]
    }, {
        className: 'row',
        fieldGroup: [
            { className: 'col-xs-8', fieldGroup: [ fields.descriptionField ]},
            { className: 'col-xs-4', fieldGroup: [ fields.overallRatingField, fields.typeField, fields.lifecyclePhaseField, fields.businessCriticalityField ]}
        ]
    }
];


function fieldValuesRender(field) {
    const original = field.originalModel[field.key];
    const current = field.model[field.key];

    if (field.type === 'tags-input') {
        return {
            original: JSON.stringify(original),
            current: JSON.stringify(_.map(current, 'text'))
        };
    } else {
        return { original, current };
    }
}


function fieldDiff(field) {
    const values = fieldValuesRender(field);
    return {
        key: field.key,
        name: field.templateOptions.label,
        dirty: field.formControl.$dirty,
        ...values
    };
}


function controller(app,
                    tags,
                    aliases,
                    orgUnits,
                    appStore,
                    notification,
                    $state) {

    setupDropDowns(orgUnits);
    setupTagAutoComplete(appStore);

    const formModel = {
        app,
        tags,
        aliases
    };

    fields.nameField.model = formModel.app;
    fields.descriptionField.model = formModel.app;
    fields.assetCodeField.model = formModel.app;
    fields.parentAssetCodeField.model = formModel.app;
    fields.orgUnitField.model = formModel.app;
    fields.typeField.model = formModel.app;
    fields.lifecyclePhaseField.model = formModel.app;
    fields.aliasesField.model = formModel;
    fields.tagsField.model = formModel;
    fields.overallRatingField.model = formModel.app;
    fields.businessCriticalityField.model = formModel.app;


    function onSubmit() {
        const onSuccess = () => {
            notification.success('Application updated');
            $state.go('main.app.view', { id: app.id });
        };

        const onFailure = (result) => {
            console.error(result);
            notification.error('Error: '+ result.statusText);
        };

        const changes = _.chain(fields)
            .map(fieldDiff)
            .filter(fd => fd.dirty)
            .value();

        const action = {
            ...formModel,
            tags: _.map(formModel.tags, 'text'),   // override model
            aliases: _.map(formModel.aliases, 'text'),  // override model
            changes
        };

        appStore.update(app.id, action)
            .then(onSuccess, onFailure);

    }

    const ctrl = this;
    ctrl.application = app;
    ctrl.formModel = formModel;
    ctrl.fieldLayout = fieldLayout;
    ctrl.onSubmit = onSubmit;

}

controller.$inject = [
    'app',
    'tags',
    'aliases',
    'orgUnits',
    'ApplicationStore',
    'Notification',
    '$state'
];


export default {
    template: require('./app-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
