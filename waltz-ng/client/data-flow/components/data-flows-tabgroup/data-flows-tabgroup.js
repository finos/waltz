import _ from "lodash";
import {authoritativeRatingBackgroundColorScale} from "../../../common/colors";


const bindings = {
    flowData: '<',
    applications: '<',
    onLoadDetail: '<',
    options: '<',  // { graphTweakers ... }
    optionsVisible: '<',
    onTabChange: '<'
};


const defaultFilterOptions = {
    type: 'ALL',
    scope: 'INTRA'
};


const defaultOptions = {
    graphTweakers: {
        node : {
            enter: (selection) => console.log("default graphTweaker.node.entry, selection: ", selection),
        },
        link : {
            enter: (selection) => selection.attr('stroke', 'red')
        }
    }
};


const initialState = {
    applications: [],
    selectedApplication: null,
    boingyEverShown: false,
    dataTypes: [],
    flowData: null,
    filterOptions: defaultFilterOptions,
    onLoadDetail: () => console.log("No onLoadDetail provided for data-flows-tabgroup"),
    options: defaultOptions,
    optionsVisible: false,
    onTabChange: () => console.log("No onTabChange provided for data-flows-tabgroup"),
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


function mkScopeFilterFn(appIds = [], scope = 'INTRA') {
    return (f) => {
        switch (scope) {
            case "INTRA":
                return _.includes(appIds, f.target.id) && _.includes(appIds, f.source.id);
            case "ALL":
                return true;
            case "INBOUND":
                return _.includes(appIds, f.target.id);
            case "OUTBOUND":
                return _.includes(appIds, f.source.id);
        }
    };
}


function mkTypeFilterFn(decorators = []) {
    const flowIds = _.chain(decorators)
        .map('dataFlowId')
        .uniq()
        .value();
    return f => _.includes(flowIds, f.id);
}


function buildFlowFilter(filterOptions = defaultFilterOptions,
                         appIds = [],
                         flowDecorators = []) {
    const typeFilterFn = mkTypeFilterFn(flowDecorators);
    const scopeFilterFn = mkScopeFilterFn(appIds, filterOptions.scope);
    return f => typeFilterFn(f) && scopeFilterFn(f);
}


function buildDecoratorFilter(options = defaultFilterOptions) {
    return d => {
        const isDataType = d.decoratorEntity.kind === 'DATA_TYPE';
        const matchesDataType = options.type === 'ALL' || d.decoratorEntity.id === Number(options.type);
        return isDataType && matchesDataType;
    };
}


function calculateFlowData(allFlows = [],
                           applications = [],
                           allDecorators = [],
                           filterOptions = defaultFilterOptions) {
    // note order is important.  We need to find decorators first
    const decoratorFilterFn = buildDecoratorFilter(filterOptions);
    const decorators = _.filter(allDecorators, decoratorFilterFn);

    const appIds = _.map(applications, "id");
    const flowFilterFn = buildFlowFilter(filterOptions, appIds, decorators);
    const flows = _.filter(allFlows, flowFilterFn);

    const entities = calculateEntities(flows);

    return {flows, entities, decorators};
}


function getDataTypeIds(decorators = []) {
    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
        .map('decoratorEntity.id')
        .uniq()
        .value();
}


function prepareGraphTweakers(dataFlowUtilityService,
                              applications = [],
                              decorators = [],
                              appSelectFn = (d) => console.log("dftg: no appSelectFn given", d))
{
    const appIds = _.map(applications, 'id');
    const tweakers = dataFlowUtilityService.buildGraphTweakers(appIds, decorators);

    const dfltNodeEnter = tweakers.node.enter;
    const nodeEnter = selection => selection
        .on('click.app-select', appSelectFn)
        .call(dfltNodeEnter);

    tweakers.node.enter = nodeEnter;
    return tweakers;
}


function mkEntityNameCell(entityDataObjectField, valueField, columnHeading, entityNavViewName) {
    return {
        field: valueField,
        displayName: columnHeading,
        cellTemplate: `<div class="ui-grid-cell-contents">\n<a ui-sref="main.${entityNavViewName}.view ({ id: row.entity[\'${entityDataObjectField}\'][\'id\']})" ng-bind="COL_FIELD">\n</a>\n</div>`
    };
}


function groupDecoratorsByFlowId(decorators = [], displayNameService) {
    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
        .map(dc => _.assign(
                    {},
                    {
                        dataFlowId: dc.dataFlowId,
                        dataType: {
                            id: dc.decoratorEntity.id,
                            name: displayNameService.lookup('dataType', dc.decoratorEntity.id)
                        },
                        authSourceRating: dc.rating
                    }))
        .groupBy('dataFlowId')
        .value();
}


function prepareGridData(flows = [], decorators = [], displayNameService) {
    const groupedDecorators = groupDecoratorsByFlowId(decorators, displayNameService);
    return _.flatMap(flows,
                    f => _.map(groupedDecorators[f.id],
                                dc => _.assign(
                                        {},
                                        f,
                                        { dataType: dc.dataType },
                                        { authSourceRating: dc.authSourceRating },
                                        { rowStyle: { 'background-color': authoritativeRatingBackgroundColorScale(dc.authSourceRating) } })));
}


function prepareGridOptions(filteredFlowData = {}, $animate, uiGridConstants, displayNameService) {
    const gridData = prepareGridData(filteredFlowData.flows, filteredFlowData.decorators, displayNameService);

    const columnDefs = [
        mkEntityNameCell('source', 'source.name', 'Source', 'app'),
        mkEntityNameCell('target', 'target.name', 'Target', 'app'),
        mkEntityNameCell('dataType', 'dataType.name', 'Data Type', 'data-type'),
        { field: 'authSourceRating', displayName: 'Source Rating', cellFilter: 'toDisplayName:"rating"' }
    ];

    return {
        columnDefs,
        data: gridData,
        enableGridMenu: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        enableSorting: true,
        exporterCsvFilename: "flows.csv",
        exporterMenuPdf: false,
        rowTemplate: '<div ng-style="row.entity.rowStyle"><div ng-repeat="col in colContainer.renderedColumns track by col.colDef.name" class="ui-grid-cell" ui-grid-cell></div></div>',
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        }
    };
}


