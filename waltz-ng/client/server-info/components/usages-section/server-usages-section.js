import template from "./server-usages-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import _ from "lodash";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    usages: []
};


function prepareUsages(participations = [],
                       physicalFlows = [],
                       physicalSpecs = [],
                       logicalFlows = []) {
    const logicalsById = _.keyBy(logicalFlows, "id");
    const physicalsById = _.keyBy(physicalFlows, "id");
    const specsById = _.keyBy(physicalSpecs, "id");

    return _
        .chain(participations)
        .map(p => {
            const pf = physicalsById[p.physicalFlowId];
            if (!pf) return null;
            const ps = specsById[pf.specificationId];
            if (!ps) return null;
            const lf = logicalsById[pf.logicalFlowId];
            if (!lf) return null;
            return {
                participation: p,
                logicalFlow: lf,
                physicalSpecification: ps,
                physicalFlow: pf
            };
        })
        .compact()
        .value();
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        const participantPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowParticipantStore.findByParticipant,
                [ vm.parentEntityRef ])
            .then(r => r.data);

        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findBySelector,
                [ selectionOptions ])
            .then(r => r.data);

        const physicalSpecsPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findBySelector,
                [ selectionOptions ])
            .then(r => r.data);

        const logicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ selectionOptions ])
            .then(r => r.data);

        const promises = [
            participantPromise,
            physicalFlowPromise,
            physicalSpecsPromise,
            logicalFlowPromise
        ];

        $q.all(promises)
            .then(([ participations, physicalFlows, physicalSpecs, logicalFlows ]) =>
                vm.usages = prepareUsages(
                    participations,
                    physicalFlows,
                    physicalSpecs,
                    logicalFlows));
    }
}


controller.$inject = ["$q", "ServiceBroker"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzServerUsagesSection"
};