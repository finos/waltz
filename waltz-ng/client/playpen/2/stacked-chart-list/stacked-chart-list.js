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

    const finding = _
        .chain(stackedData)
        .map(d => {

            console.log(d);

            const firstStackedValues = _.find(d.stackData, s => s.s < earliest && s.e > earliest);
            const latestStackedValues = _.find(d.stackData, s => s.s < latest && s.e > latest);

            const before = (!_.isUndefined(firstStackedValues))
                ? firstStackedValues.values
                : [];
            const after = (!_.isUndefined(latestStackedValues))
                ? latestStackedValues.values
                : [];

            if(!_.isUndefined(firstStackedValues) && !_.isUndefined(latestStackedValues)){

                const newR = _.difference(latestStackedValues.values.r, firstStackedValues.values.r);
                const newA = _.difference(latestStackedValues.values.a, firstStackedValues.values.a);
                const newG = _.difference(latestStackedValues.values.g, firstStackedValues.values.g);
                const lostR = _.difference(firstStackedValues.values.r, latestStackedValues.values.r);
                const lostA = _.difference(firstStackedValues.values.a, latestStackedValues.values.a);
                const lostG = _.difference(firstStackedValues.values.g, latestStackedValues.values.g);

                console.log("new and lost");
                console.log( {newR, newA, newG, lostR, lostA, lostG});
            }

            // console.log({f: firstStackedValues, l: latestStackedValues});
            return { k: d.k, before, after }
        })
        .value();

    return finding;

    console.log({finding: finding});



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
        console.log(vm.dateExtent);
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