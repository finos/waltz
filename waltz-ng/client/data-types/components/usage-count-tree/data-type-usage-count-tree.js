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
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies, doSearch, prepareSearchNodes} from "../../../common/hierarchy-utils";
import template from "./data-type-usage-count-tree.html";
import {mkAuthoritativeRatingSchemeItems} from "../../../ratings/rating-utils";

const bindings = {
    onSelection: "<"
};


function ratingToRag(r) {
    switch(r){
        case "PRIMARY":
            return "G";
        case "SECONDARY":
            return "A";
        case "DISCOURAGED":
            return "R";
        case "NO_OPINION":
            return "Z";
        default:
            return r;
    }
}


function prepareTree(dataTypes = [], usageCounts = []) {
    const dataTypesById = _.keyBy(dataTypes, "id");
    _.chain(usageCounts)
        .filter(uc => uc.decoratorEntityReference.kind === "DATA_TYPE")
        .forEach(uc => {
            const dtId = uc.decoratorEntityReference.id;
            const dt = dataTypesById[dtId];
            const rag = ratingToRag(uc.rating);
            dt.directCounts = Object.assign(
                {},
                dt.directCounts,
                { [rag] : uc.count });
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

    _.forEach(hierarchy, root => {
        const R = sumBy("R", root);
        const A = sumBy("A", root);
        const G = sumBy("G", root);
        const Z = sumBy("Z", root);
        root.cumulativeCounts = {
            R,
            A,
            G,
            Z,
            total: R + A + G + Z
        };
    });

    return hierarchy;
}


function prepareExpandedNodes(hierarchy = []) {
    return hierarchy.length < 6  // pre-expand small trees
        ? _.clone(hierarchy)
        : [];
}


function controller(displayNameService, serviceBroker) {
    const vm = this;

    vm.$onInit = () => {

        vm.ratingSchemeItems = mkAuthoritativeRatingSchemeItems(displayNameService);

        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll, [])
            .then(r => {
                vm.dataTypes = r.data;
                vm.searchNodes = prepareSearchNodes(vm.dataTypes);
            })
            .then(() => serviceBroker.loadViewData(CORE_API.LogicalFlowDecoratorStore.summarizeInboundForAll))
            .then(r => {
                vm.hierarchy = prepareTree(vm.dataTypes, r.data);
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