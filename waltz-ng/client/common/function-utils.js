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

/**
 * Always returns the same value, `val`, regardless of input.
 * @param val
 * @returns {function(): *}
 */
export function always(val) {
    return () => val;
}


/**
 * Attempts to return the result of the given function.
 * If the function throws an exception the default value
 * will be returned
 *
 * @param fn
 * @param dflt  value to return if fn throws an exception
 * @returns {*}
 */
export function tryOrDefault(fn, dflt) {
    try {
        return fn();
    } catch (e) {
        return dflt;
    }
}


/**
 * Invokes a function and also passes in any provided arguments in order
 * e.g. invokeFunction(onClick, arg1, arg2)
 * @param fn
 * @returns {*}
 */
export function invokeFunction(fn) {
    if (_.isFunction(fn)) {
        const parameters = _.slice(arguments, 1);
        return fn(...parameters);
    }
    console.log("invokeFunction - attempted to invoke empty function: ", fn)
    return null;
}


/**
 * If the given object is defined attempt to invoke the function `fn` on it.
 * Otherwise return the given default value, `dflt`.
 * @param obj
 * @param fn
 * @param dflt
 * @returns {*}
 */
export function ifPresent(obj, fn, dflt) {
    return obj
        ? fn(obj)
        : dflt;
}


/**
 * Iterates over a set of functions (`fns`), invoking each in turn on the
 * object (`obj`) provided by the first parameter.
 *
 * - if the result of calling the function on the object is _null_ (or _undefined_) the next
 * function is tried
 * - if the result is _non null_ then that result is returned and no other functions are tried
 * - if _none_ of the functions produce a result then `null` is returned
 *
 * @param obj
 * @param fns
 * @returns {null}
 */
export function firstToReturn(obj, fns = []) {
    let result = null;
    for (const fn of fns) {
        result = invokeFunction(fn, obj);
        if (! _.isNil(result)) break;
    }
    return result;
}


export function coalesceFns(...fns) {
    for (const fn of fns) {
        const res = fn(); 
        if (!_.isEmpty(res)) return res;
    }    
}