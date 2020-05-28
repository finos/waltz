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
import {initialiseData} from "../../../common";
import {sameRef} from "../../../common/entity-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {entity} from "../../../common/services/enums/entity";
import {getEnumName} from "../../../common/services/enums";
import {sanitizeRelationships} from "../../measurable-relationship-utils";

import template from "./related-measurables-panel.html";
import {displayError} from "../../../common/error-utils";


/**
 * @name waltz-related-measurables-panel
 *
 * @description
 * This component displays entities related to a given measurable.
 * If the user has 'CAPABILITY_EDITOR' role then edit facilities
 * are provided.
 */


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    categories: [],
    columnDefs: [],
    measurables: [],
    relationships: [],
    selectedCategory: null,
    selectedRow: null,
    gridData: [],
    form: {
        relationshipKind: null,
        description: null},
    visibility: {
        editor: false,
        detailMode: "table", // table | tree,
        detailModeChanger: false,
        createEditor: false,
        updateEditor: false,
        createRelationshipKind: false
    }
};


const DEFAULT_SELECTION_FILTER_FN = () => true;


function mkGridData(selfRef,
                    relationships = [],
                    measurables = [],
                    categories = [],
                    appGroups = [],
                    rowFilterFn = () => true) {

    const measurablesById = _.keyBy(measurables, "id");
    const categoriesById = _.keyBy(categories, "id");
    const appGroupsById = _.keyBy(appGroups, "id");

    const toGenericCell = r => {
        return Object.assign({}, r, { type: getEnumName(entity, r.kind) });
    };

    const toMeasurableCell = r => {
        const c = categoriesById[measurablesById[r.id].categoryId];
        return Object.assign({}, r, { type: c.name });
    };

    const toAppGroupCell = r => {
        const c = appGroupsById[r.id];
        return Object.assign({}, r, { name: c !== null ? c.name : "", type: "App Group" });
    };

    const mkCell = (kind, side) => {
        switch (kind) {
            case "MEASURABLE":
                return toMeasurableCell(side);
            case "APP_GROUP":
                return toAppGroupCell(side);
            default:
                return toGenericCell(side);
        }
    };


    return _
        .chain(relationships)
        .filter(rowFilterFn)
        .map(r => {
            const outbound = sameRef(r.a, selfRef, { skipChecks: true });
            const a = mkCell(r.a.kind, r.a);
            const b = mkCell(r.b.kind, r.b);

            return {
                outbound,
                a,
                b,
                relationship: r
            };
        })
        .filter(r => r !== null)
        .sortBy(["a.name", "b.name"])
        .value()
}


