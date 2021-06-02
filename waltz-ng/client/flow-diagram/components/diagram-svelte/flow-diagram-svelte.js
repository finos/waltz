import FlowDiagram from "./FlowDiagram.svelte"
import {store, processor} from "./diagram-model-store";
import {initialiseData} from "../../../common";

const initialState = {
    FlowDiagram
};

function controller(flowDiagramStateService) {
    const vm = initialiseData(this, initialState);

    processor.set(flowDiagramStateService.processCommands);
    flowDiagramStateService.onChange((newState) => console.log("ns", {newState}) || store.set(newState));
}


controller.$inject = [
    "FlowDiagramStateService"
];


const component = {
    template: `<waltz-svelte-component component="$ctrl.FlowDiagram">
               </waltz-svelte-component>`,
    bindings: {},
    controller
};



export default {
    id: "waltzFlowDiagramSvelte",
    component
};