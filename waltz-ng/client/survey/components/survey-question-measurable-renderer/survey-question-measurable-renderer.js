import template from "./survey-question-measurable-renderer.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {mkRef} from "../../../common/entity-utils";
import _ from "lodash";
import {reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";
import {parseMeasurableListResponse} from "../../survey-utils";

const bindings = {
    question: "<",
    listResponse: "<"
};

const initialState = {
    
};

function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);


    vm.$onInit = () => {
        const measurableCategoryId = vm.question.qualifierEntity.id;
        const selectionOptions = mkSelectionOptions(mkRef("MEASURABLE_CATEGORY", measurableCategoryId), "EXACT");

        serviceBroker
            .loadAppData(
                CORE_API.MeasurableStore.findMeasurablesBySelector,
                [selectionOptions])
            .then(r => {
                vm.measurables = r.data;
                const measurablesById = _.keyBy(vm.measurables, d => d.id);

                const {measurableIds, notFoundSiphon} = parseMeasurableListResponse(vm.listResponse, measurablesById);

                vm.expandedNodes = measurableIds;
                vm.requiredMeasurables = reduceToSelectedNodesOnly(vm.measurables, measurableIds);
                vm.notFoundResults = _.map(notFoundSiphon.results, d => d.input);
            });

        vm.onSelect = () => {};
    }
}

controller.$inject = [
    "ServiceBroker"
];

const component = {
    bindings, 
    template,
    controller
};

export default {
    component,
    id : "waltzSurveyQuestionMeasurableRenderer"
}