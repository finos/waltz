import _ from "lodash";
import {termSearch, perhaps} from "../../common";

const BINDINGS = {
    softwareCatalog: '=',
    servers: '=',
    databases: '='
};


const FIELDS_TO_SEARCH = {
    SOFTWARE: ['vendor', 'name', 'version', 'maturityStatus']
};

function createDefaultTableOptions(uiGridConstants) {
    return {
        enableSorting: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        columnDefs: [],
        data: []
    };
}


function prepareServerGridOptions(uiGridConstants) {

    const columnDefs = [
        {field: 'hostname', displayName: 'Host'},
        {field: 'environment'},
        {
            field: 'virtual',
            displayName: 'Virtual',
            filter: {
                type: uiGridConstants.filter.SELECT,
                selectOptions: [
                    {value: 'true', label: 'Yes'},
                    {value: 'false', label: 'No'}
                ]
            },
            cellTemplate: '<div class="ui-grid-cell-contents"> <waltz-icon ng-if="COL_FIELD" name="check"></waltz-icon></div>'
        },
        {field: 'operatingSystem'},
        {field: 'operatingSystemVersion', displayName: 'Version'},
        {field: 'location'},
        {field: 'country'}
    ];

    const baseTable = createDefaultTableOptions(uiGridConstants);
    return _.extend(baseTable, { columnDefs });
}


function prepareDatabaseGridOptions(uiGridConstants) {

    const columnDefs = [
        { field: 'instanceName', displayName: 'Instance' },
        { field: 'databaseName', displayName: 'Database' },
        { field: 'environment' },
        { field: 'dbmsVendor', displayName: 'Vendor' },
        { field: 'dbmsName', displayName: 'Product Name' },
        { field: 'dbmsVersion', displayName: 'Version' }
    ];

    const baseTable = createDefaultTableOptions(uiGridConstants);
    return _.extend(baseTable, { columnDefs });
}



function controller($scope, uiGridConstants) {

    const vm = this;

    const watchExpressionForQueries = [
        'ctrl.qry',
        'ctrl.softwareCatalog.packages',
        'ctrl.servers',
        'ctrl.databases'
    ];

    $scope.$watchGroup(
        watchExpressionForQueries,
        ([qry, packages = [], servers = [], databases = []]) => {
            vm.filteredSoftwarePackages = qry
                ? termSearch(packages, qry, FIELDS_TO_SEARCH.SOFTWARE)
                : packages;

            vm.filteredServers = qry
                ? termSearch(servers, qry)
                : servers;

            vm.filteredDatabases =  qry
                ? termSearch(databases, qry)
                : databases;
        }
    );


    $scope.$watch(
        'ctrl.softwareCatalog.usages',
        (usages = []) => {
            vm.softwareUsages = _.chain(usages)
                .groupBy('provenance')
                .value();
        }
    );


    $scope.$watch(
        'ctrl.servers',
        servers => vm.serverGridOptions.data = servers
    );

    $scope.$watch(
        'ctrl.databases',
        databases => vm.databaseGridOptions.data = databases
    );


    vm.serverGridOptions = prepareServerGridOptions(uiGridConstants);
    vm.databaseGridOptions = prepareDatabaseGridOptions(uiGridConstants);


    vm.hasAnyData = () => {
        const hasSoftware = perhaps(() => vm.softwareCatalog.packages.length > 0, false);
        const hasServers = perhaps(() => vm.servers.length > 0, false);
        const hasDatabases = false;

        return hasSoftware || hasServers || hasDatabases;
    }




}

controller.$inject = [ '$scope', 'uiGridConstants' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./technology-section.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
