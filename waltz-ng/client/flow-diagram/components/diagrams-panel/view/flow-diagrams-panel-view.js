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

import _ from "lodash";


import template from "./flow-diagrams-panel-view.html";

import {allEntityLifecycleStatuses, initialiseData} from "../../../../common/index";
import { CORE_API } from "../../../../common/services/core-api-utils";
import { mkSelectionOptions } from "../../../../common/selector-utils";
import { dyamicSectionNavigationDefaultOffset } from "../../../../dynamic-section/components/dynamic-section-navigation/dynamic-section-navigation";
import { pageHeaderDefaultOffset } from "../../../../widgets/page-header/page-header";
import { displayError } from "../../../../common/error-utils";

const bindings = {
    parentEntityRef: '<',
    onEditDiagram: '<',
    onDismissDiagram: '<'
};


const initialState = {
    contextPopup: {
        entityReference: null,
        entity: null,
        styling: {}
    },
    visibility: {
        layers: {},
        menuPopup: false,
        contextPopup: false
    }
};


function determinePopupTopPosition(evt, scrollOffset, elementHeight) {
    // get the width and height of the element
    const navOffset = 60; // the nav bar margin (navbar.html)
    const pageHeaderOffset = scrollOffset > pageHeaderDefaultOffset ? 40 : 0;  // refer to page-header.js
    const dynamicNavOffset = scrollOffset > dyamicSectionNavigationDefaultOffset ? 40 : 0; // refer to dynamic-section-navigation

    const halfHeight = elementHeight / 2;

    // evt.clientY + scrollOffset is for IE support to get the same and pageY accounting for the scroll
    const pageY = (evt.pageY || evt.clientY + scrollOffset);
    const shiftedTop = pageY - halfHeight > scrollOffset ? pageY - halfHeight : scrollOffset;
    return shiftedTop + navOffset + pageHeaderOffset + dynamicNavOffset
}


function determinePopupPosition(evt, $window, $element) {
    //get the width and height of the element
    const elementWidth = _.get($element, '[0].parentElement.clientWidth');
    const elementHeight = _.get($element, '[0].parentElement.clientHeight');
    const scrollOffset = $window.pageYOffset;
    const halfWidth = elementWidth / 2;

    const top = determinePopupTopPosition(evt, scrollOffset, elementHeight);
    const left = evt.clientX < halfWidth
        ? evt.clientX
        : evt.clientX - halfWidth;

    return {
        top: `${top}px`,
        left: `${left}px`
    };
}


function enrichDiagram(flowDiagram, flowActions = []) {

    return Object.assign(
        {},
        flowDiagram,
        {type: "Flow", icon: "random", actions: flowActions}
    );

}

