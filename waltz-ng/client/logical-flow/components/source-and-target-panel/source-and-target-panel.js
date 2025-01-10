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
import {event} from "d3-selection";
import {initialiseData} from "../../../common";
import {downloadTextFile} from "../../../common/file-utils";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkTweakers} from "../source-and-target-graph/source-and-target-utilities";
import {
    filterUtils,
    getSelectedTagsFromPreferences,
    maybeAddUntaggedFlowsTag,
    saveTagFilterPreferences
} from "../../logical-flow-utils";

import template from "./source-and-target-panel.html";
import {sameRef} from "../../../common/entity-utils";
import {appLogicalFlowFilterExcludedTagIdsKey} from "../../../user";
import {loadFlowClassificationRatings} from "../../../flow-classification-rule/flow-classification-utils";
import ImageDownloadLink from "../../../common/svelte/ImageDownloadLink.svelte";
import FlowRatingCell from "../../../common/svelte/FlowRatingCell.svelte"
import {flowDirection as FlowDirection} from "../../../common/services/enums/flow-direction";

const bindings = {
    entityRef: "<",
    changeUnits: "<",
    logicalFlows: "<",
    decorators: "<",
    physicalFlows: "<",
    physicalSpecifications: "<",
    tags: "<",
    ratingDirection: "<"
};


const defaultFilterOptions = {
    typeIds: filterUtils.defaultOptions.typeIds,
    nodeInfo: "ALL", // {position, entity}
    selectedTags: filterUtils.defaultOptions.selectedTags
};


const initialState = {
    filteredFlowData: {
        flows: [],
        decorators: []
    },
    filterOptions: defaultFilterOptions,
    filterFlags: {
        typeFilterApplied: false,
        nodeFilterApplied: false,
        tagFilterApplied: false
    },
    tags: [],
    flowClassificationsByCode: [],
    ImageDownloadLink,
    diagramElem: null,
    FlowRatingCell
};


function mkNodeFilterFn(nodeInfo) {
    return f => nodeInfo === "ALL" || sameRef(f[nodeInfo.position], nodeInfo.entity);
}


function buildFlowFilter(filterOptions = defaultFilterOptions,
                         allFlows = [],
                         flowDecorators = [],
                         allTags = []) {

    const typeFilterFn = filterUtils.mkTypeFilterFn(flowDecorators);
    const nodeFilterFn = mkNodeFilterFn(filterOptions.nodeInfo);
    const tagFilterFn = filterUtils.mkTagFilterFn(filterOptions.selectedTags, allTags, allFlows);
    return f => typeFilterFn(f) && nodeFilterFn(f) && tagFilterFn(f);
}


function calculateFlowData(allFlows = [],
                           allDecorators = [],
                           allTags = [],
                           filterOptions = defaultFilterOptions) {
    // note order is important.  We need to find decorators first
    const decoratorFilterFn = filterUtils.buildDecoratorFilter(filterOptions.typeIds);
    const decorators = _.filter(allDecorators, decoratorFilterFn);

    const flowFilterFn = buildFlowFilter(filterOptions, allFlows, decorators, allTags);
    const flows = _.filter(allFlows, flowFilterFn);
    return {flows, decorators};
}


function calcPhysicalFlows(physicalFlows = [], specifications = [], logicalFlowId) {
    const specsById = _.keyBy(specifications, "id");

    return _
        .chain(physicalFlows)
        .filter(pf => pf.logicalFlowId === logicalFlowId)
        .map(pf => Object.assign({}, pf, { specification: specsById[pf.specificationId] }))
        .value();
}


// flowId -> [ { id (typeId), rating }... ]
function mkTypeInfo(decorators = [], dataTypes) {
    return _
        .chain(decorators)
        .filter({ decoratorEntity: { kind: "DATA_TYPE" }})
        .groupBy(d => d.dataFlowId)
        .mapValues(xs => _.map(xs, x => {
            return {
                id: x.decoratorEntity.id,
                rating: x.rating,
                inboundRating: x.targetInboundRating,
                name: _.get(dataTypes, x.decoratorEntity.id).name
            };
        }))
        .value();
}

// flowId -> [ {id (tagId), name }... ]
function mkTagInfo(tags = []) {
    return _
        .chain(tags)
        .flatMap(t => {
            return _.map(t.tagUsages, tu => ({
                flowId: tu.entityReference.id,
                tag: {
                    id: t.id,
                    name: t.name
                }
            }));
        })
        .groupBy(ft => ft.flowId)
        .mapValues(xs => _.map(xs, x => ({ id: x.tag.id, name: x.tag.name })))
        .value();
}


