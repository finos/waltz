import template from "./stacked-chart-list.html";
import {initialiseData} from "../../../common";
import {calcDateExtent, toStackData} from "../milestone-utils";
import * as _ from "lodash";


const bindings = { rawData: "<"};

const initialState = {
    stacks: [],
    dateExtent: null,
    selectedDate: null,
    lastSelectedDate: null,
    showSelectBoxes: false
};


const margin = {
    left: 20,
    right: 20,
    top: 10,
    bottom: 10
};


const findComparison = (stackedData, lowestDate, highestDate) => {

    console.log({st: stackedData});

    const earliest = Date.parse(lowestDate);
    const latest = Date.parse(highestDate);

    return _
        .chain(stackedData)
        .map(d => {

            const firstStackedValues = _.find(d.stackData, s => s.s < earliest && s.e > earliest);
            const latestStackedValues = _.find(d.stackData, s => s.s < latest && (s.e > latest || _.isUndefined(s.e)));

            const empty = {a: [], g: [], r:[] };

            const before = (!_.isUndefined(firstStackedValues))
                ? firstStackedValues.values
                : empty;
            const after = (!_.isUndefined(latestStackedValues))
                ? latestStackedValues.values
                : empty;

            console.log(before);


            const newR = _.difference(after.r, before.r);
            const newA = _.difference(after.a, before.a);
            const newG = _.difference(after.g, before.g);
            const lostR = _.difference(before.r, after.r);
            const lostA = _.difference(before.a, after.a);
            const lostG = _.difference(before.g, after.g);

            console.log("new and lost");
            console.log( {newR, newA, newG, lostR, lostA, lostG});
            return { k: d.k, before, after, diff: {newR, newA, newG, lostR, lostA, lostG} }

        })
        .value();
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        const groupedByVenue = _.groupBy(vm.rawData, d => d.id_b);

        vm.stacks = _.map(
            groupedByVenue,
            (v, k) => ({k, stackData: toStackData(v)}));

        vm.dateExtent = calcDateExtent(vm.rawData, 30 * 12);

        vm.onSelect = (data, date) => {

            if(vm.selectedDate == null){
                vm.selectedDate = date;
            } else {
                vm.lastSelectedDate = vm.selectedDate;
                vm.selectedDate = date;

                const dateArray = [vm.lastSelectedDate, vm.selectedDate];

                vm.highestDate = _.max(dateArray);
                vm.lowestDate = _.min(dateArray);

                vm.reportChanges = findComparison(vm.stacks, vm.lowestDate, vm.highestDate);

            }

        };
    }
}

controller.$inject = [];

const component = {
    bindings,
    controller,
    template
};


export default {
    id: "waltzStackedChartList",
    component
};