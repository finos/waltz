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

import {initialiseData} from "../../../common";
import template from "./change-summaries-panel.html";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import CalendarHeatmap from "../../../common/svelte/calendar-heatmap/CalendarHeatmap.svelte"
import moment from "moment";
import _ from "lodash";

const modes = {
    LOADING: "LOADING",
    NO_SELECTION: "NO_SELECTION",
    DATE_SELECTED: "DATE_SELECTED",
    DETAIL_SELECTED: "DETAIL_SELECTED"
};


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    modes,
    mode: modes.NO_SELECTION,
    selectedDate: null,
    heatmapData: null,
    summaries: [],
    CalendarHeatmap
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        vm.mode = modes.LOADING;
        serviceBroker
            .loadViewData(
                CORE_API.ChangeLogStore.findSummaries,
                ["APPLICATION", selectionOptions, 365])
            .then(r => {
                vm.heatmapData = r.data;
                vm.mode = modes.NO_SELECTION;
            });
    };

    vm.onSelectDate = (date) => {
        const dateStr = moment(date).format("YYYY-MM-DD");
        vm.startDate = dateStr;
        vm.endDate = dateStr;
        loadChangeSummariesForDateRange();
        vm.mode = modes.DATE_SELECTED;
    };

    vm.onSelectDateRange = (dates) => {
        vm.startDate = moment(_.min(dates)).format("YYYY-MM-DD");
        vm.endDate = moment(_.max(dates)).format("YYYY-MM-DD");
        loadChangeSummariesForDateRange();
        vm.mode = modes.DATE_SELECTED;
    };

    vm.onDetailSelect = (ref, startDate, endDate) => {
        vm.mode = modes.LOADING;
        serviceBroker
            .loadViewData(
                CORE_API.ChangeLogStore.findByEntityReferenceForDateRange,
                [ref, startDate, endDate])
            .then(r => {
                vm.detail = {
                    ref,
                    entries: r.data,
                    startDate,
                    endDate
                };
                vm.mode = modes.DETAIL_SELECTED;
            })
            .catch(() => vm.mode = modes.DATE_SELECTED);
    };

    vm.onClearSelectedDate = () => {
        vm.selectedDate = null;
        vm.mode = modes.NO_SELECTION;
    };


    vm.onClearSelectedDetail = () => {
        vm.detail = null;
        vm.mode = modes.DATE_SELECTED;
    };


    function loadChangeSummariesForDateRange() {
        vm.mode = modes.LOADING;
        serviceBroker
            .loadViewData(
                CORE_API.ChangeLogSummariesStore.findSummariesForKindBySelectorForDateRange,
                ["APPLICATION", mkSelectionOptions(vm.parentEntityRef), vm.startDate, vm.endDate])
            .then(r => {
                vm.summaries = r.data;
            });
    }
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller,
    transclude: {
        "noData": "?noData"
    }
};


export default {
    id: "waltzChangeSummariesPanel",
    component
}