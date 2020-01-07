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
import template from "./related-measurables-tree.html";
import {initialiseData, isEmpty} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {sameRef} from "../../../common/entity-utils";
import {buildHierarchies} from "../../../common/hierarchy-utils";
import {truncateMiddle} from "../../../common/string-utils";

const bindings = {
    parentEntityRef: '<',
    category: '<'
};

const initialState = {
    selectedNode: null,
    allRelationships: [],
    measurablesByCategory: {}
};


function determineRequiredMeasurables(relationships = [], measurables = [], exclusions = []) {
    const measurablesById = _.keyBy(measurables, 'id');

    const directMeasurables = _
        .chain(relationships)
        .flatMap(rel => [rel.a, rel.b])
        .filter(ref => ref.kind === 'MEASURABLE' && measurablesById[ref.id])
        .uniqBy(ref => ref.id)
        .reject(ref => _.some(exclusions, exclusion => sameRef(exclusion, ref, { skipChecks: true })))
        .map(ref => Object.assign({}, measurablesById[ref.id], { direct: true }))
        .value();

    const directMeasurableIds = _.map(directMeasurables, 'id');

    const indirectMeasurables = _
        .chain(directMeasurables)
        .flatMap(m => {
            const parents = [];
            let p = measurablesById[m.parentId];
            while(p) {
                parents.push(p);
                p = measurablesById[p.parentId];
            }
            return parents;
        })
        .uniqBy(m => m.id)
        .reject(m => _.includes(directMeasurableIds, m.id))
        .map(ref => Object.assign({}, measurablesById[ref.id], { direct: false }))
        .value();

    return _.concat(directMeasurables, indirectMeasurables);
}


function prepareTreeData(allRelationships = [], measurables = []) {
    const requiredMeasurables = determineRequiredMeasurables(allRelationships, measurables);
    return buildHierarchies(requiredMeasurables, false);
}


function expandTreeData(treeData = []) {
    if (isEmpty(treeData)) return [] ;
    return  _.concat(treeData, treeData[0].children);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.treeData = prepareTreeData(vm.allRelationships, vm.measurablesByCategory[vm.category.id]);
        vm.expandedNodes = expandTreeData(vm.treeData);
        vm.selectedNode = null;
    };

    vm.$onInit = () => {
        const relationshipPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableRelationshipStore.findByEntityReference, [vm.parentEntityRef], { force: true })
            .then(r => vm.allRelationships = r.data);

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurablesByCategory = _
                .chain(r.data)
                .map(m => Object.assign({}, m, { displayName: truncateMiddle(m.name, 96)}))
                .groupBy('categoryId')
                .value());

        relationshipPromise
            .then(() => measurablesPromise)
            .then(() => refresh());
    };

    vm.$onChanges = () => {
        if (vm.allRelationships && vm.measurablesByCategory) {
            refresh();
        }
    };

    vm.onSelect = (node) => {
        vm.selectedNode = node;
    };
}

controller.$inject =  [ 'ServiceBroker' ];


const component = {
    controller,
    template,
    bindings
};


const id=  'waltzRelatedMeasurablesTree';


export default {
    id,
    component
};