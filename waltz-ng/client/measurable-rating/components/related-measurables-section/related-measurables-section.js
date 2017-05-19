import _ from 'lodash';
import {initialiseData} from '../../../common';


/**
 * @name waltz-related-measurables-explorer
 *
 * @description
 * This component ...
 */


const bindings = {
    parentEntityRef: '<',
    stats: '<',
    measurables: '<',
    categories: '<'
};


const initialState = {};


const template = require('./related-measurables-section.html');


function calcRelatedMeasurables(ratingTallies = [], allMeasurables = []) {
    const relatedMeasurableIds = _.map(ratingTallies, 'id');
    const measurablesById = _.keyBy(allMeasurables, 'id');

    return _
        .chain(allMeasurables)
        .filter(m => _.includes(relatedMeasurableIds, m.id))
        .reduce(
            (acc, m) => {
                let ptr = m;
                while(ptr) {
                    acc[ptr.id] = ptr;
                    ptr = measurablesById[ptr.parentId];
                }
                return acc;
            },
            {})
        .values()
        .value();
}


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        vm.relatedMeasurables = calcRelatedMeasurables(vm.stats, vm.measurables);
    };

    vm.onSelect = m => vm.selectedMeasurable = m;

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;