function controller($element,
                    $q,
                    $window,
                    $timeout,
                    flowDiagramStateService,
                    serviceBroker,
                    notification) {
    const vm = initialiseData(this, initialState);

    const loadVisibility = () =>
        vm.visibility.layers = flowDiagramStateService.getState().visibility.layers;

    const loadFlowDiagram = (force = true, id) => serviceBroker
        .loadViewData(
            CORE_API.FlowDiagramStore.getById,
            [ id ],
            { force })
        .then(r => vm.diagram = r.data);

    const flowActions = [
        {
            name: "Clone",
            icon: "clone",
            execute: (diagram) => {
                const newName = prompt("What should the cloned copy be called?", `Copy of ${diagram.name}`);
                if (newName == null) {
                    notification.warning("Clone cancelled");
                    return;
                }
                if (_.isEmpty(newName.trim())) {
                    notification.warning("Clone cancelled, no name given");
                    return;
                }
                serviceBroker
                    .execute(CORE_API.FlowDiagramStore.clone, [diagram.id, newName])
                    .then(newId => {
                        notification.success("Diagram cloned");
                        reload(newId.data);
                    })
                    .catch(e => displayError(notification, "Failed to clone diagram", e));

            }}
    ];

    function reload(newId) {
        let id = newId ? newId : vm.parentEntityRef.id;

        return loadFlowDiagram(true, id)
            .then((flowDiagram) => {
                flowDiagramStateService.reset();
                flowDiagramStateService
                    .load(id)
                    .then(() => loadVisibility());
                vm.diagram = enrichDiagram(flowDiagram, flowActions);
                return vm.diagram;
            });
    }

    vm.$onInit = () => {
        reload();
    };

    vm.toggleLayer = (layer) => {
        const currentlyVisible = flowDiagramStateService.getState().visibility.layers[layer];
        const cmd = {
            command: currentlyVisible ? 'HIDE_LAYER' : 'SHOW_LAYER',
            payload: layer
        };
        flowDiagramStateService.processCommands([cmd]);
        loadVisibility();
    };

    const showNodeDetail = (n) => {
        $timeout(() => vm.visibility.contextPopup = false);

        vm.contextPopup.styling = determinePopupPosition(event, $window, $element);

        const nodeRef = n.data;
        if (nodeRef.kind === 'APPLICATION') {
            serviceBroker
                .loadViewData(CORE_API.ApplicationStore.getById, [ nodeRef.id ])
                .then(r => {
                    $timeout(() => {
                        vm.contextPopup.entityReference = nodeRef;
                        vm.contextPopup.entity = r.data;
                        vm.visibility.contextPopup = true;
                    })
                });
        }
    };

    const showFlowBucketDetail = (logicalFlow) => {
        $timeout(() => vm.visibility.contextPopup = false);
        vm.contextPopup.styling = determinePopupPosition(event, $window, $element);

        const state = flowDiagramStateService.getState();
        const physFlowsPath = ['model', 'decorations', logicalFlow.id];
        const physicalFlowIds = _
            .chain(state)
            .get(physFlowsPath)
            .map('data')
            .map('id')
            .value();

        const diagramSelector = mkSelectionOptions(vm.parentEntityRef, "EXACT", allEntityLifecycleStatuses);

        const flowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findBySelector,
                [ diagramSelector ])
            .then(r => r.data);

        const specPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findBySelector,
                [ diagramSelector ])
            .then(r => r.data);

        const changeUnitPromise = serviceBroker
            .loadViewData(
                CORE_API.ChangeUnitStore.findBySelector,
                [diagramSelector])
            .then(r => r.data);

        $q.all([flowPromise, specPromise, changeUnitPromise])
            .then(([flows, specs, changeUnits]) => {
                const flowsById = _.keyBy(flows, 'id');
                const specsById = _.keyBy(specs, 'id');

                vm.changeUnits = changeUnits;
                const changeUnitsByPhysicalFlowId = _.chain(vm.changeUnits)
                    .filter(cu => cu.subjectEntity.kind = "PHYSICAL_FLOW")
                    .keyBy(cu => cu.subjectEntity.id)
                    .value();

                const flowData = _
                    .chain(physicalFlowIds)
                    .map(flowId => {
                        const physicalFlow = flowsById[flowId];
                        const specId = physicalFlow.specificationId;
                        const physicalSpecification = specsById[specId];
                        const changeUnit = changeUnitsByPhysicalFlowId[physicalFlow.id];
                        return {
                            physicalFlow,
                            physicalSpecification,
                            changeUnit
                        };})
                    .orderBy("physicalSpecification.name")
                    .value();

                vm.contextPopup.entityReference = logicalFlow.data;
                vm.contextPopup.entity = {
                    logical: logicalFlow,
                    physical: flowData
                };
                vm.visibility.contextPopup = true;
            });
    };

    vm.clickHandlers = {
        node: showNodeDetail,
        flowBucket: showFlowBucketDetail
    };
}


controller.$inject = [
    '$element',
    '$q',
    '$window',
    '$timeout',
    'FlowDiagramStateService',
    'ServiceBroker',
    'Notification'
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: 'waltzFlowDiagramsPanelView'
};
