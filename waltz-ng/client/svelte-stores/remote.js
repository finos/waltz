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

import {$http} from "../common/WaltzHttp";
import stringify from "json-stable-stringify";
import {writable} from "svelte/store";


class Cache {
    constructor(name) {
        this.name = name;
        this.data = new Map();
    }

    clear() {
        console.log("Clear", this.name)
        this.data.clear();
    }

    get(key) {
        return this.data.get(key);
    }

    has(key) {
        return this.data.has(key);
    }

    init(key, d) {
        return this.data.set(key, writable(d))
    }

    set(key, d) {
        // console.log("set", {key, d})
        return this.data.get(key).set(d);
    }

    name() {
        return name;
    }
}


function mkPromise(method, url, data) {
    switch (method) {
        case "GET":
            return $http.get(url);
        case "POST":
            return $http.post(url, data);
        case "PUT":
            return $http.put(url, data);
        case "DELETE":
            return $http.delete(url);
        default:
            Promise.reject(`Unknown verb: ${method}`);
    }
}


function mkKey(method, url, config) {
    return `${method}_${url}_${stringify(config)}`;
}


function _fetchData(cache, method, url, data, init = [], config = { force: false }) {
    const key = mkKey(method, url, data);
    const forcing = _.get(config, "force", false);

    const doFetch = () => mkPromise(method, url, data)
        .then(r => cache.set(key, r.data));

    if (cache.has(key)) {
        if (forcing) {
            doFetch()
        }
    } else {
        cache.init(key, writable(init));
        doFetch();
    }

    return cache.get(key);
}


function _execute(method, url, data) {
    return mkPromise(method, url, data)
}


const appCache = new Cache("App");
const viewCache = new Cache("View");


function initRemote() {
    return {
        fetchViewList: (method, url, data, config) => _fetchData(viewCache, method, url, data, [], config),
        fetchViewDatum: (method, url, data, config) => _fetchData(viewCache, method, url, data, null, config),
        fetchViewData: (method, url, data, init, config) => _fetchData(viewCache, method, url, data, init, config),
        fetchAppData: (method, url, data, init, config) => _fetchData(appCache, method, url, data, init, config),
        fetchAppDatum: (method, url, data, config) => _fetchData(appCache, method, url, data, null, config),
        fetchAppList: (method, url, data, config) => _fetchData(appCache, method, url, data, [], config),
        execute: (method, url, data) => _execute(method, url, data),
        clear: () => viewCache.clear(),
    };
}

export const remote = initRemote();