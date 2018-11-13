import template from "./pending-taxonomy-changes-list.html";

const bindings = {
    pendingChanges: "<"
};


function controller() {

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

