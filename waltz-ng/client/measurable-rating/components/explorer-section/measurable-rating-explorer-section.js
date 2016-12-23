import _ from 'lodash';
import {initialiseData, mkLinkGridCell} from '../../../common';
import {ragColorScale} from '../../../common/colors';
import {investmentRatingNames, measurableKindNames} from '../../../common/services/display-names';

/**
 * @name waltz-measurable-rating-explorer-section
 *
 * @description
 * This component provides an overview pie chart showing a breakdown of measurable
 * ratings by their rated values.  It also provides a detail table showing
 * all the ratings and their associated applications.
 */


const bindings = {
    applications: '<',
    measurableKind: '<',
    measurables: '<',
    ratings: '<',
    sourceDataRatings: '<'
};


const initialState = {
    query: '',
    pie: null,
    visibility: {
        ratingOverlay: false
    }
};


const template = require('./measurable-rating-explorer-section.html');


function preparePie(ratings = [], onSelect) {
    const counts = _.countBy(ratings, 'rating');
    const data = [
        { key: "R", count: counts['R'] || 0 },
        { key: "A", count: counts['A'] || 0 },
        { key: "G", count: counts['G'] || 0 },
        { key: "Z", count: counts['Z'] || 0 },
    ];

    return {
        selectedSegmentKey: null,
        data,
        config: {
            onSelect,
            colorProvider: (d) => ragColorScale(d.data.key),
            labelProvider: (d) => investmentRatingNames[d.key] || d.key
        }
    };
}


function prepareTableData(ratings = [],
                          applications = [],
                          measurables = []) {
    const measurablesById = _.keyBy(measurables, 'id');
    const applicationsById = _.keyBy(applications, 'id');
    return _.chain(ratings)
        .map(r => {
            return {
                rating: r,
                ratingName: investmentRatingNames[r.rating] || r.rating,
                measurable: measurablesById[r.measurableId],
                application: applicationsById[r.entityReference.id]
            };
        })
        .value();
}


function prepareColumnDefs(measurableKind, measurables) {
     // We only want to show the measurable column if there are multiple measurables to
     // differentiate between.

    const initialCols = [
        mkLinkGridCell('Name', 'application.name', 'application.id', 'main.app.view'),
        {
            field: 'application.assetCode',
            name: 'Asset Code'
        },
        {
            field: 'ratingName',
            name: 'Rating',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-rating-indicator-cell rating="row.entity.rating.rating" label="COL_FIELD"></waltz-rating-indicator-cell></div>'
        }
    ];

    const measurableCols = measurables.length > 1
        ? [ { field: 'measurable.name', name: measurableKindNames[measurableKind] } ]
        : [];

    const finalCols = [{
        field: 'rating.description',
        name: 'Comment'
    }];

    return [].concat(initialCols, measurableCols, finalCols);
}


function controller() {
    const vm = initialiseData(this, initialState);

    const onSelect = (d) => {
        vm.pie.selectedSegmentKey = d ? d.key : null;

        const ratings = d
                ? _.filter(vm.ratings, r => r.rating === d.key)
                : vm.ratings;

        vm.tableData = prepareTableData(
            ratings,
            vm.applications,
            vm.measurables);
    };

    vm.$onChanges = (c) => {
        if (vm.measurableKind && vm.measurables) {
            vm.columnDefs = prepareColumnDefs(vm.measurableKind, vm.measurables);
        }

        if (c.ratings) {
            vm.pie = preparePie(vm.ratings, onSelect);
        }

        if (vm.ratings && vm.applications && vm.measurables) {
            vm.tableData = prepareTableData(vm.ratings, vm.applications, vm.measurables);
        }
    };

    vm.onGridInitialise = (cfg) =>
        vm.exportData = () => cfg.exportFn("measurable-ratings.csv");

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;