function controller($animate,
                    $scope,
                    dataFlowUtilityService,
                    uiGridConstants,
                    displayNameService) {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = () => {
        if (vm.flowData) {
            vm.dataTypes = getDataTypeIds(vm.flowData.decorators);
        }
        vm.filterChanged();
    };

    vm.filterChanged = (filterOptions = vm.filterOptions) => {
        vm.filterOptions = filterOptions;

        if (! vm.flowData) return;

        vm.filteredFlowData = calculateFlowData(
            vm.flowData.flows,
            vm.applications,
            vm.flowData.decorators,
            filterOptions);

        vm.graphTweakers = prepareGraphTweakers(
            dataFlowUtilityService,
            vm.applications,
            vm.filteredFlowData.decorators,
            app => $scope.$applyAsync(() => vm.selectedApplication = app));

        vm.gridOptions = prepareGridOptions(vm.filteredFlowData, $animate, uiGridConstants, displayNameService);
    };

    vm.loadDetail = () => {
        if (vm.onLoadDetail) {
            vm.onLoadDetail();
        } else {
            console.log("No handler for detail provided ('on-load-detail')");
        }
    };

    vm.tabSelected = (tabName, index) => {
        if(index > 0) {
            vm.loadDetail();
        }
        if(index === 1) {
            vm.boingyEverShown = true;
        }
        vm.onTabChange(tabName, index);
    };
}


controller.$inject = [
    '$animate',
    '$scope',
    'DataFlowUtilityService',
    'uiGridConstants',
    'WaltzDisplayNameService'
];


const component = {
    controller,
    bindings,
    template: require('./data-flows-tabgroup.html')
};


export default component;
