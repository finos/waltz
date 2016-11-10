import {initialiseData} from '../../common';
import {ragColorScale} from '../../common/colors';


const bindings = {
    appCapabilities: '<',
    apps: '<',
    sourceDataRatings: '<'
};


const initialState = {
    appCapabilities: [],
    apps: [],
    visibility: {
        ratingOverlay: false
    },
    query: '',
    pie: {
        selectedSegmentKey: null,
        data: [],
        config: null
    },
    columnDefs: [
        {
            field: 'app.name',
            name: 'Name',
            cellTemplate: '<div class="ui-grid-cell-contents"><a ui-sref="main.app.view ({ id: row.entity[\'app\'].id })"><span ng-bind="COL_FIELD"></span></a></div>'
        }, {
            field: 'app.assetCode',
            name: 'Asset Code'
        }, {
            field: 'rating',
            name: 'Rating',
            cellTemplate: '<div class="ui-grid-cell-contents"><waltz-rating-indicator-cell rating="COL_FIELD" label="COL_FIELD | toDisplayName:\'applicationRating\'"></waltz-rating-indicator-cell></div>'
        }, {
            field: 'description',
            name: 'Comment'
        },
    ]
};


const template = require('./capability-ratings-section.html');


function prepareAppCapabilities(appCapabilities = [], apps = []) {
    const appsById = _.keyBy(apps, "id");

    return _.map(
        appCapabilities,
        ac => Object.assign({}, ac, { app: appsById[ac.applicationId] }));
}


function preparePie(appCapabilities = [], displayNameService, onSelect = () => null) {
    const config = {
        colorProvider: (d) => ragColorScale(d.data.key),
        labelProvider: (d) => displayNameService.lookup('applicationRating', d.key),
        onSelect
    };
    const counts = _.countBy(appCapabilities, "rating");
    const data =  [
        { key: "R", count: counts['R'] || 0 },
        { key: "A", count: counts['A'] || 0 },
        { key: "G", count: counts['G'] || 0 },
        { key: "Z", count: counts['Z'] || 0 },
    ];

    return {
        config,
        data
    }
}


function controller(displayNameService) {
    let rawTableData = [];

    const vm = initialiseData(this, initialState);

    const onSelect = (d) => {
        vm.pie.selectedSegmentKey = d ? d.key : null;

        vm.tableData = d
            ? _.filter(rawTableData, r => r.rating === d.key)
            : rawTableData;
    };

    vm.$onChanges = () => {
        rawTableData = prepareAppCapabilities(vm.appCapabilities, vm.apps);
        vm.tableData = rawTableData;

        vm.pie = preparePie(vm.appCapabilities, displayNameService, onSelect);
    };

    vm.onGridInitialise = (d) => vm.exportData = d.export;
}


controller.$inject = ['WaltzDisplayNameService'];


const component = {
    template,
    bindings,
    controller
};

export default component;