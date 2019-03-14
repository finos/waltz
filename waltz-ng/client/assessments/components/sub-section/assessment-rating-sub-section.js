import {initialiseData} from "../../../common";

import template from "./assessment-rating-sub-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkEnrichedAssessmentDefinitions} from "../../assessment-utils";


const bindings = {
    parentEntityRef: "<",
};

const modes = {
    LIST: "LIST",
    VIEW: "VIEW"
};

const initialState = {
    mode: modes.LIST,
};


function controller($q, notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadAll = () => {
        const definitionsPromise = serviceBroker
            .loadViewData(CORE_API.AssessmentDefinitionStore.findByKind, [vm.parentEntityRef.kind]);

        const ratingsPromise = serviceBroker
            .loadViewData(CORE_API.AssessmentRatingStore.findForEntityReference, [vm.parentEntityRef], {force: true});

        const ratingSchemePromise = serviceBroker.loadViewData(CORE_API.RatingSchemeStore.findAll);

        $q.all([definitionsPromise, ratingsPromise, ratingSchemePromise])
            .then(([r1, r2, r3]) => ([r1.data, r2.data, r3.data]))
            .then(([definitions, ratings, ratingSchemes]) => {
                vm.assessmentDefinitions = definitions;
                vm.assessmentRatings = ratings;
                vm.ratingSchemes = ratingSchemes;

                vm.assessments = mkEnrichedAssessmentDefinitions(vm.assessmentDefinitions, vm.ratingSchemes, vm.assessmentRatings);
                if (vm.selectedAssessment) {
                    // re-find the selected assessment
                    vm.selectedAssessment = _.find(vm.assessments, a => a.definition.id === vm.selectedAssessment.definition.id);
                }
            });
    };


    vm.$onInit = () => {
        loadAll();
    };

    vm.onSelect = (def) => {
        vm.selectedAssessment = def;
        vm.mode = modes.VIEW;
    };

    vm.onClose = () => {
        vm.selectedAssessment = null;
        vm.mode = modes.LIST;
        loadAll();
    };

    vm.onRemove = (ctx) => {
        return serviceBroker
            .execute(CORE_API.AssessmentRatingStore.remove, [ vm.parentEntityRef, ctx.definition.id ])
            .then(() => {
                vm.onClose();
                notification.warning("Assessment removed");
            })
            .catch(e => notification.error("Failed to remove", e.message));
    };


    vm.onSave = (value, comments, ctx) => {
        const saveMethod = ctx.rating
            ? CORE_API.AssessmentRatingStore.update
            : CORE_API.AssessmentRatingStore.create;
        return serviceBroker
            .execute(saveMethod, [vm.parentEntityRef, ctx.definition.id, value, comments])
            .then(d => {
                loadAll();
                notification.success("Assessment saved");
            })
            .catch(e => notification.error("Failed to save", e.message));
    };


}


controller.$inject = [
    "$q",
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingSubSection"
};
