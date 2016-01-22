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

import _ from 'lodash';

import * as fields from './formly/fields';


function setupTagAutoComplete(appStore) {
    appStore.findAllTags().then(tags => {
        fields.tagsField.templateOptions.autoCompleteLoader = (query) =>
            _.filter(tags, t => t.toLowerCase().indexOf(query.toLowerCase()) > -1);
    });
}


function setupDropDowns(orgUnits, displayNameService) {
    fields.orgUnitField.templateOptions.options = _.map(orgUnits, (u) => ({ name: u.name, code: u.id}));
    fields.typeField.templateOptions.options = displayNameService.toOptions('applicationKind');
    fields.lifecyclePhaseField.templateOptions.options = displayNameService.toOptions('lifecyclePhase');
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
            { className: 'col-xs-4', fieldGroup: [ fields.overallRatingField, fields.typeField, fields.lifecyclePhaseField ]}
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


function controller(appView, orgUnits, displayNameService, appStore, $state) {

    setupDropDowns(orgUnits, displayNameService);
    setupTagAutoComplete(appStore);

    const formModel = {
        app: appView.app,
        tags: appView.tags,
        aliases: appView.aliases
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


    function onSubmit() {
        const onSuccess = () => {
            $state.go('main.app-view', {id: appView.app.id});
        };

        const onFailure = (result) => {
            console.log('Error:', result);
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

        appStore.update(appView.app.id, action)
            .then(onSuccess, onFailure);

    }

    const ctrl = this;
    ctrl.application = appView.app;
    ctrl.formModel = formModel;
    ctrl.fieldLayout = fieldLayout;
    ctrl.onSubmit = onSubmit;

}

controller.$inject = [
    'appView',
    'orgUnits',
    'WaltzDisplayNameService',
    'ApplicationStore',
    '$state'
];


export default {
    template: require('./app-edit.html'),
    controller,
    controllerAs: 'ctrl'
};
