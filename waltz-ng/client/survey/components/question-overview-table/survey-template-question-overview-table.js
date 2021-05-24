import template from "./survey-template-question-overview-table.html";
import {initialiseData} from "../../../common";


const bindings = {
    questions: "<",
    actions: "<?"
}

const modes = {
    DEFAULT_VIEW: "DEFAULT_VIEW",
    CONDITIONAL_VIEW: "CONDITIONAL_VIEW",
};


const initialState = {
    actions: [],
    modes,
    mode: modes.DEFAULT_VIEW
};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [];


export default {
    id: "waltzSurveyTemplateQuestionOverviewTable",
    component: {
        template,
        controller,
        bindings,
    }
};

