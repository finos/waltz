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
import angular from "angular";
import {checkIsEntityRef} from "../../common/checks";

function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/shared-preference`;

    const getByKeyAndCategory = (key, category) => {
        // encode because the key will be used in URI
        const keyEncoded = encodeURIComponent(key);
        return $http
            .get(`${base}/key/${keyEncoded}/category/${category}`)
            .then(result => result.data);
    };

    const findByCategory = (category) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/category/${category}`)
            .then(r => r.data);
    };

    const generateKeyRoute = (obj) => {
        // toJson - to remove the angular binding properties before we use object
        const data = angular.toJson(obj);
        return $http
            .post(`${base}/generate-key`, data)
            .then(result => result.data);
    };


    const save = (key, category, value) => {
        // toJson - to remove the angular binding properties before we use object
        const data = angular.toJson(value);
        const cmd = {
            key,
            category,
            value: data
        };
        return $http
            .post(`${base}/save`, cmd)
            .then(result => result.data);
    };

    return {
        getByKeyAndCategory,
        findByCategory,
        generateKeyRoute,
        save
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = 'SharedPreferenceStore';


export const SharedPreferenceStore_API = {
    getByKeyAndCategory: {
        serviceName,
        serviceFnName: 'getByKeyAndCategory',
        description: 'executes getByKeyAndCategory'
    },
    findByCategory: {
        serviceName,
        serviceFnName: 'findByCategory',
        description: 'executes findByCategory'
    },
    generateKeyRoute: {
        serviceName,
        serviceFnName: 'generateKeyRoute',
        description: 'executes generateKeyRoute'
    },
    save: {
        serviceName,
        serviceFnName: 'save',
        description: 'executes save'
    },
};


export default {
    store,
    serviceName
};
