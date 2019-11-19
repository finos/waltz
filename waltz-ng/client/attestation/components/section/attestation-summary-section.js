
import template from "./attestation-summary-section.html";
import {initialiseData, toKeyCounts} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {attestationPieConfig, prepareSummaryData} from "../../attestation-pie-utils";
import {entity} from "../../../common/services/enums/entity";


const initialState = {
};

const bindings = {
    parentEntityRef: "<"
};

function controller($q,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selectionOptions = mkSelectionOptions(vm.parentEntityRef, "EXACT");

        const attestationPromise = serviceBroker
            .loadViewData(CORE_API.AttestationRunStore.findBySelector,
                [selectionOptions])
            .then(r => r.data);

        const appPromise = serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector,
                [selectionOptions])
            .then(r => r.data);

        $q.all([attestationPromise, appPromise])
            .then(([attestations, applications]) => {
                vm.attestations = attestations;
                vm.applications = applications;
                vm.summaryData = {
                    logical: prepareSummaryData(applications, attestations, entity.APPLICATION.key, entity.LOGICAL_DATA_FLOW.key),
                    physical: prepareSummaryData(applications, attestations, entity.APPLICATION.key, entity.PHYSICAL_FLOW.key)
                };
                vm.config.size = 70;
            });
    };

    vm.config = attestationPieConfig;

}

controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzAttestationSummarySection",
    controllerAs: "$ctrl"
};