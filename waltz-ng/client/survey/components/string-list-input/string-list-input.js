import template from "./string-list-input.html";
import {initialiseData} from "../../../common";
import StringListInput from "./StringListInput.svelte";

const bindings = {
    question: "<",
    responses: "<?",
    onSaveListResponse: "<?"
};

const initialState = {
    onSaveListResponse: (d) => console.log("default on save list response handler", d),
    responses: [],
    StringListInput
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.onListChange = (newList) => {
        vm.onSaveListResponse(vm.question.id, newList);
    }
}


controller.$inject = [];

const component = {
    template,
    bindings,
    controller
}

export default {
    id: "waltzSurveyStringListInput",
    component
}