import {initialiseData} from "../../common";
import {authoritativeRatingColorScale} from "../../common/colors";
import _ from "lodash";


const bindings = {
    data: '<',
    dataTypes: '<',
    typeId: '<'
};


const initialState = {
    flowData: {},
    filterOptions: {
        showPrimary: true,
        showSecondary: true,
        showDiscouraged: false,
        showNoOpinion: false
    }
};


const template = require('./rated-flow-boingy-graph.html');


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


const buildGraphTweakers = (decorators = [],
                            onAppSelect) => {
    const decoratorsByFlowId = _.keyBy(decorators, 'dataFlowId');

    const getRatingForLink = (link) => {
        const decorator = decoratorsByFlowId[link.data.id];
        return decorator
            ? decorator.rating
            : 'NO_OPINION';
    };

    return {
        node : {
            enter: (selection) => {
                selection
                    .on('click.fixer', app => app.fixed = true)
                    .on('click.appSelect', onAppSelect)
                    .on('dblclick.fixer', app => app.fixed = false)
            },
            exit: _.identity,
            update: _.identity
        },
        link : {
            enter: (selection) => {
                selection
                    .attr({
                        stroke: (d) => {
                            const rating = getRatingForLink(d);
                            return authoritativeRatingColorScale(rating);
                        },
                        'marker-end': d => {
                            const rating = getRatingForLink(d);
                            return `url(#arrowhead-${rating})`;
                        }

                    });
            },
            exit: _.identity,
            update: _.identity
        }
    };
};


function mkKeepDecoratorFn(filterOptions = {}) {
    return (decorator) => {
        const rating = decorator.rating;
        switch (rating) {
            case 'PRIMARY':
                return filterOptions.showPrimary;
            case 'SECONDARY':
                return filterOptions.showSecondary;
            case 'DISCOURAGED':
                return filterOptions.showDiscouraged;
            case 'NO_OPINION':
                return filterOptions.showNoOpinion;
        }
    };
}


function filterDecorators(decorators =[],
                          filterOptions) {
    return _.filter(decorators, mkKeepDecoratorFn(filterOptions));
}


function filterFlows(flows = [],
                     decorators = []) {
    const flowIds = _.map(decorators, 'dataFlowId');
    return _.filter(flows, f => _.includes(flowIds, f.id));

}


function filterData(flows = [],
                    decorators = [],
                    filterOptions) {
    const filteredDecorators = filterDecorators(decorators, filterOptions);
    const filteredFlows = filterFlows(flows, filteredDecorators);
    const filteredEntities = calculateEntities(filteredFlows);
    return {
        entities: filteredEntities,
        flows: filteredFlows,
        decorators: filteredDecorators
    };
}


function controller($scope) {
    const vm = initialiseData(this, initialState);
    const onAppSelect = (app) => $scope.$applyAsync(() => vm.selectedApp = app);

    vm.filterChanged = () => {
        const filteredData = filterData(
            vm.rawFlows,
            vm.rawDecorators,
            vm.filterOptions);
        vm.flowData = filteredData;
    };

    vm.showAll = () => {
        vm.filterOptions = {
            showPrimary: true,
            showSecondary: true,
            showDiscouraged: true,
            showNoOpinion: true
        };
        vm.filterChanged();
    };

    vm.$onChanges = () => {
        const rawFlows = vm.data
            ? vm.data.flows
            : [];
        const rawDecorators = vm.data
            ? vm.data.decorators
            : [];

        vm.graphTweakers = buildGraphTweakers(
            rawDecorators,
            onAppSelect);

        vm.rawFlows = rawFlows;
        vm.rawDecorators = rawDecorators;
        vm.filterChanged();
    };

    vm.refocusApp = app => {
        onAppSelect(app);
    };
}


controller.$inject = [
    '$scope'
];


const component = {
    bindings,
    template,
    controller
};


export default component;