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

import d3 from "d3";
import _ from "lodash";
import angular from "angular";
import {authoritativeRatingColorScale} from "../../../common/colors.js";
import {pickWorst} from "../../../auth-sources/services/auth-sources-utils.js";
import {appRatingCellSelected, appSelected} from "./events.js";

function prepareAppScale(flowsByAppThenTypeThenRating, height) {
    const appIds = _.chain(flowsByAppThenTypeThenRating)
        .map('app')
        .sortBy('name')
        .map('id')
        .value();

    return d3.scale.ordinal()
        .domain(appIds)
        .rangeBands([0, height], 0.11);
}


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

    resize(width) {
        //console.log('resizing', width);
    }

    render(data, displayNameService, eventDispatcher) {
        const rowHeight = 24;
        const appLabelWidth = 200;
        const headerHeight = 150;

        const appCount = data.flowsByAppThenTypeThenRating.length;
        const width = this.vizElem.offsetWidth;
        const height = (appCount * rowHeight) + headerHeight;
        this.svg.attr({
            height,
            width
        });

        const typeScale = prepareTypeScale(data.dataTypes, width - appLabelWidth - 10);
        const appScale = prepareAppScale(data.flowsByAppThenTypeThenRating, height - headerHeight);


        this.svg.selectAll('.data-type-title')
            .data(data.dataTypes)
            .enter()
            .append('text')
            .attr({
                transform: d=> `translate(${appLabelWidth + typeScale(d) + typeScale.rangeBand() / 3}, ${headerHeight - 5}) rotate(-50)`
            })
            .text(d => displayNameService.lookup('dataType', d));

        const appRows = this.svg
            .selectAll('.app-row')
            .data(data.flowsByAppThenTypeThenRating, a => a.key);

        const newAppRows = appRows
            .enter()
            .append('g')
            .classed('app-row', true);

        appRows.attr({
            transform: d => `translate(0, ${headerHeight + appScale(d.key)})`
        });

        newAppRows.append('text')
            .classed('clickable', true)
            .attr({
                'text-anchor': 'end',
                x: appLabelWidth - 5,
                y: appScale.rangeBand() / 2,
                dy: '0.4em'
            })
            .text(d => d.app.name)
            .on('click', d => eventDispatcher.dispatch(appSelected(d)));

        const typeCells = appRows.selectAll('.type-cell')
            .data(d => d.values, d => d.key);

        typeCells.enter()
            .append('rect')
            .classed('type-cell', true)
            .classed('clickable', true)
            .on('click', d => eventDispatcher.dispatch(appRatingCellSelected(d)));

        typeCells.attr({
            x: d => appLabelWidth + typeScale(d.key),
            width: typeScale.rangeBand(),
            height: appScale.rangeBand(),
            fill: d => {
                const ratings = _.map(d.values, 'key');
                const worstRating = pickWorst(ratings);
                return authoritativeRatingColorScale(worstRating).brighter();
            },
            stroke: d => {
                const ratings = _.map(d.values, 'key');
                const worstRating = pickWorst(ratings);
                return authoritativeRatingColorScale(worstRating);
            }
        });

    }
}


// ---DIRECTIVE------------------------------------------------------


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
