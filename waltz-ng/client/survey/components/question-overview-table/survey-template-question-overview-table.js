import template from "./survey-template-question-overview-table.html";
import {initialiseData} from "../../../common";


const bindings = {
    questionInfos: "<",
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


function controller($transclude) {
    const vm = initialiseData(this, initialState);
}


controller.$inject = ["$transclude"];


export default {
    id: "waltzSurveyTemplateQuestionOverviewTable",
    component: {
        template,
        controller,
        bindings,
    }
};

