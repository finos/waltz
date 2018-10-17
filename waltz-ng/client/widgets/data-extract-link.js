/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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


const bindings = {
    name: "@",
    extract: "@",
    method: "@",
    filename: "@",
    requestBody: "<",
    styling: "@?"
};


const initialState = {
    name: "Export",
    filename: "extract.csv",
    method: "GET",
    requestBody: null,
    styling: "button"
};


function calcClasses(styling = "button") {
    switch(styling) {
        case "link":
            return ["clickable"];
        default:
            return ["btn", "btn-xs", "btn-primary"];
    }
}


function getFileNameFromHttpResponse(httpResponse) {
    var contentDispositionHeader = httpResponse.headers("Content-Disposition");
    if(!contentDispositionHeader) {
        return null;
    }
    var result = contentDispositionHeader.split(";")[1].trim().split("=")[1];
    return result.replace(/"/g, "");
}


function controller($http, BaseExtractUrl) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.url = `${BaseExtractUrl}/${vm.extract}`;
        vm.classes = calcClasses(vm.styling);
    };

    vm.export = () => {
        switch (vm.method) {
            case "GET":
                return $http
                    .get(vm.url)
                    .then(r => downloadFile(r.data, getFileNameFromHttpResponse(r) || vm.filename));
            case "POST":
                return $http
                    .post(vm.url, vm.requestBody)
                    .then(r => downloadFile(r.data, getFileNameFromHttpResponse(r) || vm.filename));
            default:
                throw "Unrecognised method: " + vm.method;
        }
    };

}


controller.$inject = [
    "$http",
    "BaseExtractUrl"
];


const component = {
    bindings,
    controller,
    template
};


export default component;