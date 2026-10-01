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

import * as XLSX from "xlsx";
import {downloadFile} from "../../../common/file-utils";
import {buildSheetData} from "./analytics-export-mappings";


export function buildAnalyticsWorkbook(datasets) {
    const wb = XLSX.utils.book_new();
    buildSheetData(datasets).forEach(({name, rows}) => {
        const ws = rows.length > 0
            ? XLSX.utils.json_to_sheet(rows)
            : XLSX.utils.aoa_to_sheet([["No data"]]);
        XLSX.utils.book_append_sheet(wb, ws, name);
    });
    return wb;
}


export function downloadAnalyticsWorkbook(datasets, filename = "analytics-dashboard") {
    const wb = buildAnalyticsWorkbook(datasets);
    const wbout = XLSX.write(wb, {bookType: "xlsx", type: "array"});
    downloadFile(wbout, `${filename}.xlsx`, "XLSX");
}
