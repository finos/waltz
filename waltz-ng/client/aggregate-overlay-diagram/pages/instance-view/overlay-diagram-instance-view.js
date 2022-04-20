import template from "./overlay-diagram-instance-view.html";
import OverlayDiagramInstanceView from "../../components/OverlayDiagramInstanceView.svelte"
import {initialiseData} from "../../../common";

const bindings = {}

const initialState = {
    OverlayDiagramInstanceView
}


function controller($stateParams) {

    const vm = initialiseData(this, initialState);

    const instanceId = $stateParams.id;

    vm.parentEntityRef = {
        kind: "AGGREGATE_OVERLAY_DIAGRAM_INSTANCE",
        id: instanceId,
        name: "?"
    };

    console.log("Hello world!!!")
    console.log({per: vm.parentEntityRef});
}


controller.$inject = [
    "$stateParams"
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