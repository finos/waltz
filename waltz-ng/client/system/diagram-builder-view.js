import DiagramBuilder from "../entity-diagrams/components/entity-overlay-diagrams/builder/DiagramBuilder.svelte";
import {initialiseData} from "../common";

const initialState = {
    DiagramBuilder
};


function controller() {
    initialiseData(this, initialState);
}

const page = {
    controller,
    template: "<waltz-svelte-component component=\"$ctrl.DiagramBuilder\"></waltz-svelte-component>",
    controllerAs: "$ctrl"
};


export default page;