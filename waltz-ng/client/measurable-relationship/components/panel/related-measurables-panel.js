/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import _ from 'lodash';
import {initialiseData} from '../../../common';
import {toOptions, measurableRelationshipKindNames}  from '../../../common/services/display-names';


/**
 * @name waltz-related-measurables-panel
 *
 * @description
 * This component ...
 */


const bindings = {
    categories: '<',
    measurable: '<',
    measurables: '<',
    relationships: '<',
    onSave: '<',
    onRemove: '<'
};



const initialState = {
    categories: [],
    measurable: null,
    measurables: [],
    relationships: [],
    onSave: (d) => Promise.resolve(console.log('wrmp:onSave - default impl', d)),
    onRemove: (d) => Promise.resolve(console.log('wrmp:onRemove - default impl', d))
};


const template = require('./related-measurables-panel.html');

const DEFAULT_SELECTION_FILTER_FN = (m) => true;
const DEFAULT_RELATIONSHIP_FORM = {
    description: '',
    relationshipKind: "STRONGLY_RELATES_TO",
    measurable: null
};


function categorizeRelationships(relationships = [], measurables = [], id) {
    const measurablesById = _.keyBy(measurables, 'id');
    return _
        .chain(relationships)
        .map(reln => id === reln.measurableA
            ? reln.measurableB
            : reln.measurableA) // counterpart
        .map(counterpartId => measurablesById[counterpartId])
        .groupBy(measurable => measurable.categoryId)
        .value();
}


function mkGridData(id,
                    relationships = [],
                    measurables = [],
                    categories = [],
                    selectedCategory)
{
    const measurablesById = _.keyBy(measurables, 'id');
    const categoriesById = _.keyBy(categories, 'id');
    return _.chain(relationships)
        .map(r => {
            const outbound = r.measurableA === id;
            const measurableA = measurablesById[r.measurableA];
            const measurableB = measurablesById[r.measurableB];
            const categoryA = categoriesById[measurableA.categoryId];
            const categoryB = categoriesById[measurableB.categoryId];

            const counterpart = Object.assign({}, outbound ? measurableB : measurableA, { kind: 'MEASURABLE' });

            if (selectedCategory) {
                if (outbound && measurableB.categoryId !== selectedCategory.id) {
                    return null;
                }
                if (!outbound && measurableA.categoryId !== selectedCategory.id) {
                    return null;
                }
            }

            return {
                outbound,
                measurableA,
                measurableB,
                categoryA,
                categoryB,
                relationship: r,
                counterpart
            };
        })
        .filter(r => r !== null)
        .sortBy('measurableA.name')
        .value()
}


function controller($timeout, notification) {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    const calcGridData = () => mkGridData(
        vm.measurable.id,
        vm.relationships,
        vm.measurables,
        vm.categories,
        vm.selectedCategory);

    vm.$onChanges = (c) => {
        if (vm.measurable && vm.measurables) {
            vm.relatedByCategory = categorizeRelationships(
                vm.relationships,
                vm.measurables,
                vm.measurable.id);
            vm.gridData = calcGridData();
        }
    };

    vm.selectCategory = (c) => $timeout(() => {
        vm.selectedCategory = c;
        vm.gridData = calcGridData(c);
        vm.selectedRow = null;
        const validMeasurableIds = _
            .chain(vm.measurables)
            .filter(m => m.categoryId === c.id)
            .map('id')
            .value();

        vm.selectionFilterFn = m => m && _.includes(validMeasurableIds, m.id);
    });

    vm.clearCategory = () => $timeout(() => {
        vm.selectedCategory = null;
        vm.selectedRow = null;
        vm.gridData = calcGridData();
        vm.selectionFilterFn = DEFAULT_SELECTION_FILTER_FN;
    });

    vm.selectRow = (r) => {
        if (r === vm.selectedRow) {
            vm.clearRow(); // toggle
        } else {
            vm.selectedRow = r;
        }
        vm.cancelEditor();
    };

    vm.clearRow = () => {
        vm.selectedRow = null;
    };

    vm.isSelected = (row) => {
        if (vm.selectedRow) {
            const sameA = row.measurableA.id === vm.selectedRow.measurableA.id;
            const sameB = row.measurableB.id === vm.selectedRow.measurableB.id;
            return sameA && sameB;
        } else {
            return false;
        }
    };

    vm.beginNewRelationship = () => {
        if (vm.relationshipForm) return; // nothing to do
        vm.selectedRow = null;
        vm.relationshipForm = Object.assign(
            {},
            DEFAULT_RELATIONSHIP_FORM,
            { measurableA: vm.measurable, mode :'CREATE' });
    };

    vm.cancelEditor = () => {
        vm.relationshipForm = null;
    };

    vm.onMeasurableSelection = (correlationId, m) => vm.relationshipForm.measurableB = m;

    vm.selectionFilterFn = DEFAULT_SELECTION_FILTER_FN;

    vm.relationshipKinds = toOptions(measurableRelationshipKindNames);

    vm.isFormValid = () => {
        const form = vm.relationshipForm;

        const hasMeasurable = form.measurableA != null && form.measurableB != null;
        const hasKind = form.relationshipKind != null;
        const notSelf = hasMeasurable && form.measurableA.id != form.measurableB.id;

        return hasMeasurable && hasKind && notSelf;
    };

    vm.submit = () => {
        if (vm.isFormValid()) {
            const form = vm.relationshipForm;
            const submission = {
                measurableA: form.measurableA.id,
                measurableB: form.measurableB.id,
                relationshipKind: form.relationshipKind,
                description: form.description
            };
            vm.onSave(submission)
                .then(() => {
                    notification.success("Relationship saved");
                    vm.cancelEditor();
                })
                .catch(e => {
                    notification.error("Could not save because: "+e.message);
                });
        }
    };

    vm.removeRow = () => {
        vm.onRemove(vm.selectedRow.relationship)
            .then(() => {
                notification.success("Relationship removed");
                vm.clearRow();
            })
            .catch(e => {
                notification.error("Could not remove because: "+e.message);
            });
    };

    vm.editRow = () => {
        const relationship = vm.selectedRow.relationship;
        vm.relationshipForm = {
            description: relationship.description,
            measurableA: vm.selectedRow.measurableA,
            measurableB: vm.selectedRow.measurableB,
            relationshipKind: relationship.relationshipKind,
        };
    }
}


controller.$inject = [
    '$timeout',
    'Notification'
];


const component = {
    template,
    bindings,
    controller
};


export default component;