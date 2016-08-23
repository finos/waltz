import _ from "lodash";
import {termSearch, perhaps} from "../../common";

const BINDINGS = {
    softwareCatalog: '=',
    servers: '=',
    databases: '='
};


const FIELDS_TO_SEARCH = {
    SOFTWARE: [
        'vendor',
        'name',
        'version',
        'maturityStatus'
    ]
};

function createDefaultTableOptions($animate, uiGridConstants, exportFileName = "export.csv") {
    return {
        columnDefs: [],
        data: [],
        enableGridMenu: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        enableSorting: true,
        exporterCsvFilename: exportFileName,
        exporterMenuPdf: false,
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        }
    };
}


function prepareServerGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        { field: 'hostname', displayName: 'Host' },
        { field: 'environment' },
        {
            field: 'virtual',
            displayName: 'Virtual',
            width: "5%",
            filter: {
                type: uiGridConstants.filter.SELECT,
                selectOptions: [
                    {value: 'true', label: 'Yes'},
                    {value: 'false', label: 'No'}
                ]
            },
            cellTemplate: '<div class="ui-grid-cell-contents"> <waltz-icon ng-if="COL_FIELD" name="check"></waltz-icon></div>'
        },
        { field: 'operatingSystem' },
        { field: 'operatingSystemVersion', displayName: 'Version' },
        { field: 'location' },
        { field: 'country' },
        { field: 'hardwareEndOfLifeDate', displayName: 'h/w End of Life' },
        { field: 'operatingSystemEndOfLifeDate', displayName: 'OS End of Life' }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "server.csv");
    return _.extend(baseTable, { columnDefs });
}


function prepareDatabaseGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        { field: 'instanceName', displayName: 'Instance' },
        { field: 'databaseName', displayName: 'Database' },
        { field: 'environment' },
        { field: 'dbmsVendor', displayName: 'Vendor' },
        { field: 'dbmsName', displayName: 'Product Name' },
        { field: 'dbmsVersion', displayName: 'Version' },
        { field: 'endOfLifeDate', displayName: 'End of Life' }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "database.csv");
    return _.extend(baseTable, { columnDefs });
}



function controller($animate, $scope, uiGridConstants) {

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
            if (qry) {
                vm.filteredSoftwareCatalog = {
                    usages: vm.softwareCatalog.usages,
                    packages: termSearch(packages, qry, FIELDS_TO_SEARCH.SOFTWARE)
                };
                vm.filteredServers = termSearch(servers, qry);
                vm.filteredDatabases = termSearch(databases, qry);
            } else {
                vm.filteredSoftwareCatalog = vm.softwareCatalog;
                vm.filteredServers = servers;
                vm.filteredDatabases = databases;
            }
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


    vm.serverGridOptions = prepareServerGridOptions($animate, uiGridConstants);
    vm.databaseGridOptions = prepareDatabaseGridOptions($animate, uiGridConstants);


    vm.hasAnyData = () => {
        const hasSoftware = perhaps(() => vm.softwareCatalog.packages.length > 0, false);
        const hasServers = perhaps(() => vm.servers.length > 0, false);
        const hasDatabases = false;

        return hasSoftware || hasServers || hasDatabases;
    }

}


controller.$inject = [
    '$animate',
    '$scope',
    'uiGridConstants'
];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./technology-section.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
};


export default () => directive;
