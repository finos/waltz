export function onlySelectedTypes(dataTypeMap) {
    return _.chain(dataTypeMap)
        .reduce((acc, v, k) => {
            if (v) acc[k] = v;
            return acc;
        }, {})
        .keys()
        .value();
}

export function toRef(app) {
    return { id: app.id, kind: 'APPLICATION', name: app.name };
}

export function toCommandObject(model, originalTypes) {

    const source = toRef(model.source.app);
    const target = toRef(model.target.app);

    const updatedTypes = onlySelectedTypes(model.dataTypes);

    const addedTypes = _.difference(updatedTypes, originalTypes);
    const removedTypes = _.without(originalTypes, ...updatedTypes);

    return {
        source,
        target,
        addedTypes,
        removedTypes
    };
}


export function calculateDataTypeUsage(sourceApp, targetApp, flows) {

    if ( !sourceApp || !targetApp ) {
        return {};
    }

    const filterFn = f =>
    f.target.id === targetApp.id && f.source.id === sourceApp.id;


    const usageMap = _.chain(flows)
        .filter(filterFn)
        .map('dataType')
        .foldl((acc, t) => { acc[t] = true; return acc; }, {})
        .value();

    return usageMap;
}


export function loadAppAuthSources(authSourceStore, appId, vm) {
    return authSourceStore
        .findByApp(appId)
        .then(aas => vm.appAuthSources = aas);
}


export function loadOrgUnitAuthSources(authSourceStore, ouId, vm) {
    return authSourceStore
        .findByReference('ORG_UNIT', ouId)
        .then(ouas => vm.ouAuthSources = ouas);
}


export function loadDataFlows(dataFlowStore, id, vm) {
    return dataFlowStore.findEnrichedFlowsForApplication(id)
        .then(flows => vm.flows = flows);
}

export function loadDataTypes(dataTypeStore, vm) {
    return dataTypeStore.findAll().then(xs => vm.dataTypes = xs);
}
