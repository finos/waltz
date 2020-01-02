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

import _ from 'lodash';
import { initialiseData } from '../../common';
import XLSX from 'xlsx'
import { invokeFunction } from '../../common/index';


import template from './spreadsheet-loader.html';


const bindings = {
    onSpreadsheetLoaded: '<'
};


const initialState = {
    columnDefs: [],
    rowData: [],
    selectedSheetName: null,
    sheetNames: [],
    workbook: null,

    onSpreadsheetLoaded: (event) => console.log('default onSpreadsheetLoaded handler for spreadsheet-loader, ', event)
};


function convertToBinaryString(data) {
    if(!data) {
        return;
    }
    let binary = "";
    const bytes = new Uint8Array(data);
    for (var i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return binary;
}


function prepareColumnDefs(headerNames) {
    return _.map(headerNames, name => ({field: name, name}));
}


function mkHeadersAndRows(workbook, sheetName) {
    const headerNames = XLSX.utils.sheet_to_json(
        workbook.Sheets[sheetName],
        { header: 1 }
    )[0];

    const rowData = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName]);
    const columnDefs = prepareColumnDefs(headerNames);

    return {
        columnDefs,
        rowData
    };
}


function controller($element, $scope) {
    const vm = initialiseData(this, initialState);

    const loadWorksheet = () => {
        const { columnDefs, rowData } = mkHeadersAndRows(vm.workbook, vm.selectedSheetName);
        vm.columnDefs = columnDefs;
        vm.rowData = rowData;

        const event = {
            columnDefs,
            rowData
        };
        invokeFunction(vm.onSpreadsheetLoaded, event);
    };


    vm.$onInit = () => {
        vm.input = $element.find('input');
        if(! vm.input) {
            throw 'Could not find input element';
        }
        vm.input.on('change', vm.fileChange);
    };

    vm.$onDestroy = () => {
        if(vm.input) {
            vm.input.off('change', vm.fileChange);
        }
    };

    vm.fileChange = (e) => {
        const files = _.get(e, 'target.files', []);
        if(files.length == 0) return;
        const file = files[0];

        const reader = new FileReader();
        reader.onload = function (evt) {
            $scope.$apply(function () {
                const evtData = _.get(evt, 'target.result');
                const data = convertToBinaryString(evtData);
                vm.workbook = XLSX.read(data, {type: 'binary'});

                vm.sheetNames = vm.workbook.SheetNames;
                vm.selectedSheetName = vm.sheetNames[0];
                loadWorksheet();
            });
        };
        reader.readAsArrayBuffer(file);
    };

    vm.onSelectedSheetChange = () => {
        loadWorksheet();
    };

}


controller.$inject = [
    '$element',
    '$scope'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzSpreadsheetLoader'
};
