/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks";

function store($http, baseUrl) {

    const BASE = `${baseUrl}/roadmap`;

    const getRoadmapById = (roadmapId) =>
        $http
            .get(`${BASE}/id/${roadmapId}`)
            .then(result => result.data);

    const findRoadmapsBySelector = (selectionOptions) => {
        checkIsIdSelector(selectionOptions);
        return $http
            .post(`${BASE}/by-selector`, selectionOptions)
            .then(result => result.data);
    };


    return {
        getRoadmapById,
        findRoadmapsBySelector
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "RoadmapStore";


export const RoadmapStore_API = {
    getRoadmapById: {
        serviceName,
        serviceFnName: "getRoadmapById",
        description: "executes findScenariosForRoadmap [roadmapId]"
    },
    findRoadmapsBySelector: {
        serviceName,
        serviceFnName: "findRoadmapsBySelector",
        description: "executes findRoadmapsBySelector [selectorOptions]"
    }
};


export default {
    serviceName,
    store
};