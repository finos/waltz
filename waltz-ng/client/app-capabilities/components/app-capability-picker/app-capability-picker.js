
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
import {initialiseData, invokeFunction} from "../../../common";


const bindings = {
    capabilities: '<',
    onSave: '<',
    onCancel: '<'
};


const initialState = {
    onSave: (d) => 'No onSave handler defined for app-capability-picker: ' + d,
    onCancel: () => 'No onCancel handler defined for app-capability-picker: ',
    selectedCapability: null
};


const template = require('./app-capability-picker.html');


function validate(capability, rating, comments) {
    return capability && rating;
}


function controller($scope) {
    const vm = initialiseData(this, initialState);

    $scope.$watch('$ctrl.selectedCapability', c => console.log('cappy', c));


    vm.selectRating = (rating) => {
        vm.rating = rating;
    };


    vm.save = () => {
        if(vm.canSave()) {
            invokeFunction(vm.onSave, {
                capability: vm.selectedCapability,
                rating: vm.rating,
                comments: vm.comments
            });
        }
    };


    vm.cancel = () => {
        return invokeFunction(vm.onCancel);
    };


    vm.canSave = () => {
        return validate(vm.selectedCapability, vm.rating, vm.comments);
    };

}


controller.$inject = ['$scope'];


const component = {
    bindings,
    template,
    controller
};


export default component;