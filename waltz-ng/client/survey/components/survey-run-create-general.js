/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {initialiseData} from "../../common/index";

const bindings = {
    surveyTemplate: '<',
    surveyRun: '<',
    onSave: '<'
};

const initialState = {};

const template = require('./survey-run-create-general.html');

function controller(appGroupStore, involvementKindStore) {
    const vm = initialiseData(this, initialState);

    appGroupStore.findPublicGroups().then(
        appGroups => {
            vm.availableAppGroups = appGroups;
        }
    );

    involvementKindStore.findAll().then(
        involvementKinds => {
            vm.availableInvolvementKinds = involvementKinds;
        }
    );

    vm.onSubmit = () => {
        vm.onSave(this.surveyRun);
    }
}

controller.$inject = ['AppGroupStore', 'InvolvementKindStore'];

export default {
    bindings,
    template,
    controller
};

