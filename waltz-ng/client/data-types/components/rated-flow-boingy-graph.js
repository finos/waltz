import {initialiseData} from "../../common";
import {authoritativeRatingColorScale} from "../../common/colors";
import _ from "lodash";


const bindings = {
    data: '<',
    typeId: '<'
};


const defaultFilterOptions = {
    rating: 'PRIMARY',
};


const initialState = {
    flowData: {},
    filteredFlowData: {},
    selectedRating: defaultFilterOptions.rating
};


const template = require('./rated-flow-boingy-graph.html');


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


const buildGraphTweakers = (decorators = [], onAppSelect) => {
    const decoratorsByFlowId = _.keyBy(decorators, 'dataFlowId');

    return {
        node : {
            enter: (selection) => {
                selection
                    .on('click.fixer', app => app.fixed = true)
                    .on('click.appSelect', onAppSelect)
                    .on('dblclick', app => app.fixed = false)
            },
            exit: _.identity,
            update: _.identity
        },
        link : {
            enter: (selection) => {
                selection
                    .attr({
                        stroke: (d) => {
                            const decorator = decoratorsByFlowId[d.data.id];
                            const rating = decorator
                                ? decorator.rating
                                : 'NO_OPINION';
                            const colour = authoritativeRatingColorScale(rating);
                            return colour;

                        },
                        'marker-end': d => {
                            const decorator = decoratorsByFlowId[d.data.id];
                            const rating = decorator
                                ? decorator.rating
                                : 'NO_OPINION';
                            return `url(#arrowhead-${rating})`;
                        }

                    });
            },
            exit: _.identity,
            update: _.identity
        }
    };
};


function prepareData(dataTypeId, flows = [], decorators = [], onAppSelect, filterOptions = defaultFilterOptions) {
    const filteredDecorators = _.filter(decorators, (d) => {
        if(filterOptions.rating === 'ALL') return true;
        return d.rating === filterOptions.rating;
    })

    const graphTweakers = buildGraphTweakers(filteredDecorators, onAppSelect);

    const filteredFlowIds = _.map(filteredDecorators, 'dataFlowId');

    const filteredFlows = _.filter(flows, f => _.includes(filteredFlowIds, f.id));
    const flowData = {
        entities: calculateEntities(filteredFlows),
        flows: filteredFlows,
        decorators: filteredDecorators
    };

    return { graphTweakers, flowData };
}


function controller($scope) {
    const vm = initialiseData(this, initialState);
    const onAppSelect = (app) => vm.selectedApp = app;

    const filterData = (options = defaultFilterOptions) => {
        const preparedData = prepareData(vm.typeId, vm.rawFlows, vm.rawDecorators, onAppSelect, options);
        return preparedData;
    };

    vm.$onChanges = (changes) => {
        const rawFlows = vm.data ? vm.data.flows : [];
        const rawDecorators = vm.data ? vm.data.decorators : [];

        vm.rawFlows = rawFlows;
        vm.rawDecorators = rawDecorators;
        Object.assign(vm, filterData(defaultFilterOptions));
    }

    vm.filterChanged = (filterOptions) => {
        Object.assign(vm, filterData(filterOptions));
    };

    $scope.$watch(
        '$ctrl.selectedRating',
        (rating = 'ALL') => {
            const filterOptions = {
                rating,
            }
            vm.filterChanged(filterOptions)
        }
    );
}

controller.$inject = ['$scope'];

const component = {
    bindings,
    template,
    controller
};


export default component;