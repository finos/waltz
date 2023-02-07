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
        this.cacheData = new Map();
    }

    clear() {
        this.cacheData.clear();
    }

    get(key) {
        return this.cacheData.get(key);
    }

    has(key) {
        return this.cacheData.has(key);
    }

    init(key, d) {
        return this.cacheData.set(key, writable({ data: d, error: null, status: "loading" }));
    }

    set(key, d) {
        return this.cacheData.get(key).set({ data: d, error: null, status: "loaded" })
    }

    err(key, e, d) {
        return this.cacheData.get(key).set({ data: d, error: e, status: "error" })
    }

    name() {
        return name;
    }
}


function mkPromise(method, url, data, options) {
    switch (method) {
        case "GET":
            return $http.get(url, options);
        case "POST":
            return $http.post(url, data, options);
        case "PUT":
            return $http.put(url, data, options);
        case "DELETE":
            return $http.delete(url, options);
        default:
            Promise.reject(`Unknown verb: ${method}`);
    }
}


function mkKey(method, url, data) {
    return `${method}_${url}_${stringify(data)}`;
}


function _fetchData(cache, method, url, data, init = [], config = { force: false }) {
    const key = mkKey(method, url, data);
    const forcing = _.get(config, ["force"], false);

    const invokeFetch = () => mkPromise(method, url, data)
        .then(r => cache.set(key, r.data))
        .catch(e => cache.err(key, e, init));

    if (cache.has(key)) {
        if (forcing) {
            invokeFetch();
        }
    } else {
        cache.init(key, init);
        invokeFetch();
    }

    return cache.get(key);
}


function _execute(method, url, data) {
    return mkPromise(method, url, data);
}


const appCache = new Cache("App");
const viewCache = new Cache("View");


function initRemote(cache) {
    return {
        fetchViewList: (method, url, data, config) => _fetchData(cache, method, url, data, [], config),
        fetchViewDatum: (method, url, data, config) => _fetchData(cache, method, url, data, null, config),
        fetchViewData: (method, url, data, init, config) => _fetchData(cache, method, url, data, init, config),
        fetchAppData: (method, url, data, init, config) => _fetchData(cache, method, url, data, init, config),
        fetchAppDatum: (method, url, data, config) => _fetchData(cache, method, url, data, null, config),
        fetchAppList: (method, url, data, config) => _fetchData(cache, method, url, data, [], config),
        execute: (method, url, data) => _execute(method, url, data),
        clear: () => viewCache.clear(),
    };
}

export const remote = initRemote(viewCache);
export const remoteApp = initRemote(appCache);