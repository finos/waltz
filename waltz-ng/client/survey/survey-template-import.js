import SurveyTemplateImport from "./SurveyTemplateImport.svelte";
import {initialiseData} from "../common";

const template = `
    <waltz-svelte-component component="$ctrl.SurveyTemplateImport">
    </waltz-svelte-component>
`;

const bindings = {};

const initialState = {
    SurveyTemplateImport
}


function controller() {
    const vm = initialiseData(this, initialState);
}

controller.$inject = [];

const component = {
    template,
    controller,
    bindings
}

export default {
    component,
    id: "waltzSurveyTemplateImport",
};