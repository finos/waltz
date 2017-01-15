/*
 *
 * COMMON DOMAIN OBJECTS
 *   - do a full test if changing anything in this file
 *
 */
import _ from 'lodash';


export const f = 'F'

// -- ENTITY_REFS ---

function mkRef(kind, id, name, description) {
    return {
        id, kind, name, description
    }
}

export function mkAppRef(id, name, description) {
    return mkRef('APPLICATION', id, name, description);
}

export const applicationReferences = {
    APP_A: mkAppRef(1, 'AppA'),
    APP_B: mkAppRef(2, 'AppA'),
    APP_C: mkAppRef(3, 'AppC')
};

export const applicationReferencesList = _.values(applicationReferences);




// -- MEASURABLES ---

export const measurables = {
    regions: {
        EU: {
            id: 1,
            name: 'Europe',
            description: 'Region Definition',
            kind: 'REGION'
        },
        UK: {
            id: 10,
            parent: 1,
            name: 'UK',
            description: 'Region Definition',
            kind: 'REGION'
        }
    },
    processes: {
        TRADE: {
            id: 2,
            name: 'Trade',
            description: 'Process Definition',
            kind: 'PROCESS'
        },
        SELL: {
            id: 20,
            parent: 2,
            name: 'SELL',
            description: 'Process Definition',
            kind: 'PROCESS'
        }
    },
    products: {
        RETAIL: {
            id: 3,
            name: 'Retail',
            description: 'Process Definition',
            kind: 'PRODUCT'
        },
        MORTGAGE: {
            id: 30,
            parent: 2,
            name: 'MORTGAGE',
            description: 'Product Definition',
            kind: 'PRODUCT'
        }
    }
};


export const measurablesList = [
    ..._.values(measurables.regions),
    ..._.values(measurables.products),
    ..._.values(measurables.processes)
];


// -- MEASURABLE_RATINGS ---

export function mkMeasurableRating(measurableId, entityRef, rating = 'A', description = 'desc') {
    return {
        measurableId: _.isObject(measurableId) ? measurableId.id : measurableId,
        entityRef,
        rating,
        description
    };
}



// -- PERSPECTIVES ---

export const perspectiveDefinitions = {
    PROCESS_REGION: {
        id: 12,
        name: 'TestPerspective',
        description: 'TestPerspective Definition',
        kindA: 'REGION',
        kindB: 'PROCESS'
    }
};





