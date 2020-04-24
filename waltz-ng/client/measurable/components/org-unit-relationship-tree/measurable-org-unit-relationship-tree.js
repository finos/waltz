import template from "./measurable-org-unit-relationship-tree.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {
    buildHierarchies,
    doSearch,
    prepareSearchNodes,
    reduceToSelectedNodesOnly
} from "../../../common/hierarchy-utils";
import _ from "lodash";


const bindings ={
    parentEntityRef: "<",
    selectedCategory: "<"
};


const initialState = {
    linkToState: "main.measurable.view",
    displayFullTree: false,
    searchNodes: [],
    searchTerms: "",
};


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function loadData() {

        const allMeasurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => r.data);

        const relatedMeasurablesPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findByOrgUnitId,
                [vm.parentEntityRef.id])
            .then(r => r.data);

        const orgUnitPromise = serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => r.data);

        $q
            .all([allMeasurablesPromise, relatedMeasurablesPromise, orgUnitPromise])
            .then(([allMeasurables, relatedMeasurables, orgUnits]) => {

                const relatedMeasurableIds = _.map(relatedMeasurables, d => d.id);
                const orgUnitsById = _.keyBy(orgUnits, d => d.id);

                vm.enrichedNodes = _.chain(allMeasurables)
                    .filter(m => vm.selectedCategory.id === m.categoryId)
                    .map(m => Object.assign({}, m, {
                        direct: _.includes(relatedMeasurableIds, m.id),
                        orgUnit: _.get(orgUnitsById, m.organisationalUnitId, null) }))
                    .value();

                vm.directNodes = reduceToSelectedNodesOnly(vm.enrichedNodes, relatedMeasurableIds);

                return vm.hierarchy = buildHierarchies(vm.directNodes, false)

            })
            .then(() => vm.searchNodes = prepareSearchNodes(vm.enrichedNodes));
    }

    vm.$onInit = () => {
        loadData();
    };

    vm.$onChanges = (c) => {
        if(c.selectedCategory){
            vm.displayFullTree = false;
            loadData();
        }
    };

    vm.showAll = () => {
        vm.hierarchy = buildHierarchies(vm.enrichedNodes, false);
        vm.displayFullTree = true;
    };

    vm.searchTermsChanged = (termStr = "") => {
        if (termStr === "") {
            const nodes = (vm.displayFullTree) ? vm.enrichedNodes : vm.directNodes;
            vm.hierarchy = buildHierarchies(nodes, false);
            vm.expandedNodes = [];
        } else {
            const matchedNodes = doSearch(termStr, vm.searchNodes);
            vm.hierarchy = buildHierarchies(matchedNodes, false);
            vm.expandedNodes = matchedNodes;
        }
    };

    vm.clearSearch = () => {
        vm.searchTermsChanged("");
        vm.searchTerms = "";
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzMeasurableOrgUnitRelationshipTree"
};
