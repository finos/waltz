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

import _ from "lodash";

import template from "./related-measurables-table.html";
import {initialiseData} from "../../../common/index";
import {sameRef} from "../../../common/entity-utils";
import {downloadTextFile} from "../../../common/file-utils";
import {getEnumName} from "../../../common/services/enums/index";
import {relationshipKind} from "../../../common/services/enums/relationship-kind";


const bindings = {
    rows: "<",
    onRowSelect: "<",
    selectedRow: "<"
};


const columnDefs = [
    {
        field: "a.name",
        name: "From"
    }, {
        field: "a.type",
        name: "(From Type)"
    }, {
        field: "relationship.relationship",
        name: "Relationship",
        cellFilter: "toDisplayName:'relationshipKind'"
    }, {
        field: "b.name",
        name: "To"
    }, {
        field: "b.type",
        name: "(To Type)"
    }
];

const initialState = {
    rows: [],
    columnDefs,
    onRowSelect: (row) => console.log("default on row select", { row })
};


function mkExportData(rows = []) {
    const columnNames = [[
        "From",
        "From type",
        "To",
        "To type",
        "Relationship",
        "Description",
        "Last Updated At",
        "Last Updated By"
    ]];

    const exportData = _.map(rows, r => [
        r.a.name,
        r.a.type,
        r.b.name,
        r.b.type,
        getEnumName(relationshipKind, r.relationship.relationship),
        r.relationship.description,
        r.relationship.lastUpdatedAt,
        r.relationship.lastUpdatedBy
    ]);

    return columnNames.concat(exportData);
}



function controller() {
    const vm = initialiseData(this, initialState);

    vm.isSelected = (row) => {
        if (vm.selectedRow) {
            const sameA = sameRef(row.a, vm.selectedRow.a, { skipChecks: true });
            const sameB = sameRef(row.b, vm.selectedRow.b, { skipChecks: true });
            return sameA && sameB;
        } else {
            return false;
        }
    };


    vm.export = () => {
        const data = mkExportData(vm.rows);
        downloadTextFile(data, ",", "related_viewpoints.csv");
    };
}


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzRelatedMeasurablesTable"
};