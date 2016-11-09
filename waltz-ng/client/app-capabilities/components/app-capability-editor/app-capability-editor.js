
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
    selected: '<',
    onSave: '<',
    onCancel: '<'
};


const initialState = {
    onSave: (d) => 'No onSave handler defined for app-capability-editor: ' + d,
    onCancel: () => 'No onCancel handler defined for app-capability-editor: '
};


const template = require('./app-capability-editor.html');


function validate(rating, comments) {
    return rating != null;
}


function controller() {
    const vm = initialiseData(this, initialState);

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
        return validate(vm.rating, vm.comments);
    };

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;