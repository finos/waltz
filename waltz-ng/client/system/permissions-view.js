import {initialiseData} from "../common";
import PermissionsView from "./svelte/permissions/PermissionsView.svelte";

const initialState = {
    PermissionsView
};


function controller() {
    initialiseData(this, initialState);
}

const page = {
    controller,
    template: `<waltz-svelte-component component="$ctrl.PermissionsView"></waltz-svelte-component>`,
    controllerAs: "$ctrl"
};


export default page;