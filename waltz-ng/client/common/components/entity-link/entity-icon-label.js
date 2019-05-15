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

import {initialiseData} from "../../../common";
import template from './entity-icon-label.html';
import {CORE_API} from "../../services/core-api-utils";


const bindings = {
    entityRef: '<',
    iconPlacement: '@',
    tooltipPlacement: '@'
};


const initialState = {
    iconPlacement: 'left', // can be left, right, none
    tooltipPlacement: 'top', // left, top-left, top-right; refer to: (https://github.com/angular-ui/bootstrap/tree/master/src/tooltip)
    trigger: 'none'
};


const entityLoaders = {
    // custom loaders, add more entity types here with links to their CORE_API loader method and
    // a post-processing step (mkProps) to generate a list of { name, value } pairs

    "APPLICATION": {
        method: CORE_API.ApplicationStore.getById,
        mkProps: (app, displayNameService) => ([
            {
                name: "Asset Code",
                value: app.assetCode || "?"
            }, {
                name: "Kind",
                value: displayNameService.lookup("applicationKind", app.applicationKind, "?")
            }, {
                name: "Lifecycle",
                value: displayNameService.lookup("lifecyclePhase", app.lifecyclePhase, "?")
            }, {
                name: "Criticality",
                value: displayNameService.lookup("criticality", app.businessCriticality, "?")
            }
        ])
    }
};


function controller(displayNameService, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = c => {
        if (! vm.entityRef) return;
        if (_.has(entityLoaders, vm.entityRef.kind)) {
            vm.popoverTemplate = `weil-popover-custom`;
            vm.trigger = "mouseenter";
        } else {
            vm.popoverTemplate = `weil-popover-basic`;
            vm.trigger = vm.entityRef.description || vm.entityRef.lifecyclePhase
                ? 'mouseenter'
                : 'none';
        }
    };

    vm.lazyLoad = () => {
        const loader = entityLoaders[vm.entityRef.kind];
        if (loader) {
            serviceBroker
                .loadViewData(loader.method, [ vm.entityRef.id ])
                .then(r => vm.entity = r.data)
                .then(() => vm.props = loader.mkProps(vm.entity, displayNameService));
        }
    };
}


controller.$inject = [
    "DisplayNameService",
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default component;
