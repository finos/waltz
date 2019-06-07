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
import * as _ from 'lodash';


const iconCodes = {
    // codes from: http://fontawesome.io/cheatsheet/  (conversion: &#x1234; -> \u1234)
    files: '\uf0c5',
    file: '\uf016',
    question: '\uf128',
    questionCircle: '\uf29c',
    folder: '\uf115',
    hourglass: '\uf252',
};


function toIcon(count = 0) {
    switch (count) {
        case 0:
            return {
                code: iconCodes.questionCircle,
                description: 'No physical files specified',
                color: '#c66'
            };
        case 1:
            return {
                code: iconCodes.file,
                description: 'One linked physical files',
                color: '#000'
            };
        case 2:
            return {
                code: iconCodes.files,
                description: 'Two linked physical files',
                color: '#000'
            };
        default:
            return {
                code: iconCodes.folder,
                description: 'Several linked physical files',
                color: '#000'
            };
    }
}


function toCUIcon(count = 0) {
    switch (count) {
        case 0:
            return {
                code: '',
                description: '',
                color: '#000'
            };
        default:
            return {
                code: iconCodes.hourglass,
                description: 'Changes are associated with this flow',
                color: '#000'
            };
    }
}


export function mkTweakers(tweakers = {},
                    physicalFlows = [],
                    logicalFlows = [],
                    changeUnits = []) {

    const toIdentifier = (entRef) => `${entRef.kind}/${entRef.id}`;

    const logicalFlowsById = _.keyBy(logicalFlows, 'id');
    const physicalFlowsById = _.keyBy(physicalFlows, 'id');


    const countPhysicalFlows = (direction) =>
        _.countBy(physicalFlows, pf => {
            const logicalFlow = logicalFlowsById[pf.logicalFlowId];
            return logicalFlow
                ? toIdentifier(logicalFlow[direction])
                : null;
        });

    const sourceCounts = countPhysicalFlows('source');
    const targetCounts = countPhysicalFlows('target');

    const physicalFlowChangeUnits = _.filter(
        changeUnits,
        cu => cu.subjectEntity.kind = "PHYSICAL_FLOW");

    // now count change units for apps
    const countChangeUnits = (direction) => _.countBy(physicalFlowChangeUnits, (cu) => {
        const physicalFlow = physicalFlowsById[cu.subjectEntity.id];
        const logicalFlow = logicalFlowsById[physicalFlow.logicalFlowId];
        return logicalFlow
            ? toIdentifier(logicalFlow[direction])
            : null;
    });

    const cuSourceCounts = countChangeUnits('source');
    const cuTargetCounts = countChangeUnits('target');

    tweakers.source.icon = (appRef) => toIcon(sourceCounts[toIdentifier(appRef)]);
    tweakers.target.icon = (appRef) => toIcon(targetCounts[toIdentifier(appRef)]);

    tweakers.source.cuIcon = (appRef) => toCUIcon(cuSourceCounts[toIdentifier(appRef)]);
    tweakers.target.cuIcon = (appRef) => toCUIcon(cuTargetCounts[toIdentifier(appRef)]);

    return Object.assign({} , tweakers);
}