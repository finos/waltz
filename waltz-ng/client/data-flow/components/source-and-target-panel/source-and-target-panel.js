import _ from 'lodash';


const bindings = {
    entityRef: '<',
    logicalFlows: '<',
    decorators: '<',
    physicalFlows: '<',
    physicalArticles: '<'
};

function controller($scope) {
    const vm = this;

    vm.$onChanges = (changes) => {
        const keyedLogicalFlows = calculateSourceAndTargetFlowsByAppId(
            vm.entityRef,
            vm.logicalFlows);

        function select(app, type, flowId, evt) {
            const typeInfoByFlowId = mkTypeInfo(vm.decorators);
            const physicalFlowsByLogicalFlowId = mkPhysicalFlowInfo(vm.physicalArticles, vm.physicalFlows);
            const types = typeInfoByFlowId[flowId] || [];
            const physicalFlows = physicalFlowsByLogicalFlowId[flowId] || [];
            return {
                type,
                types,
                physicalFlows,
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
            keyedLogicalFlows,
            vm.physicalFlows);
    };
}


controller.$inject = [
    '$scope'
];


const template = require('./source-and-target-panel.html');

// flowId -> [ { ...physicalFlow, article: {} } ... ]
function mkPhysicalFlowInfo(physicalArticles = { consumes: [], produces: [] },
                            physicalFlows = [])
{

    const allArticles = _.concat(physicalArticles.consumes, physicalArticles.produces);
    const articlesById = _.keyBy(allArticles || [], "id");
    return _.chain(physicalFlows)
        .map(pf => Object.assign({}, pf, { article: articlesById[pf.articleId] }))
        .groupBy("flowId")
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
                         keyedLogicalFlows = {},
                         physicalFlows = []) {
    const physicalCountsByLogicalId = _.countBy(physicalFlows, "flowId");

    const getIcon = (id, relevantFlowsByAppId = {}) => {
        const logicalFlowId = relevantFlowsByAppId[id];
        const physicalCount = physicalCountsByLogicalId[logicalFlowId] || 0;
        return toIcon(physicalCount);
    };

    const { sourceFlowsByAppId = {}, targetFlowsByAppId = {} } = keyedLogicalFlows;

    tweakers.source.icon = (d) => getIcon(d.id, sourceFlowsByAppId);
    tweakers.target.icon = (d) => getIcon(d.id, targetFlowsByAppId);

    return Object.assign({} , tweakers);
}


const component = {
    template,
    bindings,
    controller
};


export default component;
