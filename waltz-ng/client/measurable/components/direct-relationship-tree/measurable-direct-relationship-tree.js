import template from "./measurable-direct-relationship-tree.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {buildHierarchies, reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";
import _ from "lodash";


const bindings ={
    parentEntityRef: "<",
    selectedCategory: "<"
};


const initialState = {
    linkToState: "main.measurable.view",
    displayFullTree: false
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

        const orgUnitPromise = serviceBroker.loadAppData(CORE_API.OrgUnitStore.findAll)
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

                const nodes = reduceToSelectedNodesOnly(vm.enrichedNodes, relatedMeasurableIds);

                return vm.hierarchy = buildHierarchies(nodes)
            });
    }

    vm.$onInit = () => {
        loadData();
    };

    vm.$onChanges = (c) => {
        if(c.selectedCategory){
            loadData();
        }
    };

    vm.showAll = () => {
        vm.hierarchy = buildHierarchies(vm.enrichedNodes);
        vm.displayFullTree = true;
    }

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
    id: "waltzMeasurableDirectRelationshipTree"
};
