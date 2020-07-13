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

export function store($http, BaseApiUrl) {

    const BASE = `${BaseApiUrl}/favourites`;

    const getFavouritesGroup = () => $http
        .get(`${BASE}/group`)
        .then(result => result.data);

    const getFavouritesGroupEntries = () => $http
        .get(`${BASE}/entries`)
        .then(result => result.data);

    const addApplication = (applicationId) => $http
        .post(`${BASE}/application/${applicationId}`)
        .then(result => result.data);

    const removeApplication = (applicationId) => $http
        .delete(`${BASE}/application/${applicationId}`)
        .then(result => result.data);

    return {
        getFavouritesGroup,
        getFavouritesGroupEntries,
        addApplication,
        removeApplication
    };
}


store.$inject = [
    '$http',
    'BaseApiUrl'
];


const serviceName = "FavouritesStore";


export const FavouritesStore_API = {
    getFavouritesGroup: {
        serviceName,
        serviceFnName: 'getFavouritesGroup',
        description: 'fetches Favourites Group for user'
    },
    getFavouritesGroupEntries: {
        serviceName,
        serviceFnName: 'getFavouritesGroupEntries',
        description: 'fetches application entries for favourites group'
    },
    addApplication: {
        serviceName,
        serviceFnName: 'addApplication',
        description: 'executes addApplication'
    },
    removeApplication: {
        serviceName,
        serviceFnName: 'removeApplication',
        description: 'executes removeApplication'
    }
};

export default {
    serviceName,
    store
}