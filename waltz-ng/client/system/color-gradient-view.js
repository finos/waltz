import ColorScale from "./svelte/color-scale/ColorScale.svelte";
import {initialiseData} from "../common";

const initialState = {
    ColorScale
};


function controller() {
    initialiseData(this, initialState);
}

const page = {
    controller,
    template: `<waltz-svelte-component component="$ctrl.ColorScale"></waltz-svelte-component>`,
    controllerAs: '$ctrl'
};


export default page;