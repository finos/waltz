

function utils(appGroupStore, capabilityStore, orgUnitStore, processStore) {

    const findAllForKind = (kind) => {
        switch (kind) {
            case 'APP_GROUP':
                return appGroupStore
                    .findMyGroupSubscriptions()
                    .then(gs => _.map(gs, 'appGroup'));
            case 'ORG_UNIT':
                return orgUnitStore.findAll();
            case 'CAPABILITY':
                return capabilityStore.findAll();
            case 'PROCESS':
                return processStore.findAll();
            default :
                throw 'Cannot create hierarchy for kind: '+kind;
        }
    };

    return {
        findAllForKind
    };
}


utils.$inject = [
    'AppGroupStore',
    'CapabilityStore',
    'OrgUnitStore',
    'ProcessStore'
];


export default utils;