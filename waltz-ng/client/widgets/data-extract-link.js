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

import {downloadFile} from "../common/file-utils";
import {initialiseData} from "../common/index";

import template from "./data-extract-link.html";
import {displayError} from "../common/error-utils";


const bindings = {
    name: "@?",
    extract: "@",
    method: "@?",
    format: "@?",
    filename: "@?",
    requestBody: "<",
    styling: "@?" // link | button
};


const initialState = {
    name: "Export",
    filename: "extract.csv",
    method: "GET",
    requestBody: null,
    styling: "button",
    extracting: false
};


function calcClasses(styling = "button") {
    switch (styling) {
        case "link":
            return ["clickable"];
        default:
            return ["btn", "btn-xs", "btn-default"];
    }
}


function getFileNameFromHttpResponse(httpResponse) {
    const contentDispositionHeader = httpResponse.headers("Content-Disposition");
    if(!contentDispositionHeader) {
        return null;
    }
    const result = contentDispositionHeader.split(";")[1].trim().split("=")[1];
    return result.replace(/"/g, "");
}


function controller($http, notification, baseExtractUrl) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.url = `${baseExtractUrl}/${vm.extract}`;
        vm.classes = calcClasses(vm.styling);
    };


    const invokeExport = (format) => {
        const options = {
            params : { format }
        };
        if (format === "XLSX") {
            options.responseType = "arraybuffer";
        }
        switch (vm.method) {
            case "GET":
                return $http
                    .get(vm.url, options)
                    .then(r => downloadFile(r.data, getFileNameFromHttpResponse(r) || vm.filename, format));
            case "POST":
                return $http
                    .post(vm.url, vm.requestBody, options )
                    .then(r => downloadFile(r.data, getFileNameFromHttpResponse(r) || vm.filename, format));
            default:
                return Promise.reject(`Unrecognised method: ${vm.method}`);
        }
    };

    const doExport = (format) => {
        notification.info("Exporting data");
        vm.extracting = true;
        invokeExport(format)
            .then(() => notification.success("Data exported"))
            .catch(e => displayError(notification, "Data export failure", e))
            .finally(() => vm.extracting = false);
    };

    vm.exportCsv = () => doExport("CSV");
    vm.exportXlsx = () => doExport("XLSX");
    vm.exportAs = (format) => doExport(format);
}


controller.$inject = [
    "$http",
    "Notification",
    "BaseExtractUrl"
];


const component = {
    bindings,
    controller,
    template
};


export default component;