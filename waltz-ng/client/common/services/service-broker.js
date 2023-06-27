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

import _ from "lodash";
import moment from "moment";
import stringify from "json-stable-stringify";

import {initialiseData} from "../../common";
import {checkIsArray, checkIsServiceBrokerOptions, checkIsServiceBrokerTarget} from "../checks";

const initialState = {
    viewData: new Map(),
    appData: new Map(),
    viewDataCacheRefreshListeners: new Map(), // cacheKey -> Set{handler1, handler2...}
    appDataCacheRefreshListeners: new Map(), // cacheKey -> Set{handler1, handler2...}
};


function getFunction(service, serviceName, serviceFnName) {
    const serviceFn = service[serviceFnName];
    if (serviceFn && _.isFunction(serviceFn)) {
        return serviceFn;
    }
    throw serviceName + "." + serviceFnName + " not found!"
}


function createKey(serviceName, serviceFnName, targetParams) {
    return serviceName + "_" + serviceFnName + "_" + stringify(targetParams);
}


function mkCacheValue(promise) {
    return {
        promise,
        lastRefreshed: moment()
    }
}


function notifyListeners(cacheKey, cacheRefreshListenersMap, target, targetParams, eventType) {
    const listeners = cacheRefreshListenersMap.get(cacheKey);
    if (listeners) {
        const detail = {
            eventType,
            serviceName: target.serviceName,
            serviceFnName: target.serviceFnName,
            params: targetParams
        };
        listeners.forEach((f) => f(detail));
    }
}


function invokeServiceFunction(service, serviceName, serviceFnName, targetParams) {
    const serviceFn = getFunction(service, serviceName, serviceFnName);
    const promise = serviceFn(...targetParams);
    return promise;
}


function registerCacheRefreshListener(cacheRefreshListenersMap, cacheKey, cacheRefreshListener) {
    if (!cacheRefreshListenersMap.has(cacheKey)) {
        cacheRefreshListenersMap.set(cacheKey, new Map());
    }
    cacheRefreshListenersMap
        .get(cacheKey)
        .set(cacheRefreshListener.componentId, cacheRefreshListener.fn);
}


function performChecks(target, targetParams, options) {
    checkIsServiceBrokerTarget(target);
    checkIsArray(targetParams, "targetParams must be an array");
    checkIsServiceBrokerOptions(options);
}


function loadData($injector,
                  cache,
                  cacheRefreshListenersMap,
                  target,
                  targetParams = [],
                  options) {


    const {serviceName, serviceFnName} = target;
    const {force = false, cacheRefreshListener} = options;

    if (! $injector.has(serviceName)) {
        const message = "ServiceBroker::loadData - Unable to locate service: " + serviceName;
        console.error(message);
        return Promise.reject(message);
    }

    const service = $injector.get(serviceName);
    const cacheKey = createKey(serviceName, serviceFnName, targetParams);

    // execute and cache data
    if (!cache.has(cacheKey) || force === true) {
        const promise = invokeServiceFunction(service, serviceName, serviceFnName, targetParams);
        cache.set(cacheKey, mkCacheValue(promise));

        notifyListeners(cacheKey, cacheRefreshListenersMap, target, targetParams, "REFRESH");
    }

    // register cache refresh listener
    if (cacheRefreshListener) {
        registerCacheRefreshListener(cacheRefreshListenersMap, cacheKey, cacheRefreshListener);
    }

    // return data from cache
    const cacheValue = cache.get(cacheKey);
    const resultPromise = cacheValue.promise.then(data => ({
        lastRefreshed: cacheValue.lastRefreshed,
        data
    })).catch(error => {
        //evict the cache entry because it's in error
        cache.delete(cacheKey);
        console.warn(`ServiceBroker::loadData - ${serviceName}.${serviceFnName}: ${error.statusText} - ${error.data?.message}`, targetParams);
        throw error;
    });
    return resultPromise;
}


function service($injector) {
    const vm = initialiseData(this, initialState);


    const loadViewData = (target,
                          targetParams = [],
                          options = { force: false }) => {

        performChecks(target, targetParams, options);

        return loadData($injector,
                        vm.viewData,
                        vm.viewDataCacheRefreshListeners,
                        target,
                        targetParams,
                        options);
    };

    const loadAppData = (target,
                         targetParams = [],
                         options = { force: false }) => {

        performChecks(target, targetParams, options);

        return loadData($injector,
                        vm.appData,
                        vm.appDataCacheRefreshListeners,
                        target,
                        targetParams,
                        options);
    };

    const execute = (target,
                     targetParams = [],
                     options = {}) => {

        performChecks(target, targetParams, options);

        const {serviceName, serviceFnName} = target;

        if (! $injector.has(serviceName)) {
            const message = "ServiceBroker::execute - Unable to locate service: " + serviceName;
            console.error(message);
            return Promise.reject(message)
        }

        const service = $injector.get(serviceName);
        const serviceFn = getFunction(service, serviceName, serviceFnName);
        return serviceFn(...targetParams)
            .then(data => ({data}))
            .catch(error => {
                console.warn(`ServiceBroker::execute - ${serviceName}.${serviceFnName}: ${error.statusText} - ${error.data.message}`, targetParams);
                throw error;
            });
    };

    const resetViewData = () => {
        vm.viewData.clear();
        vm.viewDataCacheRefreshListeners.clear();
    };

    const resetAppData = () => {
        vm.appData.clear();
    };

    const resetAll = () => {
        resetViewData();
        resetAppData();
    };


    return {
        execute,
        loadAppData,
        loadViewData,
        resetViewData,
        resetAppData,
        resetAll
    };
}


service.$inject = ["$injector"];


export default service;
