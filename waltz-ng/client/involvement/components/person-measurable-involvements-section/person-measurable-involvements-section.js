import template from "./person-measurable-involvements-section.html"
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import * as _ from "lodash";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {mkRef} from "../../../common/entity-utils";
import {buildHierarchies, reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    activeTab: null,
    showDirectsOnly: false
};


function controller(serviceBroker, $q){

    const vm = initialiseData(this, initialState);

    function loadData(){

        const measurableCategoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r =>  r.data);

        const reportingPeoplePromise = serviceBroker
            .loadViewData(CORE_API.PersonStore.findAllReportees,
                [vm.person.employeeId])
            .then(r => r.data);

        const involvementKindPromise = serviceBroker
            .loadAppData(CORE_API.InvolvementKindStore.findAll)
            .then(r => r.data);

        const involvementPromise = serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findByEmployeeId,
                [vm.person.employeeId])
            .then(r => _.filter(r.data, d => d.entityReference.kind === "MEASURABLE"));

        const directMeasurablesPromise = serviceBroker
            .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector,
                [ mkSelectionOptions(mkRef("PERSON", vm.person.id), "CHILDREN") ])
            .then(r => r.data);

        const allMeasurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => r.data);


        $q
            .all([measurableCategoriesPromise, allMeasurablesPromise, involvementPromise, directMeasurablesPromise, involvementKindPromise, reportingPeoplePromise])
            .then(([categories, allMeasurables, directInvolvements, directMeasurables, involvementKinds, reportees]) => {

                const measurableCategoryIds = _.map(directMeasurables, m => m.categoryId);
                const involvedEntityIds = _.map(directInvolvements, i => i.entityReference.id);
                vm.relatedMeasurableIds = _.map(directMeasurables, m => m.id);

                vm.tabs = _.filter(categories, d => _.includes(measurableCategoryIds, d.id));
                vm.activeTab = _.first(vm.tabs);

                vm.peopleByEmployeeId = _.keyBy(_.concat(reportees, vm.person), p => p.employeeId);
                vm.involvementKindsById = _.keyBy(involvementKinds, d => d.id);

                return vm.nodes = _.map(allMeasurables, m => Object.assign({}, m, {
                    direct: _.includes(involvedEntityIds, m.id),
                    related: _.includes(vm.relatedMeasurableIds, m.id)}));
            })
            .then(() => mkTree(vm.showDirectsOnly));
    }

    function mkTree(showDirectsOnly){

        const nodes = _.filter(vm.nodes, m => m.categoryId === vm.activeTab.id);

        const directNodeIds = _.chain(nodes)
            .filter(d => d.direct)
            .map(d => d.id)
            .value();

        const selectedIds = (showDirectsOnly)
            ? _.filter(vm.relatedMeasurableIds, d => _.includes(directNodeIds, d))
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
        mkTree(vm.showDirectsOnly);
    };

    vm.onSelect = (node) => {

        serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findByEntityReference, [mkRef("MEASURABLE", node.id)])
            .then(r => {
                node.roleInfo = _.chain(r.data)
                    .filter(d => _.includes(vm.relatedMeasurableIds, d.entityReference.id))
                    .map(d => {

                        const involvementKind = _.get(vm.involvementKindsById, d.kindId, null);
                        const person = _.get(vm.peopleByEmployeeId, d.employeeId, null);

                        if(_.isNull(person)){
                            return null;
                        }

                        return Object.assign({}, {
                            name: involvementKind.name,
                            value: person.displayName
                        })
                    })
                    .filter(d => !_.isNull(d))
                    .value();
            });
    };

    vm.toggleDirectsOnly = () => {
        vm.showDirectsOnly = !vm.showDirectsOnly;
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
