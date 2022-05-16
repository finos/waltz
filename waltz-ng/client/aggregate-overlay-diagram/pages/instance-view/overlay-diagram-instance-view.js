import template from "./overlay-diagram-instance-view.html";
import OverlayDiagramInstanceView from "../../components/OverlayDiagramInstanceView.svelte"
import {initialiseData} from "../../../common";
import {entity} from "../../../common/services/enums/entity";
import {CORE_API} from "../../../common/services/core-api-utils";

const bindings = {}

const initialState = {
    OverlayDiagramInstanceView
}


function controller($stateParams,
                    serviceBroker,
                    historyStore) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

        vm.parentEntityRef = {
            kind: "AGGREGATE_OVERLAY_DIAGRAM_INSTANCE",
            id: $stateParams.id,
            name: "?"
        };

        serviceBroker
            .loadViewData(CORE_API.AggregateOverlayDiagramInstanceStore.getById, [vm.parentEntityRef.id])
            .then(r => {
                historyStore.put(
                    r.data.name,
                    entity.AGGREGATE_OVERLAY_DIAGRAM_INSTANCE.key,
                    "main.aggregate-overlay-diagram.instance-view",
                    {id: vm.parentEntityRef.id});
            })
    }


}


controller.$inject = [
    "$stateParams",
    "ServiceBroker",
    "HistoryStore"
];

const component = {
    template,
    bindings,
    controller
}

export default {
    component,
    id: "waltzOverlayDiagramInstanceView"
}