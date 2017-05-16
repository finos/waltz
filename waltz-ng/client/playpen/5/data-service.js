import _ from 'lodash';
import stringify from 'json-stable-stringify';

import {initialiseData} from '../../common'

const initialState = {
    viewData: new Map(),
    appData: new Map(),
    viewDataRefreshNotificationHandlers: new Map(), // cacheKey -> Set{handler1, handler2...}
    appDataRefreshNotificationHandlers: new Map(), // cacheKey -> Set{handler1, handler2...}
};


function getFunction(service, serviceName, serviceFnName) {
    const serviceFn = service[serviceFnName];
    if (serviceFn && _.isFunction(serviceFn)) {
        return serviceFn;
    }
    throw serviceName + "." + serviceFnName + " not found!"
}


function createKey(serviceName, serviceFnName, serviceFnParams) {
    return serviceName + "_" + serviceFnName + "_" + stringify(serviceFnParams);
}


function isNamedFunction(fn) {
    return _.isFunction(fn)
        && !_.isEmpty(fn.prototype.constructor.name);
}


function ensureNamedFunction(fn, errorMessage) {
    if (!isNamedFunction(fn)) {
        throw errorMessage;
    }
}

/***
 *
 * @param $injector
 * @param cache
 * @param refreshNotifyHandlers
 * @param serviceName
 * @param serviceFnName
 * @param serviceFnParams
 * @param refreshNotificationHandler - must be a named function
 * @param force
 */
function loadData($injector,
                  cache,
                  refreshNotifyHandlers,
                  serviceName,
                  serviceFnName,
                  serviceFnParams = [],
                  refreshNotificationHandler = null,
                  force = false) {
    console.log('loading: ', serviceName, serviceFnName, serviceFnParams, force);
    const service = $injector.get(serviceName);
    const cacheKey = createKey(serviceName, serviceFnName, serviceFnParams);

    if (!cache.has(cacheKey) || force === true) {
        const serviceFn = getFunction(service, serviceName, serviceFnName);
        const promise = serviceFn(...serviceFnParams);
        cache.set(cacheKey, promise);
        const handlers = refreshNotifyHandlers.get(cacheKey) || [];
        handlers.forEach(f => f());
        console.log('saved in cache with key: ', cacheKey);
    }

    if (refreshNotificationHandler) {
        const errorMessage = serviceName + "." + serviceFnName + " refreshNotificationHandler must be a non-anonymous function!";
        ensureNamedFunction(refreshNotificationHandler, errorMessage);
        if (!refreshNotifyHandlers.has(cacheKey)) {
            refreshNotifyHandlers.set(cacheKey, new Set());
        }
        refreshNotifyHandlers.get(cacheKey).add(refreshNotificationHandler);
        console.log("set handler for cache key", cacheKey, refreshNotifyHandlers.get(cacheKey));
    }

    return cache.get(cacheKey);
}


function service($injector) {
    const vm = initialiseData(this, initialState);

    const loadViewData = (serviceName,
                          serviceFnName,
                          serviceFnParams = [],
                          refreshNotificationHandler = null,
                          force = false) => {

        return loadData($injector,
                        vm.viewData,
                        vm.viewDataRefreshNotificationHandlers,
                        serviceName,
                        serviceFnName,
                        serviceFnParams,
                        refreshNotificationHandler,
                        force);
    };

    const loadAppData = (serviceName,
                         serviceFnName,
                         serviceFnParams = [],
                         refreshNotificationHandler = null,
                         force = false) => {

        return loadData($injector,
                        vm.appData,
                        vm.appDataRefreshNotificationHandlers,
                        serviceName,
                        serviceFnName,
                        serviceFnParams,
                        refreshNotificationHandler,
                        force);
    };

    const execute = (serviceName, serviceFnName, serviceFnParams = []) => {
        console.log('execute: ', serviceName, serviceFnName, serviceFnParams);
        const service = $injector.get(serviceName);
        const serviceFn = getFunction(service, serviceName, serviceFnName);
        return serviceFn(...serviceFnParams);
    };

    const resetViewData = () => {
        vm.viewData.clear();
        vm.viewDataRefreshNotificationHandlers.clear();
        console.log('reset view data');
    };

    const resetAll = () => {
        resetViewData();
        vm.appData.clear();
    };


    return {
        execute,
        loadAppData,
        loadViewData,
        resetViewData,
        resetAll
    };
}


service.$inject = ['$injector'];


export default service;
