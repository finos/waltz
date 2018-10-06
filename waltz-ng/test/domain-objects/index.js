/*
 *
 * COMMON DOMAIN OBJECTS
 *   - do a full test if changing anything in this file
 *
 */
import _ from "lodash";


export const f = "F";


// -- ENTITY_REFS ---

function mkRef(kind, id, name, description) {
    return {
        id, kind, name, description
    }
}

export function mkAppRef(id, name, description) {
    return mkRef("APPLICATION", id, name, description);
}

export const applicationReferences = {
    APP_A: mkAppRef(1, "AppA"),
    APP_B: mkAppRef(2, "AppA"),
    APP_C: mkAppRef(3, "AppC")
};

export const applicationReferencesList = _.values(applicationReferences);


// -- MEASURABLES ---

export const measurables = {
    regions: {
        EU: {
            id: 1,
            name: "Europe",
            description: "Region Definition",
            category: 1
        },
        UK: {
            id: 10,
            parent: 1,
            name: "UK",
            description: "Region Definition",
            category: 1
        }
    },
    processes: {
        TRADE: {
            id: 2,
            name: "Trade",
            description: "Process Definition",
            category: 3
        },
        SELL: {
            id: 20,
            parent: 2,
            name: "SELL",
            description: "Process Definition",
            category: 3
        }
    },
    products: {
        RETAIL: {
            id: 3,
            name: "Retail",
            description: "Process Definition",
            category: 2
        },
        MORTGAGE: {
            id: 30,
            parent: 2,
            name: "MORTGAGE",
            description: "Product Definition",
            categoryId: 2
        }
    }
};


export const measurablesList = [
    ..._.values(measurables.regions),
    ..._.values(measurables.products),
    ..._.values(measurables.processes)
];


// -- MEASURABLE_RATINGS ---

export function mkMeasurableRating(measurableId, entityRef, rating = "A", description = "desc") {
    return {
        measurableId: _.isObject(measurableId) ? measurableId.id : measurableId,
        entityRef,
        rating,
        description
    };
}


