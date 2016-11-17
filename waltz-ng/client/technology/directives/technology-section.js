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


const EOL_CELL_TEMPLATE = '<div class="ui-grid-cell-contents"> <waltz-icon ng-if="COL_FIELD" name="power-off"></waltz-icon></div>';


function mkBooleanColumnFilter(uiGridConstants) {
    return {
        type: uiGridConstants.filter.SELECT,
        selectOptions: [
            {value: 'true', label: 'Yes'},
            {value: 'false', label: 'No'}
        ]
    };
}


function isEndOfLife(endOfLifeStatus) {
    return endOfLifeStatus === 'END_OF_LIFE';
}


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
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: '<div class="ui-grid-cell-contents"> <waltz-icon ng-if="COL_FIELD" name="check"></waltz-icon></div>'
        },
        { field: 'operatingSystem', displayName: 'OS' },
        { field: 'operatingSystemVersion', displayName: 'Version' },
        { field: 'location' },
        { field: 'country' },
        {
            field: 'isHwEndOfLife',
            displayName: 'h/w EOL',
            width: "6%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: EOL_CELL_TEMPLATE
        },
        { field: 'hardwareEndOfLifeDate', displayName: 'h/w EOL On' },
        {
            field: 'isOperatingSystemEndOfLife',
            displayName: 'OS EOL',
            width: "6%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: EOL_CELL_TEMPLATE
        },
        { field: 'operatingSystemEndOfLifeDate', displayName: 'OS EOL On' },
        {
            field: 'lifecycleStatus',
            displayName: 'Lifecycle',
            cellFilter: "toDisplayName:'lifecycleStatus'"
        }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "server.csv");
    return _.extend(baseTable, {
        columnDefs,
        rowTemplate: '<div ng-class="{\'bg-danger\': row.entity.isHwEndOfLife || row.entity.isOperatingSystemEndOfLife}"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>'
    });
}


function prepareDatabaseGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        { field: 'instanceName', displayName: 'Instance' },
        { field: 'databaseName', displayName: 'Database' },
        { field: 'environment' },
        { field: 'dbmsVendor', displayName: 'Vendor' },
        { field: 'dbmsName', displayName: 'Product Name' },
        { field: 'dbmsVersion', displayName: 'Version' },
        {
            field: 'isEndOfLife',
            displayName: 'EOL',
            width: "5%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: EOL_CELL_TEMPLATE
        },
        { field: 'endOfLifeDate', displayName: 'EOL On' },
        {
            field: 'lifecycleStatus',
            displayName: 'Lifecycle',
            cellFilter: "toDisplayName:'lifecycleStatus'"
        }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "database.csv");
    return _.extend(baseTable, {
        columnDefs,
        rowTemplate: '<div ng-class="{\'bg-danger\': row.entity.isEndOfLife}"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>'
    });
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
        servers => {
            _.forEach(servers,
                (svr) => Object.assign(svr, {
                    "isHwEndOfLife": isEndOfLife(svr.hardwareEndOfLifeStatus),
                    "isOperatingSystemEndOfLife": isEndOfLife(svr.operatingSystemEndOfLifeStatus)
                })
            );
            vm.serverGridOptions.data = servers;
        }
    );

    $scope.$watch(
        'ctrl.databases',
        databases => {
            _.forEach(databases,
                (db) => Object.assign(db, {
                    "isEndOfLife": isEndOfLife(db.endOfLifeStatus)
                })
            );
            vm.databaseGridOptions.data = databases;
        }
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
