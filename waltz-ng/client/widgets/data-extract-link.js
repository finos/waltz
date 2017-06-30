/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import template from './data-extract-link.html';


const bindings = {
    name: '@',
    extract: '@',
    method: '@',
    filename: '@',
    requestBody: '<'
};


const initialState = {
    name: 'Export',
    filename: 'extract.csv',
    method: 'GET',
    requestBody: null
};


function controller($http, BaseExtractUrl) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.url = `${BaseExtractUrl}/${vm.extract}`;
    };

    vm.export = () => {
        switch (vm.method) {
            case 'GET':
                return $http.get(vm.url)
                    .then(r => downloadFile(r.data, vm.filename));
            case 'POST':
                return $http.post(vm.url, vm.requestBody)
                    .then(r => downloadFile(r.data, vm.filename));
            default:
                throw 'Unrecognised method: ' + vm.method;
        }
    };

}


controller.$inject = [
    '$http',
    'BaseExtractUrl'
];


const component = {
    bindings,
    controller,
    template
};


export default component;