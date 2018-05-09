/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../../common";
import {downloadTextFile} from "../../../common/file-utils";
import {sameRef} from "../../../common/entity-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {entity} from "../../../common/services/enums/entity";
import {relationshipKind} from "../../../common/services/enums/relationship-kind";
import {getEnumName} from "../../../common/services/enums";
import {sanitizeRelationships} from "../../measurable-relationship-utils";

import template from "./related-measurables-panel.html";


/**
 * @name waltz-related-measurables-panel
 *
 * @description
 * This component displays entities related to a given measurable.
 * If the user has 'CAPABILITY_EDITOR' role then edit facilities
 * are provided.
 */


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    categories: [],
    measurables: [],
    relationships: [],
    visibility: {
        editor: false,
        createEditor: false,
        updateEditor: false,
    }
};


const DEFAULT_SELECTION_FILTER_FN = (m) => true;


function mkGridData(ref,
                    relationships = [],
                    measurables = [],
                    categories = [],
                    rowFilterFn = (x) => true)
{
    const measurablesById = _.keyBy(measurables, 'id');
    const categoriesById = _.keyBy(categories, 'id');

    const toGenericCell = r => {
        return Object.assign({}, r, { type: getEnumName(entity, r.kind) });
    };

    const toMeasurableCell = r => {
        const c = categoriesById[measurablesById[r.id].categoryId];
        return Object.assign({}, r, { type: c.name });
    };

    return _
        .chain(relationships)
        .filter(rowFilterFn)
        .map(r => {
            const outbound = sameRef(r.a, ref);
            const a = r.a.kind === 'MEASURABLE'
                ? toMeasurableCell(r.a)
                : toGenericCell(r.a);

            const b = r.b.kind === 'MEASURABLE'
                ? toMeasurableCell(r.b)
                : toGenericCell(r.b);

            return {
                outbound,
                a,
                b,
                relationship: r
            };
        })
        .filter(r => r !== null)
        .sortBy(['a.name', 'b.name'])
        .value()
}


function mkExportData(relationships = [], categories = [], measurables = []) {
    const categoriesById = _.keyBy(categories, 'id');
    const categoriesByMeasurableId = _.chain(measurables)
        .map(m => ({
            measurableId: m.id,
            category: categoriesById[m.categoryId]
        }))
        .keyBy('measurableId')
        .value();

    const getType = (id, kind) => {
        return kind === 'MEASURABLE'
            ? categoriesByMeasurableId[id].category.name
            : getEnumName(entity, kind);
    };

    const columnNames = [[
        "From",
        "From type",
        "To",
        "To type",
        "Relationship",
        "Description",
        "Last Updated At",
        "Last Updated By"
    ]];

    const exportData = _.map(relationships, r => [
        r.a.name,
        getType(r.a.id, r.a.kind),
        r.b.name,
        getType(r.b.id, r.b.kind),
        getEnumName(relationshipKind, r.relationship),
        r.description,
        r.lastUpdatedAt,
        r.lastUpdatedBy
    ]);

    return columnNames.concat(exportData);
}


function controller($q, $timeout, serviceBroker, notification) {
    const vm = this;

    const calcGridData = () => {
        return mkGridData(
            vm.parentEntityRef,
            vm.relationships,
            vm.measurables,
            vm.categories,
            vm.selectionFilterFn);
    };


    // -- INTERACT --

    vm.refresh = ()=> {
        vm.cancelEditor();
        loadRelationships()
            .then(() => {
                if (vm.selectedRow) {
                    vm.selectedRow = _.find(vm.gridData || [], row => {
                        const sameSrc = sameRef(vm.selectedRow.a, row.a);
                        const sameTarg = sameRef(vm.selectedRow.b, row.b);
                        const sameRelKind = vm.selectedRow.relationship.relationship === row.relationship.relationship;
                        return sameSrc && sameTarg && sameRelKind;
                    });
                }
            });
    };

    vm.selectCategory = (c) => $timeout(() => {
        vm.selectedCategory = c;
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
    });

    vm.selectRow = (r) => {
        if (r === vm.selectedRow) {
            vm.clearRowSelection(); // toggle
        } else {
            vm.selectedRow = r;
        }
        vm.cancelEditor();
    };

    vm.clearRowSelection = () => {
        vm.selectedRow = null;
    };

    vm.isSelected = (row) => {
        if (vm.selectedRow) {
            const sameA = sameRef(row.a, vm.selectedRow.a);
            const sameB = sameRef(row.b, vm.selectedRow.b);
            return sameA && sameB;
        } else {
            return false;
        }
    };

    vm.removeRelationship = (rel) => {
        if (confirm('Are you sure you want to delete this relationship ?')) {
            remove(rel)
                .then(() => {
                    notification.warning('Relationship removed');
                    vm.clearRowSelection();
                    loadRelationships();
                })
                .catch(e => {
                    notification.error("Relationship could not be removed", e)
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
    };

    vm.updateExistingRelationship = () => {
        vm.visibility.editor = true;
        vm.visibility.createEditor = false;
        vm.visibility.updateEditor = true;
    };

    vm.selectionFilterFn = DEFAULT_SELECTION_FILTER_FN;

    vm.export = () => {
        const data = mkExportData(vm.relationships, vm.categories, vm.measurables);
        downloadTextFile(data, ",", "related_viewpoints.csv");
    };


    // -- API --

    const loadRelationships = () => {
        return serviceBroker
            .loadViewData(CORE_API.MeasurableRelationshipStore.findByEntityReference, [vm.parentEntityRef], { force: true })
            .then(r => {
                vm.relationships = sanitizeRelationships(r.data, vm.measurables, vm.categories);
                vm.gridData = calcGridData();
            });
    };

    const loadAll = () => {
        const promises = [
            serviceBroker.loadAppData(CORE_API.MeasurableCategoryStore.findAll).then(r => r.data),
            serviceBroker.loadAppData(CORE_API.MeasurableStore.findAll).then(r => r.data)
        ];
        return $q
            .all(promises)
            .then(([categories, measurables]) => {
                vm.categories = categories;
                vm.measurables = measurables;
            })
            .then(loadRelationships)

    };

    const remove = (rel) => {
        return serviceBroker
            .execute(CORE_API.MeasurableRelationshipStore.remove, [rel])
    };


    // -- BOOT --
    vm.$onInit = () => {
        initialiseData(vm, initialState);
        loadAll();
    };
}


controller.$inject = [
    '$q',
    '$timeout',
    'ServiceBroker',
    'Notification'
];


const component = {
    template,
    bindings,
    controller
};


export default component;