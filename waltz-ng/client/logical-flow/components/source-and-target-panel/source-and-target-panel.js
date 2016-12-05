import _ from 'lodash';
import {nest} from 'd3-collection';
import {event} from 'd3-selection';
import {initialiseData} from '../../../common'


const bindings = {
    entityRef: '<',
    logicalFlows: '<',
    decorators: '<',
    physicalFlows: '<',
    physicalSpecifications: '<'
};


const initialState = {
    filteredFlowData: {
        selectedTypeId: 0,
        flows: [],
        decorators: []
    }
};


const template = require('./source-and-target-panel.html');


function calcPhysicalFlows(physicalFlows, specifications, appRef, type, entityRef) {
    const specs = type === 'source' ? specifications.consumes : specifications.produces;
    const specsById = _.keyBy(specs, 'id');

    return _.chain(physicalFlows)
        .map(pf => Object.assign({}, pf, { specification: specsById[pf.specificationId] }))
        .filter(pf => type === 'source'
            ? pf.target.id ==  entityRef.id && pf.specification.owningEntity.id === appRef.id
            : pf.target.id ==  appRef.id && pf.specification.owningEntity.id === entityRef.id)
        .value();
}


function filterByType(typeId, flows = [], decorators = []) {
    if (typeId == 0) {
        return {
            selectedTypeId: 0,
            decorators,
            flows
        };
    }

    const ds = _.filter(decorators, d => d.decoratorEntity.id === typeId);
    const dataFlowIds = _.map(ds, "dataFlowId");
    const fs = _.filter(flows, f => _.includes(dataFlowIds, f.id));

    return {
        selectedTypeId: typeId,
        decorators: ds,
        flows: fs
    };
}


// flowId -> [ { id (typeId), rating }... ]
function mkTypeInfo(decorators = []) {
    return _.chain(decorators)
        .filter({ decoratorEntity: { kind: 'DATA_TYPE' }})
        .groupBy(d => d.dataFlowId)
        .mapValues(xs => _.map(xs, x => {
            return {
                id: x.decoratorEntity.id,
                rating: x.rating
            };
        }))
        .value();
}


function calculateSourceAndTargetFlowsByAppId(app, logical = []) {
    if (! app) return {};

    const sourceFlowsByAppId = _.chain(logical)
        .filter(f => f.target.id === app.id)
        .reduce((acc, f) => { acc[f.source.id] = f.id; return acc; }, {})
        .value();

    const targetFlowsByAppId = _.chain(logical)
        .filter(f => f.source.id === app.id)
        .reduce((acc, f) => { acc[f.target.id] = f.id; return acc; }, {})
        .value();

    return {
        sourceFlowsByAppId,
        targetFlowsByAppId
    };
}


const iconCodes = {
    // codes from: http://fontawesome.io/cheatsheet/  (conversion: &#x1234; -> \u1234)
    files: '\uf0c5',
    file: '\uf016',
    question: '\uf128',
    questionCircle: '\uf29c',
    folder: '\uf115'
};


function toIcon(count = 0) {
    switch (count) {
        case 0:
            return {
                code: iconCodes.questionCircle,
                color: '#c66'
            };
        case 1:
            return {
                code: iconCodes.file,
                color: '#000'
            };
        case 2:
            return {
                code: iconCodes.files,
                color: '#000'
            };
        default:
            return {
                code: iconCodes.folder,
                color: '#000'
            };
    }
}


function mkTweakers(tweakers = {},
                    entityRef = null,
                    physicalFlows = [],
                    physicalSpecifications = { consumes: [], produces: []}) {

    const allSpecs = _.concat(physicalSpecifications.produces, physicalSpecifications.consumes);
    const specsById = _.keyBy(allSpecs, 'id');

    const flows = _.map(physicalFlows, pf => {
        return {
            source: specsById[pf.specificationId].owningEntity,
            target: pf.target
        };
    });

    const flowsBySourceEntity = nest()
        .key(f => f.source.kind)
        .key(f => f.source.id)
        .object(flows);

    const flowsByTargetEntity = nest()
        .key(f => f.target.kind)
        .key(f => f.target.id)
        .object(flows);

    const getSourceCount = (ref) => {
        const possibleFlows = _.get(flowsBySourceEntity, `${ref.kind}.${ref.id}`, []);
        const actualFlows = _.filter(possibleFlows, f => f.target.id === entityRef.id && f.target.kind === entityRef.kind)
        return actualFlows.length;
    };

    const getTargetCount = (ref) => {
        const possibleFlows = _.get(flowsByTargetEntity, `${ref.kind}.${ref.id}`, []);
        const actualFlows = _.filter(possibleFlows, f => f.source.id === entityRef.id && f.source.kind === entityRef.kind)
        return actualFlows.length;
    };

    tweakers.source.icon = (appRef) => toIcon(getSourceCount(appRef));
    tweakers.target.icon = (appRef) => toIcon(getTargetCount(appRef));

    return Object.assign({} , tweakers);
}


function controller($timeout) {
    const vm = initialiseData(this, initialState);

    vm.showAll = () => vm.filteredFlowData = filterByType(0, vm.logicalFlows, vm.decorators);
    vm.$onChanges = (changes) => {

        if (changes.logicalFlows || changes.decorators) vm.filteredFlowData = vm.showAll();

        const keyedLogicalFlows = calculateSourceAndTargetFlowsByAppId(
            vm.entityRef,
            vm.logicalFlows);

        function select(app, type, flowId, evt) {
            const typeInfoByFlowId = mkTypeInfo(vm.decorators);
            const types = typeInfoByFlowId[flowId] || [];
            return {
                type,
                types,
                physicalFlows: calcPhysicalFlows(vm.physicalFlows, vm.physicalSpecifications, app, type, vm.entityRef),
                app,
                y: evt.layerY
            };
        }

        const baseTweakers = {
            source: {
                onSelect: (app, evt) => $timeout(() => {
                    const flowId = keyedLogicalFlows.sourceFlowsByAppId[app.id];
                    vm.selected = select(app, 'source', flowId, evt);
                })
            },
            target: {
                onSelect: (app, evt) => $timeout(() => {
                    const flowId = keyedLogicalFlows.targetFlowsByAppId[app.id];
                    vm.selected = select(app, 'target', flowId, evt);
                })
            },
            type: {
                onSelect: d => {
                    event.stopPropagation();
                    $timeout(() => vm.filteredFlowData = filterByType(
                        d.id,
                        vm.logicalFlows,
                        vm.decorators));

                }
            },
            typeBlock: {
                onSelect: () => {
                    event.stopPropagation();
                    $timeout(() => {
                        if (vm.filteredFlowData.selectedTypeId > 0) {
                            vm.showAll();
                        }
                    });
                }
            }
        };

        vm.tweakers = mkTweakers(
            baseTweakers,
            vm.entityRef,
            vm.physicalFlows,
            vm.physicalSpecifications);
    };
}


controller.$inject = [
    '$timeout'
];


const component = {
    template,
    bindings,
    controller
};


export default component;
