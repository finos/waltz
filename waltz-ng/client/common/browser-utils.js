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


/**
 * Stops propagation of an event, compatible with both webkit and IE variants
 * @returns {*}
 */
export function stopPropagation(e) {
    e.cancelBubble = true;
    if (e.stopPropagation) e.stopPropagation();
}


/**
 * Prevents default on an event, compatible with both webkit and IE variants
 * @returns {*}
 */
export function preventDefault(e) {
    e.returnValue = false;
    if (e.preventDefault) e.preventDefault();
}


export function isIE() {
    const match = navigator.userAgent.search(/(?:Edge|MSIE|Trident\/.*; rv:)/);
    return match !== -1;
}


export function copyTextToClipboard(text) {
    if (navigator.clipboard) {
        return navigator.clipboard.writeText(text)
            .then(() => console.log("Copied to clipboard successfully!"))
            .catch(() => console.error("Unable to write to clipboard. :-("));
    } else if (window.clipboardData) { // Internet Explorer
        window.clipboardData.setData("Text", text);
        return Promise.resolve();
    }
}


export function isDescendant(parentElement, childElement) {
    let node = childElement.parentNode;
    while (node != null) {
        if (node == parentElement) {
            return true;
        }
        node = node.parentNode;
    }
    return false;
}


export function parseParams(searchParams = "") {
    const params = {};
    searchParams
        .replace(
            /[?&]+([^=&]+)=([^&]*)/gi,
            (overallMatch, key, value) => {
                params[key] = value;
            })
    return params;
}
