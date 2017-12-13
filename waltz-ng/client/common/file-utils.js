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


export function downloadFile(fileContent, fileName = 'download.csv') {
    const D = document;
    const a = D.createElement('a');
    const strMimeType = 'application/octet-stream;charset=utf-8';
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
        const frame = D.createElement('iframe');
        document.body.appendChild(frame);

        frame.contentWindow.document.open('text/html', 'replace');
        frame.contentWindow.document.write(fileContent);
        frame.contentWindow.document.close();
        frame.contentWindow.focus();
        frame.contentWindow.document.execCommand('SaveAs', true, fileName);

        document.body.removeChild(frame);
        return true;
    }

    //html5 A[download]
    if ('download' in a) {
        const blob = new Blob(
            [fileContent],
            {type: strMimeType}
        );
        rawFile = URL.createObjectURL(blob);
        a.setAttribute('download', fileName);
    } else {
        rawFile = 'data:' + strMimeType + ',' + encodeURIComponent(fileContent);
        a.setAttribute('target', '_blank');
    }

    a.href = rawFile;
    a.setAttribute('style', 'display:none;');
    D.body.appendChild(a);
    setTimeout(function() {
        if (a.click) {
            a.click();
            // Workaround for Safari 5
        } else if (document.createEvent) {
            const eventObj = document.createEvent('MouseEvents');
            eventObj.initEvent('click', true, true);
            a.dispatchEvent(eventObj);
        }
        D.body.removeChild(a);

    }, 100);
}