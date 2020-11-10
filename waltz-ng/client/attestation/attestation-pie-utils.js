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

import { attestationStatusColorScale } from "../common/colors";
import { toKeyCounts } from "../common";
import * as _ from "lodash";
import moment from "moment";

const attestationStatus = {
    ATTESTED: {
        key: "ATTESTED",
        name: "Attested",
        description: "This flow has been attested",
        position: 10
    },
    NEVER_ATTESTED: {
        key: "NEVER_ATTESTED",
        name: "Never Attested",
        icon: null,
        description: "This flow has never been attested",
        position: 20
    }
};


export const attestationPieConfig = {
    colorProvider: (d) => attestationStatusColorScale(d.key),
    size: 40,
    labelProvider: (d) => attestationStatus[d.key] ? attestationStatus[d.key].name : "Unknown"
};



export function prepareSummaryData(applications = [], selectedYear = null) {
    const ALL_YEARS = 0;

    if (selectedYear === null || selectedYear === ALL_YEARS) {
        return toKeyCounts(applications, a => a.isAttested)
    } else if (selectedYear > ALL_YEARS) {
        const appDataForSummary = _.filter(
            applications,
            a => a.attestation
                ? moment(a.attestation.attestedAt, "YYYY-MM-DD").year() === selectedYear
                : true);
        return toKeyCounts(appDataForSummary, a => a.isAttested)
    }
}


