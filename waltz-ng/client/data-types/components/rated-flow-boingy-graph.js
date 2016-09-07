import { authoritativeRatingColorScale } from '../../common/colors'
import _ from 'lodash';


const bindings = {
    data: '<',
    typeId: '<'
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
                            return authoritativeRatingColorScale(rating);
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


function prepareData(dataTypeId, flows = [], decorators = [], onAppSelect) {
    const relevantDecorators = _.filter(
        decorators,
        d => d.decoratorEntity.id === dataTypeId && d.decoratorEntity.kind === 'DATA_TYPE');

    const relevantFlowIds = _.chain(relevantDecorators)
        .map(d => d.dataFlowId)
        .uniq()
        .value();

    const relevantFlows = _.filter(
        flows,
        f => _.includes(relevantFlowIds, f.id));

    const graphTweakers = buildGraphTweakers(relevantDecorators, onAppSelect);

    const flowData = {
        entities: calculateEntities(relevantFlows),
        flows: relevantFlows,
        decorators: relevantDecorators
    };


    return { graphTweakers, flowData };
}


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        const flows = vm.data ? vm.data.flows : [];
        const decorators = vm.data ? vm.data.decorators : [];
        const onAppSelect = (app) => vm.selectedApp = app;

        Object.assign(vm, prepareData(vm.typeId, flows, decorators, onAppSelect));
    }
}

controller.$inject = [];

const component = {
    bindings,
    template,
    controller
};


export default component;