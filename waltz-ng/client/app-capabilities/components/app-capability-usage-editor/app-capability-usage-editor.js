
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
import _ from 'lodash';
import {initialiseData, invokeFunction} from "../../../common";


const bindings = {
    appCapability: '<',
    capabilities: '<',
    onSave: '<',
    onCancel: '<',
    currentUsage: '<'
};


const initialState = {
    appCapability: null,
    workingCopy: null,
    selectedCapability: null,
    onSave: (d) => console.log('No onSave handler defined for app-capability-editor: ', d),
    onCancel: () => console.log('No onCancel handler defined for app-capability-editor: '),
    visibility: {
        capabilitySelector: true
    }
};


const template = require('./app-capability-usage-editor.html');


function validate(working = {}, original = {}) {
    return working.capabilityId && working.rating;
}


function controller($scope) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.appCapability) vm.workingCopy = _.clone(vm.appCapability);
        if (vm.currentUsage) vm.usedCapabilityIds = _.map(vm.currentUsage, 'capabilityId');
    };

    vm.selectRating = (rating) => {
        vm.workingCopy.rating = rating;
    };

    vm.save = () => {
        if(vm.canSave()) {
            const saveParams = {
                isNew: ! vm.appCapability.capabilityId,
                capabilityId: vm.workingCopy.capabilityId,
                rating: vm.workingCopy.rating,
                description: vm.workingCopy.description
            };

            invokeFunction(vm.onSave, saveParams);
        }
    };

    vm.cancel = () => {
        return invokeFunction(vm.onCancel);
    };

    vm.canSave = () => {
        return validate(vm.workingCopy);
    };

    const updateCapability = (c) => {
        if (! c) return;
        vm.workingCopy.capability = c;
        vm.workingCopy.capabilityId = c ? c.id : 0;
        vm.selectedCapability = c;
        vm.visibility.capabilitySelector = false;
    };

    vm.onNodeSelect = updateCapability;

}


controller.$inject = ['$scope'];


const component = {
    bindings,
    template,
    controller
};


export default component;