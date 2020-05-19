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

import {initialiseData} from "../../../common";
import {authoritativeRatingColorScale} from "../../../common/colors";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./data-type-flow-section.html";
import {entity} from "../../../common/services/enums/entity";
import {
    filterUtils,
    getSelectedTagsFromPreferences,
    maybeAddUntaggedFlowsTag,
    saveTagFilterPreferences
} from "../../../logical-flow/logical-flow-utils";
import {groupLogicalFlowFilterExcludedTagIdsKey} from "../../../user/services/user-preference-service";


const bindings = {
    filters: "<",
    parentEntityRef: "<"
};


const initialState = {
    flowData: {},
    filterOptions: {
        showPrimary: true,
        showSecondary: true,
        showDiscouraged: false,
        showNoOpinion: false,
        selectedTags: filterUtils.defaultOptions.selectedTags
    },
    tags: []
};


function calculateEntities(flows = []) {
    return _.chain(flows)
        .flatMap(f => [f.source, f.target])
        .uniqBy("id")
        .value();
}


const buildGraphTweakers = (decorators = [],
                            onAppSelect) => {
    const decoratorsByFlowId = _.keyBy(decorators, "dataFlowId");

    const getRatingForLink = (link) => {
        const decorator = decoratorsByFlowId[link.data.id];
        return decorator
            ? decorator.rating
            : "NO_OPINION";
    };

    return {
        node : {
            enter: (selection) => {
                selection
                    .on("click.appSelect", onAppSelect)
                    .on("dblclick.unfix", d => { d.fx = null; d.fy = null; })
            },
            exit: _.identity,
            update: _.identity
        },
        link : {
            enter: (selection) => {
                selection
                    .attrs({
                        stroke: (d) => {
                            const rating = getRatingForLink(d);
                            return authoritativeRatingColorScale(rating);
                        },
                        fill: (d) => {
                            const rating = getRatingForLink(d);
                            return authoritativeRatingColorScale(rating).brighter();
                        }
                    });
            },
            exit: _.identity,
            update: _.identity
        }
    };
};


function mkKeepDecoratorFn(filterOptions = {}) {
    return (decorator) => {
        const rating = decorator.rating;
        switch (rating) {
            case "PRIMARY":
                return filterOptions.showPrimary;
            case "SECONDARY":
                return filterOptions.showSecondary;
            case "DISCOURAGED":
                return filterOptions.showDiscouraged;
            case "NO_OPINION":
                return filterOptions.showNoOpinion;
        }
    };
}


function filterDecorators(decorators =[],
                          filterOptions) {
    return _.filter(decorators, mkKeepDecoratorFn(filterOptions));
}


function filterFlows(flows = [],
                     decorators = [],
                     allTags = [],
                     selectedTags = []) {
    const flowIds = _.map(decorators, "dataFlowId");
    const tagFilterFn = filterUtils.mkTagFilterFn(selectedTags, allTags, flows);
    return _.filter(flows, f => _.includes(flowIds, f.id) && tagFilterFn(f));
}


function filterData(flows = [],
                    decorators = [],
                    allTags = [],
                    filterOptions) {
    const filteredDecorators = filterDecorators(decorators, filterOptions);
    const filteredFlows = filterFlows(flows, filteredDecorators, allTags, filterOptions.selectedTags);
    const filteredEntities = calculateEntities(filteredFlows);
    return {
        entities: filteredEntities,
        flows: filteredFlows,
        decorators: filteredDecorators
    };
}


function controller($q, $scope, userPreferenceService, serviceBroker) {
    const vm = initialiseData(this, initialState);
    const onAppSelect = (app) => $scope.$applyAsync(() => vm.selectedApp = app);

    const loadAll = () => {
        const selector = mkSelectionOptions(
            vm.parentEntityRef,
            undefined,
            undefined,
            vm.filters);

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ selector ])
            .then(r => vm.rawFlows = r.data);

        const decoratorPromise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findBySelector,
                [ selector, entity.LOGICAL_DATA_FLOW.key ])
            .then(r => {
                vm.rawDecorators = r.data;
                vm.graphTweakers = buildGraphTweakers(
                    vm.rawDecorators,
                    onAppSelect);
            });

        const tagsPromise = serviceBroker
            .loadViewData(
                CORE_API.TagStore.findTagsByEntityKindAndTargetSelector,
                [ entity.LOGICAL_DATA_FLOW.key, selector ])
            .then(r => {
                vm.tags = maybeAddUntaggedFlowsTag(r.data);

                return getSelectedTagsFromPreferences(
                    vm.tags,
                    groupLogicalFlowFilterExcludedTagIdsKey,
                    userPreferenceService)
                    .then(selectedTags => {
                        vm.filterOptions.selectedTags = selectedTags;
                    });
            });

        $q.all([flowPromise, decoratorPromise, tagsPromise])
            .then(() => vm.filterChanged());
    };


    vm.filterChanged = () => {
        vm.flowData = filterData(
            vm.rawFlows,
            vm.rawDecorators,
            vm.tags,
            vm.filterOptions);
    };

    vm.showAllRatings = () => {
        vm.filterOptions.showPrimary = true;
        vm.filterOptions.showSecondary = true;
        vm.filterOptions.showDiscouraged = true;
        vm.filterOptions.showNoOpinion = true;

        vm.filterChanged();
    };

    vm.showAllTags = () => {
      vm.filterOptions.selectedTags = vm.tags;
      vm.filterChanged();

      saveTagFilterPreferences(
        vm.tags,
        vm.filterOptions.selectedTags,
        groupLogicalFlowFilterExcludedTagIdsKey,
        userPreferenceService);
    };

    vm.onTagsChange = () => {
        vm.filterChanged();

        saveTagFilterPreferences(
            vm.tags,
            vm.filterOptions.selectedTags,
            groupLogicalFlowFilterExcludedTagIdsKey,
            userPreferenceService);
    };

    vm.refocusApp = app => {
        onAppSelect(app);
    };

    vm.$onInit = () => {
        loadAll();
    };

    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadAll();
        }
    };
}


controller.$inject = [
    "$q",
    "$scope",
    "UserPreferenceService",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


const id = "waltzDataTypeFlowSection"

export default {
    component,
    id
};