import FlowDiagram from "./FlowDiagram.svelte"
import {processor, store} from "./diagram-model-store";
import {initialiseData} from "../../../common";

const initialState = {
    FlowDiagram
};

function controller(flowDiagramStateService) {
    const vm = initialiseData(this, initialState);

    processor.set(flowDiagramStateService.processCommands);
    flowDiagramStateService.onChange((newState) => store.set(newState));


    vm.doSave = flowDiagramStateService.save;

}


controller.$inject = [
    "FlowDiagramStateService",
];


const component = {
    template: `<waltz-svelte-component component="$ctrl.FlowDiagram" do-save="$ctrl.doSave">
               </waltz-svelte-component>`,
    bindings: {},
    controller
};



export default {
    id: "waltzFlowDiagramSvelte",
    component
};