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
import {initialiseData} from "../../../common/index";
import template from './auth-sources-summary-panel.html';
import {CORE_API} from "../../../common/services/core-api-utils";
import {arc, pie} from "d3-shape";
import {select} from "d3-selection";
import {authoritativeRatingColorScale} from "../../../common/colors";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    rowInfo: _.map(
        ['PRIMARY', 'SECONDARY', 'DISCOURAGED', 'NO_OPINION'],
        r => ({
            rating: r,
            style: {
                'border-radius': '2px',
                'border-color': authoritativeRatingColorScale(r).toString(),
                'background-color': authoritativeRatingColorScale(r).brighter(2).toString()
            }
        })),
    visibility: {
        chart: false
    }
};


const h = 130;
const w = 60;

const inboundOptions = {
    selector: '#wassp-inbound',
    transform: `translate(${w}, ${h / 2})`,
    startAngle: Math.PI,
    endAngle: 2 * Math.PI
};


const outboundOptions = {
    selector: '#wassp-outbound',
    transform: `translate(0, ${h / 2})`,
    startAngle: Math.PI,
    endAngle: 0
};

const baseStats = {
    PRIMARY: 0,
    SECONDARY: 0,
    DISCOURAGED: 0,
    NO_OPINION: 0
};


function toStats(data = []) {
    const stats = Object.assign({}, baseStats);
    return _.reduce(data, (acc, d) => {
        acc[d.rating] = acc[d.rating] += d.count;
        return acc;
    }, stats);
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const drawPie = (rawStats, options) => {


        const svg = select(options.selector)
            .append('svg')
            .attr('width', w)
            .attr('height', h);

        const g = svg.append('g')
            .attr('transform', options.transform);

        const isEmpty = _.sum(_.values(rawStats)) == 0;

        if (isEmpty) {
            return;
        } else {
            const pieStats= _.map(rawStats, (value, key) => ({value, key}));

            const pieData = pie()
                .value(d => d.value)
                .startAngle(options.startAngle)
                .endAngle(options.endAngle)
                (pieStats);

            const pieArc = arc()
                .outerRadius(w - 10)
                .innerRadius(w * 0.4)
                .padAngle(0.07)
                .cornerRadius(0);

            g.selectAll('.arc')
                .data(pieData)
                .enter()
                .append('path')
                .classed('arc', true)
                .attr('fill', d => authoritativeRatingColorScale(d.data.key).brighter())
                .attr('stroke', d => authoritativeRatingColorScale(d.data.key))
                .attr('d', d => pieArc(d));
        }
    };

    const determineIfChartShouldBeVisible = (p1, p2) => {
        $q.all([p1, p2])
            .then(() => {
                const inCount = _.sum(_.values(vm.inboundStats));
                const outCount = _.sum(_.values(vm.outboundStats));
                vm.visibility.chart = (inCount + outCount) > 0;
            });
    };

    const loadSummaryStats = () => {
        const inboundPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.summarizeInboundBySelector,
                [ { entityReference: vm.parentEntityRef, scope: 'CHILDREN' }])
            .then(r => {
                vm.inboundStats = toStats(r.data);
                drawPie(vm.inboundStats, inboundOptions);
            });

        const outboundPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.summarizeOutboundBySelector,
                [ { entityReference: vm.parentEntityRef, scope: 'CHILDREN' }])
            .then(r => {
                vm.outboundStats = toStats(r.data);
                drawPie(vm.outboundStats, outboundOptions);
            });

        determineIfChartShouldBeVisible(inboundPromise, outboundPromise);
    };

    vm.$onInit = () => {
        loadSummaryStats();
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker'
];


export const component = {
    bindings,
    template,
    controller
};


export const id = 'waltzAuthSourcesSummaryPanel';
