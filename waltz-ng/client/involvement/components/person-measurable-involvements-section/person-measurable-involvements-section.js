import template from "./person-measurable-involvements-section.html"
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import * as _ from "lodash";
import {buildHierarchies, reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    activeTab: null,
    showDirectsOnly: false,
    showInfoPanel: false,
    nodes: [],
    activeNode: null
};


function controller(serviceBroker, $q){

    const vm = initialiseData(this, initialState);

    function loadData(){

        const measurableCategoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => r.data);

        const involvementsPromise = serviceBroker
            .loadViewData(CORE_API.InvolvementViewService.findAllInvolvementsByEmployeeId, [vm.person.employeeId])
            .then(r => _.filter(r.data, d => d.involvement.entityReference.kind === "MEASURABLE"));

        const involvementKindPromise = serviceBroker
            .loadAppData(CORE_API.InvolvementKindStore.findAll)
            .then(r => r.data);

        const allMeasurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => r.data);


        $q
            .all([measurableCategoriesPromise, allMeasurablesPromise, involvementsPromise, involvementKindPromise])
            .then(([categories, allMeasurables, measurableInvolvements, involvementKinds]) => {

                vm.involvementKindsById = _.keyBy(involvementKinds, d => d.id);
                vm.involvementsByMeasurableId = _.groupBy(measurableInvolvements, i => i.involvement.entityReference.id);
                vm.relatedMeasurableIds = _.map(measurableInvolvements, i => i.involvement.entityReference.id);

                vm.directlyInvolvedMeasurableIds = _
                    .chain(measurableInvolvements)
                    .filter(i => i.involvement.employeeId === vm.person.employeeId)
                    .map(i => i.involvement.entityReference.id)
                    .value();

                const measurableCategoryIds = _
                    .chain(allMeasurables)
                    .filter(m => _.includes(vm.relatedMeasurableIds, m.id))
                    .map(m => m.categoryId)
                    .value();

                vm.tabs = _.filter(categories, d => _.includes(measurableCategoryIds, d.id));
                vm.activeTab = _.first(vm.tabs);

                return vm.nodes = _.map(allMeasurables, m => Object.assign({}, m, {
                    direct: _.includes(vm.directlyInvolvedMeasurableIds, m.id),
                    related: _.includes(vm.relatedMeasurableIds, m.id)}));
            })
            .then(() => mkTree(vm.showDirectsOnly));
    }

    function mkTree(showDirectsOnly){

        const nodes = _.filter(vm.nodes, m => m.categoryId === _.get(vm.activeTab, 'id', null));

        const selectedIds = (showDirectsOnly)
            ? _.filter(vm.relatedMeasurableIds, d => _.includes(vm.directlyInvolvedMeasurableIds, d))
            : vm.relatedMeasurableIds;

        const reducedNodes = reduceToSelectedNodesOnly(nodes, selectedIds);
        vm.hierarchy = buildHierarchies(reducedNodes, false);
    }

    vm.$onInit = () => {
        serviceBroker.loadViewData(CORE_API.PersonStore.getById, [vm.parentEntityRef.id])
            .then(r => vm.person = r.data)
            .then(loadData);
    };

    vm.onTabChange = () => {
        vm.showInfoPanel = false;
        mkTree(vm.showDirectsOnly);
    };

    vm.onSelect = (node) => {
        vm.showInfoPanel = true;
        vm.activeNode = node;
    };

    vm.onHover = (node) => {

        const involvements = _.get(vm.involvementsByMeasurableId, node.id);

        node.roleInfo = _.map(involvements, i => {

            const involvementKind = _.get(vm.involvementKindsById, i.involvement.kindId);

            return { name: involvementKind.name, value: i.person.displayName, person: i.person}
        });
    };

    vm.toggleDirectsOnly = () => {
        vm.showDirectsOnly = !vm.showDirectsOnly;
        vm.showInfoPanel = false;
        mkTree(vm.showDirectsOnly);
    }
}


controller.$inject = ["ServiceBroker", "$q"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzPersonMeasurableInvolvementsSection"
};
