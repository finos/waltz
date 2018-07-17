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

import {initialiseData} from '../../../common';
import {entity} from '../../../common/services/enums/entity'
import template from './logical-flow-counterpart-selector.html';


/**
 * @name waltz-logical-flow-counterpart-selector
 *
 * @description
 * This component ...
 */


const bindings = {
    allActors: '<',
    onAddApplication: '<',
    onAddActor: '<',
    onCancel: '<',
};


const transclude = {
    help: 'help',
    heading: 'heading'
};


const initialState = {
    actorIcon: entity.ACTOR.icon,
    applicationIcon: entity.APPLICATION.icon,
    visibility: {
        addingApplication: true,
    }
};


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {

    };

    vm.showAddActor = (visible = true) => {
        vm.visibility.addingApplication = !visible;
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller,
    transclude
};


export default component;