
import template from "./attestation-summary-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineDownwardsScopeForKind, mkSelectionOptions} from "../../../common/selector-utils";
import {
    attestationPieConfig,
    mkAppAttestationGridData, prepareSummaryData, attestationSummaryColumnDefs
} from "../../attestation-pie-utils";
import {entity} from "../../../common/services/enums/entity";


const initialState = {
    visibility : {
        tableView: false
    }
};

const bindings = {
    parentEntityRef: "<"
};

function controller($q,
                    serviceBroker,
                    displayNameService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selectionOptions = mkSelectionOptions(vm.parentEntityRef,
            determineDownwardsScopeForKind(vm.parentEntityRef.kind));

        const attestationInstancePromise = serviceBroker
            .loadViewData(CORE_API.AttestationInstanceStore.findBySelector,
                [selectionOptions])
            .then(r => r.data);

        const attestationRunPromise = serviceBroker
            .loadViewData(CORE_API.AttestationRunStore.findBySelector,
                [selectionOptions])
            .then(r => r.data);

        const appPromise = serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector,
                [selectionOptions])
            .then(r => r.data);

        $q.all([attestationInstancePromise, attestationRunPromise, appPromise])
            .then(([attestationInstances, attestationRuns, applications]) => {
                vm.applications = applications;
                vm.gridDataByLogicalFlow = mkAppAttestationGridData(applications, attestationRuns, attestationInstances, entity.LOGICAL_DATA_FLOW.key, displayNameService);
                vm.gridDataByPhysicalFlow = mkAppAttestationGridData(applications, attestationRuns, attestationInstances, entity.PHYSICAL_FLOW.key, displayNameService);
                vm.summaryData = {
                    logical: prepareSummaryData(vm.gridDataByLogicalFlow),
                    physical: prepareSummaryData(vm.gridDataByPhysicalFlow)
                };

            });
    };

    const gridSelected = (d, grid) => {
        vm.selectedApps = _.filter(grid, app => app.isAttested === d.key);
        vm.columnDefs = attestationSummaryColumnDefs;
        vm.visibility.tableView = true;
    };

    vm.onSelectLogicalFlow = (d) => {
        gridSelected(d, vm.gridDataByLogicalFlow);
    };

    vm.onSelectPhysicalFlow = (d) => {
        gridSelected(d, vm.gridDataByPhysicalFlow)
    };

    vm.config =  {
        logical: Object.assign({}, attestationPieConfig, { onSelect: vm.onSelectLogicalFlow }),
        physical: Object.assign({}, attestationPieConfig, { onSelect: vm.onSelectPhysicalFlow }),
    };


}



controller.$inject = [
    "$q",
    "ServiceBroker",
    "DisplayNameService"
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