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
import {timeFormat} from "d3-time-format";
import {variableScale} from "../../../common/colors";
import {initialiseData} from "../../../common";
import template from "./entity-statistic-history-panel.html";
import {numberFormatter} from "../../../common/string-utils";


const bindings = {
    history: "<",
    definition: "<",
    duration: "<",
    onChangeDuration: "<"
};


const initialState = {
    duration: "MONTH"
};


const dateFormatter = timeFormat("%a %d %b %Y");


function prepareData(data = []) {
    return _.chain(data)
        .flatMap(
            d => _.map(d.tallies, t => {
                return {
                    series: t.id,
                    count: t.count,
                    date: new Date(d.lastUpdatedAt)
                };
            }))
        .orderBy(d => d.date)
        .value();
}


function getOutcomeIds(data = []) {
    return _.chain(data)
        .flatMap("tallies")
        .map(d => d.id)
        .uniq()
        .value();
}


function prepareStyles(data = []) {
    const reducer = (acc, outcomeId) => {
        acc[outcomeId] = { color: variableScale(outcomeId).toString() };
        return acc;
    };
    return _.reduce(
        getOutcomeIds(data),
        reducer,
        {});
}


function findRelevantStats(history = [], d) {
    if (! d) return null;

    const soughtTime = d.getTime();
    return _.find(
        history,
        t => soughtTime === new Date(t.lastUpdatedAt).getTime());
}


function lookupStatColumnName(displayNameService, definition) {
    return definition
        ? displayNameService.lookup("rollupKind", definition.rollupKind)
        : "Value";
}


function calcTotal(stats = { tallies: [] }) {
    return _.sumBy(stats.tallies, d => d.count);
}


function controller($scope, displayNameService) {
    const vm = initialiseData(this, initialState);

    const highlight = (d) => {
        vm.highlightedDate = d;
        const relevantStats = findRelevantStats(vm.history, d);
        if (relevantStats) {
            vm.selected = relevantStats;
            vm.selected.dateString = dateFormatter(d);
            vm.totalStr = numberFormatter(calcTotal(relevantStats), 2, false);
        }
    };

    vm.onHover = (d) => $scope.$applyAsync(() => highlight(d));

    vm.$onChanges = () => {
        vm.selected  = null;
        vm.points = prepareData(vm.history);
        vm.styles = prepareStyles(vm.history);
        vm.statColumnName = lookupStatColumnName(displayNameService, vm.definition);
    };

}


controller.$inject = [
    "$scope",
    "DisplayNameService"
];


const component = {
    bindings,
    template,
    controller
};


export default component;