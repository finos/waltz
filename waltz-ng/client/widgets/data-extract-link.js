/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import {downloadFile} from "../common/file-utils";
import {initialiseData} from "../common/index";

import template from "./data-extract-link.html";
import {displayError} from "../common/error-utils";


const bindings = {
    name: "@",
    extract: "@",
    method: "@",
    filename: "@",
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