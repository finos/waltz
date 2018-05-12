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
import {CORE_API} from "../../../common/services/core-api-utils";
import {availableRelationshipKinds} from "./related-measurable-editor-utils";
import {buildHierarchies, switchToParentIds} from "../../../common/hierarchy-utils";
import {toEntityRef} from "../../../common/entity-utils";

import template from "./create-related-measurable-editor.html";


const bindings = {
    parentEntityRef: '<',
    type: '<',
    onCancel: '<',
    onRefresh: '<'
};


const initialState = {
    form: {
        relationshipKind: 'RELATES_TO',
        description: null,
        counterpart: null
    },
    counterpartType: '',
    measurables: [],
    categories: [],
    availableRelationshipKinds,
    relationshipKindsKey: '',
    onCancel: () => console.log('wcrme: onCancel - default impl'),
    onRefresh: () => console.log('wcrme: onRefresh - default impl'),
    treeOptions: {
        isSelectable: node => node.concrete
    },
    visibility: {
        changeInitiativeSelector: false,
        measurableSelector: false
    }
};


function readCategoryId(compoundId) {
    // e.g. MEASURABLE/12
    return +_.split(compoundId, '/')[1];
}


function prepareTree(nodes = []) {
    return switchToParentIds(buildHierarchies(nodes));
}


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const isMeasurable = _.startsWith(vm.type.id, 'MEASURABLE');

        if (isMeasurable) {
            vm.visibility.measurableSelector = true;
            const categoryId = readCategoryId(vm.type.id);
            serviceBroker
                .loadAppData(CORE_API.MeasurableStore.findAll)
                .then(r => {
                    vm.measurables = _.filter(r.data, {categoryId});
                    vm.nodes = prepareTree(vm.measurables);
                });

            serviceBroker
                .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
                .then(r => vm.categories = r.data)
                .then(cs => vm.category = _.find(cs, {id: categoryId}))
                .then(c => vm.counterpartType = c.name);

            serviceBroker
                .loadViewData(
                    CORE_API.MeasurableRelationshipStore.findByEntityReference,
                    [vm.parentEntityRef],
                    { force: true })
                .then(r => {
                    const usedIds = _
                        .chain(r.data)
                        .flatMap(rel => {
                            const ids = [];
                            if (rel.a.kind === 'MEASURABLE') ids.push(rel.a.id);
                            if (rel.b.kind === 'MEASURABLE') ids.push(rel.b.id);
                            return ids;
                        })
                        .uniq()
                        .value();

                    vm.treeOptions.isSelectable = (node) => {
                        return node.concrete && ! _.includes(usedIds, node.id);
                    };
                })
        } else {
            vm.visibility.changeInitiativeSelector = true;
            vm.counterpartType = 'Change Initiative';
        }

        vm.relationshipKindsKey = vm.parentEntityRef.kind + '-' + (isMeasurable ? 'MEASURABLE' : 'CHANGE_INITIATIVE');
    };


    // -- INTERACT --

    vm.onChangeInitiativeSelection = (changeInitiative) => {
        vm.form.counterpart = changeInitiative;
    };

    vm.onMeasurableSelection = (node) => {
        vm.form.counterpart = toEntityRef(node, 'MEASURABLE');
    };

    vm.isFormValid = () => {
        const hasCounterpart = vm.form.counterpart !== null;
        const hasRelationship = vm.form.relationshipKind !== null;
        return hasCounterpart && hasRelationship;
    };

    vm.submit = () => {
        if (vm.isFormValid()) {
            const form = vm.form;
            const submission = {
                a: vm.parentEntityRef,
                b: form.counterpart,
                relationshipKind: form.relationshipKind,
                description: form.description
            };

            save(submission)
                .then(() => {
                    notification.success("Relationship saved");
                    vm.onRefresh();
                })
                .catch(e => {
                    notification.error("Could not save because: "+e.message);
                });
        }
    };


    // -- API ---

    const save = d => {
        return serviceBroker
            .execute(CORE_API.MeasurableRelationshipStore.create, [d]);
    };

}


controller.$inject = [
    'Notification',
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzCreateRelatedMeasurableEditor";


export default {
    id,
    component
};