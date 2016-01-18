/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

import _ from 'lodash';
import d3 from 'd3';

import { authoritativeRatingColorScale } from '../../../common/colors.js';
import { orgUnitSelected, orgUnitRatingSelected } from './events.js';


function prepareTypeScale(dataTypes, width) {
    return d3.scale.ordinal()
        .domain(_.sortBy(dataTypes))
        .rangeBands([0, width], 0.1);
}

class Chart {

    constructor(vizElem) {
        this.vizElem = vizElem;
        this.svg = d3.select(vizElem)
            .append('svg')
        ;
    }

    render(data, displayNameService, eventDispatcher) {
        const rowHeight = 24;
        const orgLabelWidth = 200;
        const headerHeight = 150;

        const orgCount = data.children.length;
        const width = this.vizElem.offsetWidth;
        const height = (orgCount * rowHeight) + headerHeight;
        this.svg.attr({
            height,
            width
        });

        const typeScale = prepareTypeScale(data.dataTypes, width - orgLabelWidth - 10);


        const orgScale = d3.scale.ordinal()
            .domain(_.map(data.children, 'id'))
            .rangeBands([0, height - headerHeight], 0.11);

        this.svg.selectAll('.data-type-title')
            .data(data.dataTypes)
            .enter()
            .append('text')
            .attr({
                transform: d=> `translate(${orgLabelWidth + typeScale(d) + typeScale.rangeBand() / 3}, ${headerHeight - 5}) rotate(-50)`
            })
            .text(d => displayNameService.lookup('dataType', d));

        const orgRows = this.svg
            .selectAll('.org-row')
            .data(data.children, a => a.id);

        const newOrgRows = orgRows
            .enter()
            .append('g')
            .classed('org-row', true);

        orgRows.attr({
            transform: d => `translate(0, ${headerHeight + orgScale(d.id)})`
        });

        newOrgRows.append('text')
            .attr({
                'text-anchor': 'end',
                x: orgLabelWidth - 5,
                y: orgScale.rangeBand() / 2,
                dy: '0.4em'
            })
            .classed('clickable', true)
            .text(d => d.orgUnit.name)
            .on('click', d => eventDispatcher.dispatch(orgUnitSelected(d)));

        const typeCells = orgRows.selectAll('.type-cell')
            .data(d => _.map(d.stats, s => ({...s, orgUnit: d.orgUnit })), d => d.key);  // need to copy the orgUnit down so event listeners will receive it

        typeCells.enter()
            .append('g')
            .classed('type-cell', true)
            .classed('clickable', true)
            .on('click', d => eventDispatcher.dispatch(orgUnitRatingSelected(d)));

        typeCells.attr({
            transform: d => `translate(${orgLabelWidth + typeScale(d.key)}, 0)`
        });

        typeCells.each(function(d) {
            const total = _.chain(d.values).map('values').sum().value();
            const barScale = d3.scale.linear().domain([0, total]).range([0, typeScale.rangeBand()]);

            _.foldr(
                ['NO_OPINION', 'SECONDARY', 'PRIMARY', 'DISCOURAGED'],
                (acc, rating) => {

                    // note, acc is the starting x value.
                    const item = (_.findWhere(d.values, {key: rating}));

                    if (item) {
                        const w = barScale(item.values);
                        d3.select(this)
                            .append('rect')
                            .attr({
                                x: acc,
                                width: w,
                                height: orgScale.rangeBand(),
                                fill: authoritativeRatingColorScale(rating).brighter(),
                                stroke: authoritativeRatingColorScale(rating)
                            });

                        return acc + w;
                    }
                    return acc;
                },
                0);
        });
    }
}


function link(scope, elem) {
    scope.vizElem = elem[0].querySelector('.viz');
    scope.chart = new Chart(scope.vizElem);
}


function controller($scope, $window, displayNameService) {

    const waitingForData = () => {
        const allLoaded = this.data;
        return !allLoaded;
    };


    const debouncedRender = _.debounce(() => {
        if (waitingForData()) return;

        $scope.chart.render(this.data, displayNameService, this.eventDispatcher);
    }, 100);

    debouncedRender();

    angular.element($window).on('resize', () => debouncedRender);
    $scope.$watch('ctrl.data', debouncedRender);
}

controller.$inject = ['$scope', '$window', 'WaltzDisplayNameService'];



function directive() {
    return {
        restrict: 'E',
        template: '<div class="viz"></div>',
        link,
        scope: {
            data: '=',
            eventDispatcher: '='
        },
        controller,
        controllerAs: 'ctrl',
        bindToController: true
    };
}

export default directive;