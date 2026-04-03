import template from "./outstanding-actions-notification-panel.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {entity} from "../../../common/services/enums/entity";
import {isDataFlowProposalsEnabled} from "../../../common/utils/settings-util";


const bindings = {

};


const initialState = {
    summaries: [],
    settings: null,
    dataFlowProposalsEnabled: null
};


const stateNameByKind = {
    SURVEY_INSTANCE: "main.survey.instance.user",
    ATTESTATION: "main.attestation.instance.user",
    PROPOSED_FLOW: "main.data-flow.dashboard"
};


const labelsByKind = {
    SURVEY_INSTANCE: "Surveys",
    ATTESTATION: "Attestations",
    PROPOSED_FLOW: "Proposed Flows"
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadSettings = () => {
        return serviceBroker
            .loadViewData(CORE_API.SettingsStore.findAll, [])
            .then(r => {
                vm.settings = r.data;
                vm.dataFlowProposalsEnabled= isDataFlowProposalsEnabled(vm.settings)
            });
    }

    vm.$onInit = () => {
        loadSettings()
            .then(() => serviceBroker.loadViewData(
                CORE_API.NotificationStore.findAll,
                [],
                { force: true }))
            .then(r => {
                const summaries = vm.dataFlowProposalsEnabled
                    ? r.data.summary
                    : r.data.summary.filter(d => d.kind !== "PROPOSED_FLOW");

                vm.summaries = _
                    .chain(summaries)
                    .map(d => {
                        const attrs = {
                            label: labelsByKind[d.kind],  // don't use the ones from `entity` as need plurals
                            icon: entity[d.kind].icon,
                            uiStateName: stateNameByKind[d.kind] || "main.home",
                            count: d.count
                        };
                        return Object.assign({}, d, attrs);
                    })
                    .value();
            });
    }
}

controller.$inject = [
    "ServiceBroker"
];


export default {
    id: "waltzOutstandingActionsNotificationPanel",
    component: {
        controller,
        bindings,
        template
    }
};
