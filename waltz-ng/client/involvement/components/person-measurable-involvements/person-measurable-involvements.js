import template from "./person-measurable-involvements.html"
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import * as _ from "lodash";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {mkRef} from "../../../common/entity-utils";
import {mkLinkGridCell} from "../../../common/grid-utils";
import {buildHierarchies, reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";

const bindings = {
    person: "<",
    categoryId: "<"
};

const initialState = {
    enrichedInvolvements: []
};


const columnDefs = [
    mkLinkGridCell("Name", "name", "id", "main.measurable.view"),
    ];


function controller(serviceBroker, $q){

    const vm = initialiseData(this, initialState);

    vm.columnDefs = columnDefs;

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
        .then(d => d.data);

    const directMeasurablesPromise = serviceBroker
        .loadViewData(CORE_API.MeasurableStore.findMeasurablesBySelector,
            [ mkSelectionOptions(mkRef("PERSON", vm.person.id), "CHILDREN") ])
        .then(r => r.data);

    const allMeasurablePromise = serviceBroker
        .loadViewData(CORE_API.MeasurableStore.findAll)
        .then(r => r.data);


    $q
        .all([allMeasurablePromise, involvementPromise, directMeasurablesPromise, involvementKindPromise, reportingPeoplePromise])
        .then(([allMeasurables, directInvolvements, directMeasurables, involvementKinds, reportees]) => {

            //not sure I need to do the direct measurables call... if i have all the measurables/ reporting people and direct involvements...


            vm.peopleByEmployeeId = _.keyBy(_.concat(reportees, vm.person), p => p.employeeId);
            vm.relatedMeasurableIds = _.map(directMeasurables, m => m.id);
            vm.involvementKindsById = _.keyBy(involvementKinds, d => d.id);

           const involvedEntityIds = _.map(directInvolvements, i => i.entityReference.id);

            vm.directlyInvolvedMeasurables = _.filter(directMeasurables, d => d.categoryId === vm.categoryId && _.includes(involvedEntityIds, d.id));
            vm.indirectlyInvolvedMeasurables = _.filter(directMeasurables, d => d.categoryId === vm.categoryId && !_.includes(involvedEntityIds, d.id));

            const nodes = _.chain(allMeasurables)
                .filter(m => m.categoryId === vm.categoryId)
                .map(m => Object.assign({}, m, {
                    direct: _.includes(involvedEntityIds, m.id),
                    related: _.includes(vm.relatedMeasurableIds, m.id)}))
                .value();

            const reducedNodes = reduceToSelectedNodesOnly(nodes, vm.relatedMeasurableIds);

            vm.hierarchy = buildHierarchies(reducedNodes, false);

        });


    vm.onSelect = (node) => {

        serviceBroker
            .loadViewData(CORE_API.InvolvementStore.findByEntityReference, [mkRef("MEASURABLE", node.id)])
            .then(r => {
                vm.enrichedInvolvements = _.chain(r.data)
                        .filter(d => _.includes(vm.relatedMeasurableIds, d.entityReference.id))
                        .map(d => Object.assign({}, d, {
                            person: _.get(vm.peopleByEmployeeId, d.employeeId, null),
                            involvementKind: _.get(vm.involvementKindsById, d.kindId, null)

                        })
                        )
                    .value()}
            )
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
