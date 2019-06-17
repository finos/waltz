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

import _ from 'lodash';
import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from "../../../common/index";
import {color} from "d3-color";
import {blue, green, red, lightGrey} from "../../../common/colors";
import {findUnknownDataType} from '../../../data-types/data-type-utils';

import template from './logical-flows-data-type-summary-pane.html';


const bindings = {
    stats: '<'
};


const initialState = {
    visibility: {
        detail: false
    }
};


function prepareSummary(counts = [], unknownId, direction) {
    return _
        .chain(counts)
        .map(d => ({ typeId: d.dataType.id, name: d.dataType.name, parentId: d.dataType.parentId, count: d[direction] }))
        .reduce((acc, d) => {
            console.log("prepareSummary->counts " + JSON.stringify(d))
            if (d.typeId === Number(unknownId)) {
                acc.UNKNOWN  += d.count;
            // } else if (d.parentId == null){
            //     acc.LEVEL1 += d.count;
            } else if (d.name.startsWith('DEPRECATED')){
                acc.DEPRECATED += d.count;
            } else {
                acc.KNOWN += d.count;
            }
            return acc;
        }, { KNOWN: 0, UNKNOWN : 0, DEPRECATED : 0, LEVEL1 : 0})
        .map((v, k) => ({ key: k, count: v }))
        .value();
}


function controller(displayNameService, logicalFlowUtilityService, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadUnknownDataType = () => {
        return serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            //.then(r=> console.log(" DATA " + JSON.stringify(r.data)))
            .then(r => findUnknownDataType(r.data));
    };

    // const loadDeprecatedDataType = () => {
    //     return serviceBroker
    //         .loadAppData(CORE_API.DataTypeStore.findAll)
    //         //.then(r=> console.log(" DATA " + JSON.stringify(r.data)))
    //         .then(r => findDeprecatedDataType(r.data));
    // };

    vm.$onChanges = () => {


        console.log(" DATA " + JSON.stringify(loadUnknownDataType));

        if (! vm.stats) return;

        vm.enrichedDataTypeCounts = logicalFlowUtilityService.enrichDataTypeCounts(
            vm.stats.dataTypeCounts,
            displayNameService);
        console.log(" enrichedDataTypeCounts " + JSON.stringify( vm.enrichedDataTypeCounts));
        loadUnknownDataType()
            .then(unknownDataType => {

                const unknownId = unknownDataType ? unknownDataType.id : null;
                if (unknownId) {

                    vm.visibility.summaries = true;
                    vm.summaryConfig =  {
                        colorProvider: (d) => {
                            // console.log(JSON.stringify(d))
                            console.log(" datatype " + JSON.stringify(d))
                            if(d.data.key === 'KNOWN') {
                                return color(green);
                            } else if (d.data.key.startsWith('LEVEL1')) {
                                return color(lightGrey);
                            } else if (d.data.key.startsWith('DEPRECATED')) {
                                return color(blue);
                            } else {
                                return color(red);
                            }
                        },
                        valueProvider: (d) => d.count,
                        idProvider: (d) => d.data.key,
                        labelProvider: d => _.capitalize(d.key),
                        size: 40
                    };

                    vm.overviewConfig =  Object.assign({}, vm.summaryConfig, { size: 80 });

                    const summaries = [
                        { title: 'Intra', prop: 'intra'} ,
                        { title: 'Inbound', prop: 'inbound'} ,
                        { title: 'Outbound', prop: 'outbound'} ,
                        { title: 'All', prop: 'total'}
                    ];

                    vm.summaries= _.map(summaries, d => {
                        return {
                            summary: prepareSummary(vm.enrichedDataTypeCounts, unknownId, d.prop),
                            title: d.title
                        }
                    });

                }
            });
    }
}


controller.$inject = [
    'DisplayNameService',
    'LogicalFlowUtilityService',
    'ServiceBroker',
];


const component = {
    bindings,
    controller,
    template
};




export default {
    component,
    id: 'waltzLogicalFlowsDataTypeSummaryPane'
}