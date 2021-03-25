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

export const $http = {
    get,
    post,
    put,
    delete: _delete
};



const headers = {
    "Content-Type": "application/json",
    "If-Modified-Since": 0
};

const satellizerToken = localStorage.getItem("satellizer_token");
if (! _.isEmpty(satellizerToken)) {
    headers["Authorization"] = `Bearer ${satellizerToken}`;
}


function get(url) {
    const requestOptions = {
        method: "GET",
        headers
    };
    return fetch(url, requestOptions)
        .then(handleResponse);
}

function post(url, body) {
    const requestOptions = {
        method: "POST",
        headers,
        body: JSON.stringify(body)
    };
    return fetch(url, requestOptions)
        .then(handleResponse);
}

function put(url, body) {
    const requestOptions = {
        method: 'PUT',
        headers,
        body: JSON.stringify(body)
    };
    return fetch(url, requestOptions)
        .then(handleResponse);
}

// prefixed with underscored because delete is a reserved word in javascript
function _delete(url) {
    const requestOptions = {
        method: "DELETE",
        headers
    };
    return fetch(url, requestOptions)
        .then(handleResponse);
}

// helper functions

function handleResponse(response) {
    return response.text().then(text => {
        const data = text && JSON.parse(text);

        if (!response.ok) {
            const error = (data && data.message) || response.statusText;
            return Promise.reject({error, response, text});
        }

        return { data};
    });
}
