

function utils(capabilityStore, orgUnitStore, processStore) {

    const findAllForKind = (kind) => {
        let promise = null;
        switch (kind) {
            case 'ORG_UNIT':
                return orgUnitStore.findAll()
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
    'CapabilityStore',
    'OrgUnitStore',
    'ProcessStore'
];


export default utils;