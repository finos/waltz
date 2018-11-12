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
import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks"


function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/measurable`;

    const findAll = () => $http
        .get(`${baseUrl}/all`)
        .then(d => d.data);

    const findMeasurablesRelatedToPath = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const findByExternalId = (extId) => $http
        .get(`${baseUrl}/external-id/${extId}`)
        .then(d => d.data);

    const findMeasurablesBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/measurable-selector`, options)
            .then(d => d.data);
    };

    const search = (query) => $http
        .get(`${baseUrl}/search/${query}`)
        .then(x => x.data);

    return {
        findAll,
        findByExternalId,
        findMeasurablesRelatedToPath,
        findMeasurablesBySelector,
        search
    };

}


store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableStore";


export default {
    store,
    serviceName
};


export const MeasurableStore_API = {
    findAll: {
        serviceName,
        serviceFnName: "findAll",
        description: "findAll"
    },
    findByExternalId: {
        serviceName,
        serviceFnName: "findByExternalId",
        description: "saves an entity named note"
    },
    search: {
        serviceName,
        serviceFnName: "search",
        description: "executes search"
    },
    findMeasurablesRelatedToPath: {
        serviceName,
        serviceFnName: "findMeasurablesRelatedToPath",
        description: "executes findMeasurablesRelatedToPath"
    },
    findMeasurablesBySelector: {
        serviceName,
        serviceFnName: "findMeasurablesBySelector",
        description: "executes findMeasurablesBySelector"
    }
};