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
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../common/hierarchy-utils";
import template from "./data-type-usage-count-tree.html";
import {loadFlowClassificationRatings} from "../../../flow-classification-rule/flow-classification-utils";

const bindings = {
    onSelection: "<"
};


function prepareTree(dataTypes = [],
                     flowClassifications = [],
                     usageCounts = []) {

    const dataTypesById = _.keyBy(dataTypes, "id");

    _.chain(usageCounts)
        .filter(uc => uc.decoratorEntityReference.kind === "DATA_TYPE")
        .filter(uc => ! _.isNil(dataTypesById[uc.decoratorEntityReference.id]))
        .forEach(uc => {
            const dtId = uc.decoratorEntityReference.id;
            const dt = dataTypesById[dtId];
            dt.directCounts = Object.assign(
                {},
                dt.directCounts,
                { [uc.rating] : uc.count });
        })
        .value();

    const hierarchy = buildHierarchies(_.values(dataTypesById), false);

    const sumBy = (rating, n) => {
        if (!n) return 0;
        const childTotals = _.sum(_.map(n.children, c => sumBy(rating, c)));
        const total = childTotals + _.get(n, `directCounts.${rating}`, 0);
        n.cumulativeCounts = Object.assign({}, n.cumulativeCounts, { [rating] : total });
        return total;
    };

    _.forEach(hierarchy, root =>
        root.cumulativeCounts = _
            .reduce(
                flowClassifications,
                (acc, d) => {
                    const rating = d.code;
                    const count = sumBy(rating, root);
                    acc[rating] = count;
                    acc.total += count;
                    return acc;
                },
                {total: 0}));

    return hierarchy;
}


function prepareExpandedNodes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function controller($q, displayNameService, serviceBroker) {
    const vm = this;

    vm.$onInit = () => {
        const ratingsPromise = loadFlowClassificationRatings(serviceBroker)
            .then(rs => {
                vm.flowClassifications = rs;
                vm.ratings = _.map(rs, d => ({color: d.color, name: d.name, rating: d.code}))
            });

        const dataTypesPromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => {
                vm.dataTypes = r.data;
                vm.searchNodes = prepareSearchNodes(vm.dataTypes);
            });

        const summaryPromise = serviceBroker
            .loadViewData(CORE_API.LogicalFlowDecoratorStore.summarizeInboundForAll)
            .then(r => vm.summaries = r.data);

        $q.all([ratingsPromise, dataTypesPromise, summaryPromise])
            .then(() => {
                vm.hierarchy = prepareTree(
                    vm.dataTypes,
                    vm.flowClassifications,
                    vm.summaries);

                vm.maxTotal = _
                    .chain(vm.hierarchy)
                    .map("cumulativeCounts.total")
                    .max()
                    .value();
            });
    };

    vm.treeOptions = {
        nodeChildren: "children",
        dirSelectable: true,
        equality: (a, b) => a && b && a.id === b.id
    };

    vm.searchTermsChanged = (termStr = "") => {
        const matchingNodes = doSearch(termStr, vm.searchNodes);
        vm.hierarchy = prepareTree(matchingNodes);
        vm.expandedNodes = prepareExpandedNodes(vm.hierarchy);
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged("");
        vm.searchTerms = "";
    };
}


controller.$inject = [
    "$q",
    "DisplayNameService",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzDataTypeUsageCountTree";


export default {
    id,
    component
}