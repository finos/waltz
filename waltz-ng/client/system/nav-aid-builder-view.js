import NavAidBuilder from "./svelte/nav-aid-builder/NavAidBuilder.svelte";
import {initialiseData} from "../common";

const initialState = {
    NavAidBuilder
};


function controller() {
    initialiseData(this, initialState);
}

const page = {
    controller,
    template: `<waltz-svelte-component component="$ctrl.NavAidBuilder"></waltz-svelte-component>`,
    controllerAs: '$ctrl'
};


export default page;