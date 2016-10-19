import _ from 'lodash';


const bindings = {
    entityRef: '<',
    logicalFlows: '<',
    decorators: '<',
    physicalFlows: '<',
    physicalSpecifications: '<'
};

function controller($scope) {
    const vm = this;

    vm.$onChanges = (changes) => {
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
                y: evt.pageY
            };
        }

        const baseTweakers = {
            source: {
                onSelect: (app, evt) => $scope.$applyAsync(() => {
                    const flowId = keyedLogicalFlows.sourceFlowsByAppId[app.id];
                    vm.selected = select(app, 'source', flowId, evt);
                })
            },
            target: {
                onSelect: (app, evt) => $scope.$applyAsync(() => {
                    const flowId = keyedLogicalFlows.targetFlowsByAppId[app.id];
                    vm.selected = select(app, 'target', flowId, evt);
                })
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
    '$scope'
];


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
                    physicalSpecifications = []) {

    const getSourceCount = (ref) => {
        const potentialSpecs = _.chain(physicalSpecifications.consumes)
            .filter({ owningEntity: { id: ref.id, kind: ref.kind }})
            .map("id")
            .value();

        const count = _.chain(physicalFlows)
            .filter(pf => _.includes(potentialSpecs, pf.specificationId))
            .filter({ target : { kind: entityRef.kind, id: entityRef.id }})
            .size()
            .value();

        return count;
    };

    const getTargetCount = (ref) => {
        const potentialSpecs = _.chain(physicalSpecifications.produces)
            .map("id")
            .value();

        const count = _.chain(physicalFlows)
            .filter(pf => _.includes(potentialSpecs, pf.specificationId))
            .filter({ target : { kind: ref.kind, id: ref.id }})
            .size()
            .value();

        return count;
    };

    tweakers.source.icon = (appRef) => toIcon(getSourceCount(appRef));
    tweakers.target.icon = (appRef) => toIcon(getTargetCount(appRef));

    return Object.assign({} , tweakers);
}


const component = {
    template,
    bindings,
    controller
};


export default component;
