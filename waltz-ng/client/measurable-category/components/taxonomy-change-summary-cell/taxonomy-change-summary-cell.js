import template from "./taxonomy-change-summary-cell.html";
import {initialiseData} from "../../../common";


const initialState = {};

const bindings = {
    change: "<"
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};

export default {
    id: "waltzTaxonomyChangeSummaryCell",
    component
};
