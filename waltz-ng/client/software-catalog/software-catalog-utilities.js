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

import _ from "lodash";

export function countByVersionId(usages = []) {
    // console.time('counting usages')
    const countsByVersionId =  _
        .chain(usages)
        .map(u => Object.assign({}, _.pick(u, ["softwareVersionId", "applicationId"])))
        .uniqBy(u => "v:" + u.softwareVersionId + "_a:" + u.applicationId)
        .countBy(u => u.softwareVersionId)
        .value();

    // console.timeEnd('counting usages')
    return countsByVersionId;
}


export function countByVersionsByPackageId(usages = []) {
    // console.time('countByVersionsByPackageId')
    const countsByPackageId =  _
        .chain(usages)
        .map(u => Object.assign({}, _.pick(u, ["softwarePackageId", "softwareVersionId"])))
        .uniqBy(u => "sp:" + u.softwarePackageId + "_v:" + u.softwareVersionId)
        .countBy(u => u.softwarePackageId)
        .value();

    // console.timeEnd('countByVersionsByPackageId')
    return countsByPackageId;
}