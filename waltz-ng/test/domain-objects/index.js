/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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


