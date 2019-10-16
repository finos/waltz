/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import * as colors from "../../../common/colors";
import {refToString} from "../../../common/entity-utils";

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
                color: colors.amber
            };
        case 1:
            return {
                code: iconCodes.file,
                description: "One linked physical files",
                color: colors.black
            };
        case 2:
            return {
                code: iconCodes.files,
                description: "Two linked physical files",
                color: colors.black
            };
        default:
            return {
                code: iconCodes.folder,
                description: "Several linked physical files",
                color: colors.black
            };
    }
}


function toCUIcon(count = 0) {
    switch (count) {
        case 0:
            return {
                code: "",
                description: "",
                color: "#000"
            };
        default:
            return {
                code: iconCodes.hourglass,
                description: "Changes are associated with this flow",
                color: "#000"
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