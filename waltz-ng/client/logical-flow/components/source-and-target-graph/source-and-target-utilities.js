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
import * as colors from "../../../common/colors";
import {refToString} from "../../../common/entity-utils";
import {getSymbol} from "../../../common/svg-icon";

const iconCodes = {
    // codes from: http://fontawesome.io/cheatsheet/  (conversion: &#x1234; -> \u1234)
    files: "\uf0c5",
    file: "\uf016",
    question: "\uf128",
    questionCircle: "\uf29c",
    folder: "\uf115",
    hourglass: "\uf252",
};

function toIcon(count = 0) {
    switch (count) {
        case 0:
            return {
                code: iconCodes.questionCircle,
                description: "No physical files specified",
                color: colors.darkAmber,
                svgIcon: getSymbol("questionCircle")
            };
        case 1:
            return {
                code: iconCodes.file,
                description: "One linked physical files",
                color: colors.black,
                svgIcon: getSymbol("page")
            };
        case 2:
            return {
                code: iconCodes.files,
                description: "Two linked physical files",
                color: colors.black,
                svgIcon: getSymbol("pages")
            };
        default:
            return {
                code: iconCodes.folder,
                description: "Several linked physical files",
                color: colors.black,
                svgIcon: getSymbol("folder")
            };
    }
}


function toCUIcon(count = 0) {
    switch (count) {
        case 0:
            return {
                code: "",
                description: "",
                color: "#000",
                svgIcon: getSymbol("fw")
            };
        default:
            return {
                code: iconCodes.hourglass,
                description: "Changes are associated with this flow",
                color: "#000",
                svgIcon: getSymbol("hourglass")
            };
    }
}


export function mkTweakers(tweakers = {},
                           physicalFlows = [],
                           logicalFlows = [],
                           changeUnits = []) {

    const logicalFlowsById = _.keyBy(logicalFlows, d => d.id);
    const physicalFlowsById = _.keyBy(physicalFlows, d => d.id);

    const countPhysicalFlows = (nodeSelectionFn) =>
        _.countBy(physicalFlows, pf => {
            const logicalFlow = logicalFlowsById[pf.logicalFlowId];
            return logicalFlow
                ? refToString(nodeSelectionFn(logicalFlow))
                : null;
        });

    const pfSourceCounts = countPhysicalFlows(lf => lf.source);
    const pfTargetCounts = countPhysicalFlows(lf => lf.target);

    const physicalFlowChangeUnits = _.filter(
        changeUnits,
        cu => cu.subjectEntity.kind = "PHYSICAL_FLOW");

    // now count change units for apps
    const countChangeUnits = (nodeSelectionFn) => _.countBy(physicalFlowChangeUnits, (cu) => {
        const physicalFlow = physicalFlowsById[cu.subjectEntity.id];
        if (physicalFlow) {
            const logicalFlow = logicalFlowsById[physicalFlow.logicalFlowId];
            return logicalFlow
                ? refToString(nodeSelectionFn(logicalFlow))
                : null;
        } else {
            return null;
        }
    });

    const cuSourceCounts = countChangeUnits(lf => lf.source);
    const cuTargetCounts = countChangeUnits(lf => lf.target);

    tweakers.source.pfIcon = (appRef) => toIcon(pfSourceCounts[refToString(appRef)]);
    tweakers.target.pfIcon = (appRef) => toIcon(pfTargetCounts[refToString(appRef)]);

    tweakers.source.cuIcon = (appRef) => toCUIcon(cuSourceCounts[refToString(appRef)]);
    tweakers.target.cuIcon = (appRef) => toCUIcon(cuTargetCounts[refToString(appRef)]);

    return Object.assign({} , tweakers);
}