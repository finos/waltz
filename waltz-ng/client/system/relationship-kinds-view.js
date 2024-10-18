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
import template from "./relationship-kinds-view.html";
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import * as _ from "lodash";
import {displayError} from "../common/error-utils";
import {entity} from "../common/services/enums/entity";
import toasts from "../svelte-stores/toast-store";
import BulkRelationshipUpload from "./svelte/bulk-relationships/BulkRelationshipUpload.svelte";

const initialState = {
    relationshipKinds: [],
    measurableCategories: [],
    form: {
        name: null,
        reverseName: null,
        code: null,
        description: null,
        kindA: null,
        kindB: null,
        categoryA: null,
        categoryB: null,
        position: null,
        isReadonly: false,
    },
    visibility: {
        create: false,
        update: false,
        bulk: false
    },
    BulkRelationshipUpload
};


const columnDefs = [
    {
        field: "name",
        displayName: "Name",
        width: "25%",
    },{
        field: "reverseName",
        displayName: "Reverse Name",
        width: "25%",
    }, {
        field: "kindA",
        displayName: "From",
        width: "25%",
        cellTemplate: `
            <waltz-icon name="{{COL_FIELD | toIconName: 'entity' }}">
            </waltz-icon>
            <span ng-bind="COL_FIELD | toDisplayName: 'entity'"></span>`
    },{
        field: "kindB",
        displayName: "To",
        width: "25%",
        cellTemplate: `
            <waltz-icon name="{{COL_FIELD | toIconName: 'entity' }}">
            </waltz-icon>
            <span ng-bind="COL_FIELD | toDisplayName: 'entity'"></span>`
    }
];


function controller(serviceBroker, $q) {

    const vm = initialiseData(this, initialState);

    function loadData() {

        const relationshipKindsPromise = serviceBroker
            .loadAppData(CORE_API.RelationshipKindStore.findAll, [], {force: true})
            .then(r => r.data);

        const measurableCategoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => r.data);

        return $q.all([relationshipKindsPromise, measurableCategoriesPromise])
            .then(([relationshipKinds, measurableCategories]) => {

                const categoriesById = _.keyBy(measurableCategories, c => c.id);

                vm.measurableCategories = measurableCategories;

                vm.relationshipKinds = _.map(relationshipKinds,
                                             r => Object.assign({},
                                                                r, {categoryA: _.get(categoriesById, r.categoryA, null),
                                                                    categoryB: _.get(categoriesById, r.categoryB, null)}));
            });
    }

    vm.$onInit = () => {
        vm.columnDefs = columnDefs;
        vm.entityKinds = entity;
        loadData();
    };

    vm.onSelectRelationshipKind = (relationshipKind) => {

        vm.onDismiss();

        if (vm.selectedRelationshipKind === relationshipKind) {
            vm.selectedRelationshipKind = null;
        } else {
            vm.selectedRelationshipKind = relationshipKind;
        }
    };

    vm.editRelationshipKind = () => {

        vm.form.name = vm.selectedRelationshipKind.name;
        vm.form.reverseName = vm.selectedRelationshipKind.reverseName;
        vm.form.description = vm.selectedRelationshipKind.description;
        vm.form.position = vm.selectedRelationshipKind.position;

        vm.visibility.update = true;
        vm.visibility.bulk = false;
    };

    vm.createRelationshipKind = () => {
        vm.resetForm();
        vm.visibility.create = true;
        vm.visibility.update = false;
        vm.visibility.bulk = false;
        vm.selectedRelationshipKind = null;
    };

    vm.resetForm = () => {
        vm.form.name = null;
        vm.form.reverseName = null;
        vm.form.description = null;
        vm.form.code = null;
        vm.form.kindA = null;
        vm.form.kindB = null;
        vm.form.categoryA = null;
        vm.form.categoryB = null;
        vm.form.position = null;
        vm.form.isReadonly = false;
    };

    vm.isDisabled = () => {
        return _.isNull(vm.form.name)
            || vm.form.name.length === 0
            || _.isNull(vm.form.reverseName)
            || vm.form.reverseName.length === 0
            || _.isNull(vm.form.kindA)
            || _.isNull(vm.form.kindB)
            || _.isNull(vm.form.position);
    };

    vm.isValid = () => {
        return _.isUndefined(
            _.find(vm.relationshipKinds, {
                "kindA": vm.form.kindA,
                "kindB": vm.form.kindB,
                "code": _.toUpper(_.replace(vm.form.name, /\s+/g, "_"))
            }));
    };

    vm.onDismiss = () => {
        vm.visibility.create = false;
        vm.visibility.update = false;
        vm.visibility.bulk = false;
        vm.resetForm();
    };


    vm.onSaveUpdate = () => {

        const submission = {
            name: vm.form.name,
            reverseName: vm.form.reverseName,
            description: vm.form.description,
            position: vm.form.position,
        };

        const selectedRelationshipKindId = vm.selectedRelationshipKind.id;

        serviceBroker.execute(CORE_API.RelationshipKindStore.update, [ selectedRelationshipKindId, submission ])
            .then(() => {
                toasts.success("Updated relationship");
                vm.onDismiss();
            })
            .then(() => loadData())
            .then(() => vm.selectedRelationshipKind = _.find(vm.relationshipKinds, ["id", selectedRelationshipKindId]))
            .catch(e => displayError("Could not update relationship", e))
    };


    vm.onSubmit = () => {

        vm.form.code = _.toUpper(_.replace(vm.form.name, /\s+/g, "_"));

        return serviceBroker
            .execute(CORE_API.RelationshipKindStore.create, [ vm.form ])
            .then(() => {
                toasts.success("Relationship saved");
                loadData();
                vm.onDismiss();
            }).catch(e => displayError("Could not create relationship", e));
    };

    vm.removeRelationshipKind = () => {
        if (confirm("Are you sure you want to delete this relationship kind?")) {
            serviceBroker.execute(
                CORE_API.RelationshipKindStore.remove,
                [vm.selectedRelationshipKind.id])
                .then(() => {
                    toasts.warning("Relationship removed");
                    loadData();
                    vm.selectedRelationshipKind = null;
                })
                .catch(e => {
                    displayError("Relationship kind could not be removed", e);
                });
        }
    }

    vm.bulkUploadRelationships = () => {
        vm.visibility.bulk = true;
        vm.visibility.update = false;
    }
}

controller.$inject = [
    "ServiceBroker",
    "$q"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
};