import template from "./stacker-list.html";
import {initialiseData} from "../../../common";
import {toStackData} from "../milestone-utils";


const bindings = { rawData: "<"};

const initialState = {
    stacks: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const groupedByVenue = _.groupBy(vm.rawData, d => d.id_b);

        vm.stacks = _.map(
            groupedByVenue,
            (v, k) => ({k, stackData: toStackData(v)}));
    }
}

controller.$inject = [];

const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzStackerList",
    component
};