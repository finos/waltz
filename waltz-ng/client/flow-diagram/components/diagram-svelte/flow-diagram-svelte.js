import FlowDiagram from "./FlowDiagram.svelte"
import {initialiseData} from "../../../common";

const initialState = {
    FlowDiagram
};

function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [];


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