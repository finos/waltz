import VersionInfoPage from "./svelte/version-info/VersionInfoPage.svelte";
import {initialiseData} from "../common";

const initialState = {
    VersionInfoPage
};


function controller() {
    initialiseData(this, initialState);
}

const page = {
    controller,
    template: `<waltz-svelte-component component="$ctrl.VersionInfoPage"></waltz-svelte-component>`,
    controllerAs: '$ctrl'
};


export default page;