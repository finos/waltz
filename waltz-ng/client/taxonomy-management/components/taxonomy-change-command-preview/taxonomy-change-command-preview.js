import template from "./taxonomy-change-command-preview.html";
import {initialiseData} from "../../../common";
import {severityToBootstrapAlertClass} from "../../../common/severity-utils";


const bindings = {
    preview: "<"
};


const initialData = {
    severityToBootstrapAlertClass // exposing fn
};


function controller() {
    initialiseData(this, initialData);
}


controller.$inject = [];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: "waltzTaxonomyChangeCommandPreview"
};