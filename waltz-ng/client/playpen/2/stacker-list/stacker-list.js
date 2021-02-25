import template from "./stacker-list.html";
import {initialiseData} from "../../../common";


const bindings = { rawData: "<"};

const initialState = {
    stacks: []
};

function removeFromAcc(id, acc) {
    return {
        a: _.without(acc.a, id),
        r: _.without(acc.r, id),
        g: _.without(acc.g, id)
    };
}


function categorizeBucket(acc, d) {
    const accNext = removeFromAcc(d.id_a, acc);
    if (d.milestone_name === 'Launch Date') {
        accNext.g.push(d.id_a);
        return accNext;
    }
    if (d.milestone_name === 'Hold Date') {
        accNext.a.push(d.id_a);
        return accNext;
    }
    if (d.milestone_name === 'Sell Date') {
        accNext.r.push(d.id_a);
        return accNext;
    }
    return accNext;
}


function toStackData(data) {
    const groupedByDate = _
        .chain(data)
        .groupBy("milestone_date")
        .map((v, k) => ({k: Date.parse(k), v}))
        .orderBy(d => d.k)
        .value();

    let xs = [];
    let acc = {r: [], a: [], g: []};
    _.each(
        groupedByDate,
        d => {
            acc = _.reduce(d.v, categorizeBucket, acc);
            xs.push({k: d.k, values: acc});
        })
    return xs;
}


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