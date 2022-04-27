import template from "./aggregate-overlay-diagram-section.html";
import {initialiseData} from "../../../common";
import AggregateOverlayDiagramPanel from "../panel/AggregateOverlayDiagramPanel.svelte";


const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    AggregateOverlayDiagramPanel
};


function controller() {

    const vm = initialiseData(this, initialState);

}

controller.$inject = [];


const component = {
    bindings,
    controller,
    template
}


export default {
    component,
    id: "waltzAggregateOverlayDiagramSection"
}