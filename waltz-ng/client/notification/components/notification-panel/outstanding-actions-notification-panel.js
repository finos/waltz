import template from "./outstanding-actions-notification-panel.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {entity} from "../../../common/services/enums/entity";


const bindings = {

};


const initialState = {
    summaries: []
};


const stateNameByKind = {
    SURVEY_INSTANCE: "main.survey.instance.user",
    ATTESTATION: "main.attestation.instance.user"
};


const labelsByKind = {
    SURVEY_INSTANCE: "Surveys",
    ATTESTATION: "Attestations"
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.NotificationStore.findAll,
                [],
                {
                    force: true
                })
            .then(r => {
                vm.summaries = _
                    .chain(r.data)
                    .map(d => {
                        const attrs = {
                            label: labelsByKind[d.kind],  // don't use the ones from `entity` as need plurals
                            icon: entity[d.kind].icon,
                            uiStateName: stateNameByKind[d.kind] || "main.home",
                            count: d.count
                        }
                        return Object.assign({}, d, attrs)
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
