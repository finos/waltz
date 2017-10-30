import {CORE_API} from "./services/core-api-utils";

export const allowedRelationshipsByKind = {
    'CHANGE_INITIATIVE': [{
            value: 'APPLICATION_NEW',
            name: 'Application - new'
        }, {
            value: 'APPLICATION_FUNCTIONAL_CHANGE',
            name: 'Application - functional change'
        }, {
            value: 'APPLICATION_DECOMMISSIONED',
            name: 'Application - decommissioned'
        }, {
            value: 'APPLICATION_NFR_CHANGE',
            name: 'Application - NFR change'
        }, {
            value: 'DATA_PUBLISHER',
            name: 'Data publisher'
        }, {
            value: 'DATA_CONSUMER',
            name: 'Data consumer'
    }]
};


export const fetchRelationshipFunctionsByKind = {
    'CHANGE_INITIATIVE': CORE_API.ChangeInitiativeStore.findRelatedForId
};


export const changeRelationshipFunctionsByKind = {
    'CHANGE_INITIATIVE': CORE_API.ChangeInitiativeStore.changeRelationship
};

