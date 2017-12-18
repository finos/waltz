/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import _ from 'lodash';
import { initialiseData } from '../../common';
import XLSX from 'xlsx'

import template from './spreadsheet-loader.html';


const bindings = {
};


const initialState = {
    columnDefs: [],
    rowData: [],
    onGridInitialise: (cfg) => console.log('default grid initialise handler for spreadsheet-loader')
};


function prepareColumnDefs(headerNames) {
    return _.map(headerNames, name => ({field: name, name}));
}


function controller($scope) {
    const vm = initialiseData(this, initialState);

    vm.fileChange = (e) => {
        const files = e.srcElement.files;
        if(files.length == 0) return;
        const file = files[0];

        var reader = new FileReader();
        reader.onload =function (evt) {
            $scope.$apply(function () {
                var evtData = evt.target.result;
                var workbook = XLSX.read(evtData, {type: 'binary'});

                const headerNames = XLSX.utils.sheet_to_json(
                    workbook.Sheets[workbook.SheetNames[0]],
                    { header: 1 }
                )[0];

                const rowData = XLSX.utils.sheet_to_json(workbook.Sheets[workbook.SheetNames[0]]);

                vm.columnDefs = prepareColumnDefs(headerNames);
                vm.rowData = rowData;
            });
        };

        reader.readAsBinaryString(file);
    }
}


controller.$inject = [
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
