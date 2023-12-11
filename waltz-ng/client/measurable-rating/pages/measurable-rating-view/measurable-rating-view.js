import MeasurableRatingOverview
    from "../../svelte/MeasurableRatingOverview.svelte";
import {initialiseData} from "../../../common";
import template from "./measurable-rating-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";

const initialState = {
    MeasurableRatingOverview
};


const addToHistory = (historyStore, rating) => {
    if (! rating) { return; }
    historyStore.put(
        rating.name,
        "MEASURABLE_RATING",
        "main.measurable-rating.view",
        { id: rating.id });
};

function controller($stateParams, historyStore, serviceBroker) {

    const vm = initialiseData(this, initialState);

    const ratingId = $stateParams.id;

    vm.parentEntityRef = {
        id: ratingId,
        kind: "MEASURABLE_RATING",
    };

    serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingStore.getViewById,
            [ratingId])
        .then(r => {
            const view = r.data;

            vm.parentEntityRef = Object.assign(
                {},
                vm.parentEntityRef,
                {name: `Measurable Rating: ${_.get(view, ["measurable", "name"], "Unknown measurable")} for ${view?.measurableRating.entityReference.name}`
            });

            addToHistory(historyStore, vm.parentEntityRef);
        });
}


controller.$inject = [
    "$stateParams",
    "HistoryStore",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
};