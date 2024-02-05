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

import * as dsv from "d3-dsv";
import {isIE} from "./browser-utils";

export function downloadTextFile(dataRows = [],
                                 delimiter = ",",
                                 fileName = "download.csv") {
    const fileContent = dsv
        .dsvFormat(delimiter)
        .formatRows(dataRows);

    downloadFile(fileContent, fileName);
}


function determineMimeType(format) {
    switch (format) {
        case "CSV":
            return "application/octet-stream;charset=utf-8";
        case "XLSX":
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        case "JSON":
            return "application/json";
        case "SVG":
            return "image/svg+xml";
        default:
            throw `Cannot determine mime type for format: ${format}`;
    }
}


export function downloadFile(rawContent,
                             fileName = "download.csv",
                             format = "CSV") {

    const doc = document;
    const a = doc.createElement("a");
    const strMimeType = determineMimeType(format);
    const fileContent = format === "JSON"
        ? JSON.stringify(rawContent, " ", 2)
        : rawContent;

    let rawFile;

    // IE10+
    if (navigator.msSaveBlob) {
        return navigator.msSaveOrOpenBlob(
            new Blob(
                [fileContent],
                { type: strMimeType } ),
            fileName
        );
    }

    if (isIE()) {
        const frame = doc.createElement("iframe");
        document.body.appendChild(frame);

        frame.contentWindow.document.open("text/html", "replace");
        frame.contentWindow.document.write(fileContent);
        frame.contentWindow.document.close();
        frame.contentWindow.focus();
        frame.contentWindow.document.execCommand("SaveAs", true, fileName);

        document.body.removeChild(frame);
        return true;
    }

    //html5 A[download]
    if ("download" in a) {
        const blob = new Blob(
            [fileContent],
            {type: strMimeType}
        );
        rawFile = URL.createObjectURL(blob);
        a.setAttribute("download", fileName);
    } else {
        rawFile = "data:" + strMimeType + "," + encodeURIComponent(fileContent);
        a.setAttribute("target", "_blank");
    }

    a.href = rawFile;
    a.setAttribute("style", "display:none;");
    doc.body.appendChild(a);
    setTimeout(function() {
        if (a.click) {
            a.click();
            // Workaround for Safari 5
        } else if (document.createEvent) {
            const eventObj = document.createEvent("MouseEvents");
            eventObj.initEvent("click", true, true);
            a.dispatchEvent(eventObj);
        }
        doc.body.removeChild(a);

    }, 100);
}