function controller($q, $timeout, serviceBroker, notification) {
    const vm = this;

    const calcGridData = () => {
        return mkGridData(
            vm.parentEntityRef,
            vm.relationships,
            vm.measurables,
            vm.categories,
            vm.appGroups,
            vm.selectionFilterFn);
    };


    // -- INTERACT --

    vm.onChangeDetailMode = (mode) => {
        vm.visibility.detailMode = mode;
    };

    vm.refresh = ()=> {
        loadAll()
            .then(() => {
                if (vm.selectedRow) {
                    vm.selectedRow = _.find(vm.gridData || [], row => {
                        const sameSource = sameRef(vm.selectedRow.a, row.a, { skipChecks: true });
                        const sameTarget = sameRef(vm.selectedRow.b, row.b, { skipChecks: true });
                        return sameSource && sameTarget;
                    });

                    vm.filteredGridData = _.filter(vm.gridData, d => d.a.id === vm.selectedRow.a.id && d.b.id === vm.selectedRow.b.id);
                    loadAllowedRelationshipKinds(vm.filteredGridData)
                }
            });
    };

    vm.selectCategory = (c) => $timeout(() => {
        vm.selectedCategory = c;
        if (_.get(c, "ref.kind") === "MEASURABLE_CATEGORY") {
            vm.visibility.detailModeChanger = true;
        } else {
            vm.visibility.detailMode = "table";
            vm.visibility.detailModeChanger = false;
        }
        vm.selectedRow = null;
        vm.selectionFilterFn = c.relationshipFilter;
        vm.gridData = calcGridData();
        vm.cancelEditor();
    });

    vm.clearCategory = () => $timeout(() => {
        vm.selectedCategory = null;
        vm.selectedRow = null;
        vm.selectionFilterFn = DEFAULT_SELECTION_FILTER_FN;
        vm.gridData = calcGridData();
        vm.visibility.detailMode = "table";
        vm.visibility.detailModeChanger = false;
    });

    vm.selectRow = (r) => {
        if (r === vm.selectedRow) {
            vm.clearRowSelection(); // toggle
        } else {
            vm.selectedRow = r;
            vm.filteredGridData = _.filter(vm.gridData, d => d.a.id === vm.selectedRow.a.id && d.b.id === vm.selectedRow.b.id);
            loadAllowedRelationshipKinds(vm.filteredGridData);
        }
        vm.cancelEditor();
    };

    vm.clearRowSelection = () => {
        vm.selectedRow = null;
    };

    vm.selectRelationship = (r) => {
        if (r === vm.selectedRelationship) {
            vm.selectedRelationship = null; // toggle
        } else {
            vm.selectedRelationship = r;
        }
        vm.cancelEditor();
    };

    vm.removeRelationship = (rel) => {
        if (confirm("Are you sure you want to delete this relationship ?")) {
            vm.onRemove(rel)
                .then(() => {
                    notification.warning("Relationship removed");
                    vm.clearRowSelection();
                    loadRelationships();
                })
                .catch(e => {
                    displayError(notification, "Relationship could not be removed", e)
                });
        }
    };

    vm.beginNewRelationship = () => {
        vm.visibility.editor = true;
        vm.visibility.createEditor = true;
        vm.visibility.updateEditor = false;
    };

    vm.cancelEditor = () => {
        vm.visibility.editor = false;
        vm.visibility.createEditor = false;
        vm.visibility.updateEditor = false;
        vm.visibility.createRelationshipKind = false;
    };

    vm.updateExistingRelationship = () => {
        vm.visibility.editor = true;
        vm.visibility.createEditor = false;
        vm.visibility.updateEditor = true;
        vm.visibility.createRelationshipKind = false;
    };


    // -- API --

    const loadRelationships = () => {
        return serviceBroker
            .loadViewData(
                CORE_API.MeasurableRelationshipStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => {
                vm.relationships = sanitizeRelationships(r.data, vm.measurables, vm.categories);
                vm.gridData = calcGridData();
            });
    };

    const loadAllowedRelationshipKinds = (data) => {

        const existingKinds = _.map(data, d => d.relationship.relationship);

        return serviceBroker.loadViewData(
            CORE_API.RelationshipKindStore.findRelationshipKindsBetweenEntities,
            [vm.selectedRow.a, vm.selectedRow.b])
            .then(r => vm.relationshipKinds = _.reject(r.data, d => d.isReadonly || _.includes(existingKinds, d.code)));
    };

    const loadAll = () => {
        const promises = [
            serviceBroker.loadAppData(CORE_API.MeasurableCategoryStore.findAll).then(r => r.data),
            serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll).then(r => r.data),
            serviceBroker.loadAppData(CORE_API.AppGroupStore.findPublicGroups).then(r => r.data),
            serviceBroker.loadAppData(CORE_API.AppGroupStore.findPrivateGroups).then(r => r.data)
        ];
        return $q
            .all(promises)
            .then(([categories, measurables, publicAppGroups, privateAppGroups]) => {
                vm.categories = categories;
                vm.measurables = measurables;
                vm.appGroups = _.union(publicAppGroups, privateAppGroups);
            })
            .then(loadRelationships)

    };

    vm.onRemove = (rel) => {
        return serviceBroker
            .execute(CORE_API.MeasurableRelationshipStore.remove, [rel])
            .then(() => vm.cancelEditor())
    };

    vm.onAddRelationshipKind = () => {
        vm.visibility.editor = false;
        vm.visibility.createRelationshipKind = true;
    };

    vm.onSubmit = () => {

        const submission = {
            a: vm.selectedRow.a,
            b: vm.selectedRow.b,
            relationshipKind: vm.form.relationshipKind,
            description: vm.form.description
        };

        return serviceBroker
            .execute(CORE_API.MeasurableRelationshipStore.create, [submission])
            .then(() => {
                vm.cancelEditor();
                vm.clearRowSelection();
                notification.success("Relationship saved");
                vm.refresh();
            }).catch(e => {
                const message = "Could not create relationship: " + e.message;
                notification.error(message);
            });
    };


    // -- BOOT --
    vm.$onInit = () => {
        initialiseData(vm, initialState);
        loadAll();
    };
}


controller.$inject = [
    "$q",
    "$timeout",
    "ServiceBroker",
    "Notification"
];


const component = {
    template,
    bindings,
    controller
};


export default component;
