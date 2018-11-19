import template from "./pending-taxonomy-changes-list.html";
import {initialiseData} from "../../../common";


const bindings = {
    pendingChanges: "<",
    onSelect: "<"
};


const initialState = {
    onSelect: (c) => console.log("WPTCL:onSelect default handler", c)
};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzPendingTaxonomyChangesList"
}