function calculateSourceAndTargetFlowsByEntity(primaryEntity, logical = []) {
    if (! primaryEntity) return {};


    const sourceFlowsByEntityId = _
        .chain(logical)
        .filter(f => f.target.id === primaryEntity.id && f.target.kind === primaryEntity.kind)
        .reduce((acc, f) => { acc[f.source.id] = f.id; return acc; }, {})
        .value();

    const targetFlowsByEntityId = _
        .chain(logical)
        .filter(f => f.source.id === primaryEntity.id && f.source.kind === primaryEntity.kind)
        .reduce((acc, f) => { acc[f.target.id] = f.id; return acc; }, {})
        .value();

    return {
        sourceFlowsByEntityId,
        targetFlowsByEntityId
    };
}


function scrollIntoView(element, $window) {
    element.scrollIntoView({
        behavior: "smooth",
        block: "start",
    });
    $window.scrollBy(0, -90);
}


function controller($element,
                    $scope,
                    $window,
                    displayNameService,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    function applyFilter(fn) {
        $scope.$applyAsync(() => vm.filteredFlowData = fn());
    }

    const filterChanged = (filterOptions = vm.filterOptions) => {
        vm.filterOptions = filterOptions;

        applyFilter(() => calculateFlowData(
            vm.logicalFlows,
            vm.decorators,
            vm.tags,
            vm.filterOptions));

        vm.filterFlags = {
            typeFilterApplied: vm.filterOptions.typeIds !== defaultFilterOptions.typeIds,
            nodeFilterApplied: vm.filterOptions.nodeInfo !== defaultFilterOptions.nodeInfo,
            tagFilterApplied: !_.isEmpty(vm.filterOptions.selectedTags) && !_.isEmpty(vm.tags)
                                && vm.filterOptions.selectedTags.length !== vm.tags.length
        }
    };

    const resetTypeFilter = () => {
        if (vm.filterFlags.typeFilterApplied) {
            vm.filterOptions.typeIds = defaultFilterOptions.typeIds;
            filterChanged();
        }
    };

    vm.resetNodeAndTypeFilter = () => {
        vm.filterOptions.nodeInfo = defaultFilterOptions.nodeInfo;
        vm.filterOptions.typeIds = defaultFilterOptions.typeIds;
        filterChanged();
    };

    vm.$onInit = () => {
        loadFlowClassificationRatings(serviceBroker)
            .then(r => {
                vm.flowClassifications = r;

                vm.inboundClassificationsByCode = _
                    .chain(r)
                    .filter(d => d.direction === FlowDirection.INBOUND.key)
                    .keyBy(d => d.code)
                    .value();
                vm.outboundClassificationsByCode = _
                    .chain(r)
                    .filter(d => d.direction === FlowDirection.OUTBOUND.key)
                    .keyBy(d => d.code)
                    .value()

                vm.flowClassificationsByCode = vm.ratingDirection === FlowDirection.INBOUND.key
                    ? vm.inboundClassificationsByCode
                    : vm.outboundClassificationsByCode;
            });

        serviceBroker
            .loadViewData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = _.keyBy(r.data, dt => dt.id));

        vm.diagramElem = _.head($element.find("waltz-source-and-target-graph"));
    };

    vm.$onChanges = (changes) => {

        loadFlowClassificationRatings(serviceBroker)
            .then(r => {
                vm.flowClassifications = r;

                vm.inboundClassificationsByCode = _
                    .chain(r)
                    .filter(d => d.direction === FlowDirection.INBOUND.key)
                    .keyBy(d => d.code)
                    .value();
                vm.outboundClassificationsByCode = _
                    .chain(r)
                    .filter(d => d.direction === FlowDirection.OUTBOUND.key)
                    .keyBy(d => d.code)
                    .value()

                vm.flowClassificationsByCode = vm.ratingDirection === FlowDirection.INBOUND.key
                    ? vm.inboundClassificationsByCode
                    : vm.outboundClassificationsByCode;
            });

        if (changes.logicalFlows || changes.decorators) {
            vm.resetNodeAndTypeFilter();
        }

        if (!_.isEmpty(vm.tags)) {
            vm.tags = maybeAddUntaggedFlowsTag(vm.tags);

            getSelectedTagsFromPreferences(
                vm.tags,
                appLogicalFlowFilterExcludedTagIdsKey,
                serviceBroker)
                .then(selectedTags => {
                    vm.filterOptions.selectedTags = selectedTags;
                    filterChanged();
                });
        }

        const keyedLogicalFlows = calculateSourceAndTargetFlowsByEntity(
            vm.entityRef,
            vm.logicalFlows);

        const logicalFlowsById = _.keyBy(vm.logicalFlows, "id");

        function select(serviceBroker, entity, type, logicalFlowId, evt) {
            const typeInfoByFlowId = mkTypeInfo(vm.decorators, vm.dataTypes);
            const types = typeInfoByFlowId[logicalFlowId] || [];
            const logicalFlow = logicalFlowsById[logicalFlowId];
            const physicalFlows = calcPhysicalFlows(vm.physicalFlows, vm.physicalSpecifications, logicalFlowId);
            const changeUnitsByPhysicalFlowId = _
                .chain(vm.changeUnits)
                .filter(cu => cu.subjectEntity.kind = "PHYSICAL_FLOW")
                .keyBy(cu => cu.subjectEntity.id)
                .value();
            const tagInfoByFlowId = mkTagInfo(vm.tags);
            const tags = tagInfoByFlowId[logicalFlowId] || [];

            serviceBroker
                .loadViewData(CORE_API.LogicalFlowStore.findPermissionsForFlow, [logicalFlowId])
                .then(r => vm.canEditPhysical = _.some(r.data, d => _.includes(["ADD", "UPDATE", "REMOVE"], d)))

            return {
                type,
                types,
                physicalFlows,
                entity,
                logicalFlowId,
                logicalFlow,
                changeUnitsByPhysicalFlowId,
                tags,
                y: evt.layerY
            };
        }

        const baseTweakers = {
            source: {
                onSelect: (entity, evt) => $scope.$applyAsync(() => {
                    const flowId = keyedLogicalFlows.sourceFlowsByEntityId[entity.id];
                    vm.selected = select(serviceBroker, entity, "source", flowId, evt);
                })
            },
            target: {
                onSelect: (entity, evt) => $scope.$applyAsync(() => {
                    const flowId = keyedLogicalFlows.targetFlowsByEntityId[entity.id];
                    vm.selected = select(serviceBroker, entity, "target", flowId, evt);
                })
            },
            type: {
                onSelect: d => {
                    event.stopPropagation();
                    vm.filterOptions.typeIds = [d.id];
                    filterChanged();
                    scrollIntoView($element[0], $window);
                }
            },
            typeBlock: {
                onSelect: () => {
                    event.stopPropagation();
                    resetTypeFilter();
                }
            }
        };

        vm.tweakers = mkTweakers(
            baseTweakers,
            vm.physicalFlows,
            vm.logicalFlows,
            vm.changeUnits);
    };

    vm.focusOnEntity = (selected) => {
        vm.filterOptions.nodeInfo = {
            position: selected.type,
            entity: selected.entity
        };
        filterChanged();
    };

    vm.onTagsChange = () => {
        filterChanged();
        saveTagFilterPreferences(
            vm.tags,
            vm.filterOptions.selectedTags,
            appLogicalFlowFilterExcludedTagIdsKey,
            serviceBroker);
    };

    vm.showAllTags = () => {
        vm.filterOptions.selectedTags = vm.tags;
        filterChanged();
        saveTagFilterPreferences(
            vm.tags,
            vm.filterOptions.selectedTags,
            appLogicalFlowFilterExcludedTagIdsKey,
            serviceBroker);
    };

    vm.exportLogicalFlowData = () => {
        const header = [
            "Source",
            "Source code",
            "Target",
            "Target code",
            "Created At",
            "Created By",
            "Last Updated At",
            "Last Updated By",
            "Data Types",
            "Tags"
        ];

        const dataTypesByFlowId = _
            .chain(vm.decorators)
            .filter(d => d.decoratorEntity.kind === "DATA_TYPE")
            .map(d => ( { id: d.dataFlowId, code: d.decoratorEntity.id }))
            .groupBy("id")
            .value();

        const calcDataTypes = (fId) => {
            const dts = dataTypesByFlowId[fId] || [];

            return _
                .chain(dts)
                .map(dt => dt.code)
                .map(code => displayNameService.lookup("dataType", code))
                .value()
                .join("; ");
        };

        const tagsByFlowId = mkTagInfo(vm.tags);
        const calcTags = (fId) => {
            const tags = tagsByFlowId[fId] || [];

            return _
                .chain(tags)
                .map(t => t.name)
                .value()
                .join("; ");
        };

        const appIds = _
            .chain(vm.logicalFlows)
            .flatMap(f => ([ f.source, f.target ]))
            .filter(r => r.kind === "APPLICATION")
            .map(r => r.id)
            .uniq()
            .value();

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findByIds, [appIds])
            .then(r => {
                const appsById = _.keyBy(r.data, "id");

                const resolveCode = (ref) => {
                    const pathToNameAttr = [ref.id, "assetCode"];
                    return ref.kind === "APPLICATION"
                        ? _.get(appsById, pathToNameAttr, "-")
                        : ref.kind;
                };

                const dataRows = _.map(vm.logicalFlows, f => {
                    return [
                        f.source.name,
                        resolveCode(f.source),
                        f.target.name,
                        resolveCode(f.target),
                        f.created.at,
                        f.created.by,
                        f.lastUpdatedAt,
                        f.lastUpdatedBy,
                        calcDataTypes(f.id),
                        calcTags(f.id)
                    ]
                });

                const rows = _.concat(
                    [header],
                    dataRows);

                downloadTextFile(rows, ",", "logical_flows.csv");
            })
    };
}


controller.$inject = [
    "$element",
    "$scope",
    "$window",
    "DisplayNameService",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


const id = "waltzSourceAndTargetPanel";


export default {
    component,
    id
};
