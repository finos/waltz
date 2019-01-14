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


function controller($q, serviceBroker) {
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
                console.log("after reload", vm.assessments);
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
        console.log("REMOVING VALUE", ctx);
        return serviceBroker
            .execute(CORE_API.AssessmentRatingStore.remove, [vm.parentEntityRef, ctx.definition.id])
            .then(() => vm.onClose());
    };


    vm.onSave = (value, comments, ctx) => {
        console.log({value: value, comments: comments, ctx: ctx});
        const saveMethod = ctx.rating
            ? CORE_API.AssessmentRatingStore.update
            : CORE_API.AssessmentRatingStore.create;
        return serviceBroker
            .execute(saveMethod, [vm.parentEntityRef, ctx.definition.id, value, comments])
            .then(d => console.log("saved", vm.assessments));
    };


}


controller.$inject = [
    "$q",
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
