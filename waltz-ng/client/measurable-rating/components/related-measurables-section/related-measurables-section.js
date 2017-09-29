import _ from 'lodash';
import {initialiseData} from '../../../common';
import {CORE_API} from "../../../common/services/core-api-utils";


/**
 * @name waltz-related-measurables-explorer
 *
 * @description
 * This component ...
 */
const bindings = {
    parentEntityRef: '<'
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


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {

        if (! vm.parentEntityRef) {
            return;
        }

        const categoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => vm.categories = r.data);

        const measurablesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurables = r.data);

        const statsPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.statsForRelatedMeasurables,
                [ vm.parentEntityRef.id ])
            .then(r => vm.stats = r.data);

        const promises = [
            categoriesPromise,
            measurablesPromise,
            statsPromise
        ];

        $q.all(promises)
          .then(() => vm.relatedMeasurables = calcRelatedMeasurables(vm.stats, vm.measurables));

    };

    vm.onSelect = (m) => vm.selectedMeasurable = m;

}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default component;