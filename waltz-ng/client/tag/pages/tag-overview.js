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


import template from "./tag-overview.html";
import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkEnumGridCell, mkLinkGridCell} from "../../common/grid-utils";

function controller($stateParams, serviceBroker) {

    const vm = initialiseData(this);
    const id = $stateParams.id;

    vm.entityReference = { id, kind: "TAG" };

    serviceBroker
        .loadViewData(
            CORE_API.TagStore.getTagsWithUsageById,
            [ id ])
        .then(r => vm.tag = r.data);

    vm.physicalFlowColumnDefs = [
        Object.assign(mkLinkGridCell("Name", "specification.name", "physicalFlow.id", "main.physical-flow.view"), {width: "15%"}),
        Object.assign(mkLinkGridCell("Source App", `${"logicalFlow.source"}.name`, `${"logicalFlow.source"}.id`, "main.app.view"), {width: "10%"}),
        Object.assign(mkLinkGridCell("Target App", `${"logicalFlow.target"}.name`, `${"logicalFlow.target"}.id`, "main.app.view"), {width: "10%"}),
        { field: 'specification.externalId', displayName: 'Ext. Id' },
        Object.assign(mkEnumGridCell("Observation", "physicalFlow.freshnessIndicator", "FreshnessIndicator", true, true), {width: "8%"}),
        { field: 'flow.frequency', displayName: 'Frequency', width: "10%" },
        { field: 'specification.description', displayName: 'Description'}
    ];
}

controller.$inject = ["$stateParams", "ServiceBroker"];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};

