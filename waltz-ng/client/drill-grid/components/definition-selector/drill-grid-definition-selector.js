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

import template from './drill-grid-definition-selector.html';
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from 'lodash';


const bindings = {
    selectedDefinition: '<',
    onSelectDefinition: '<'
};


const initialState = {
    visibility: {
        manualPicker: false,
        choicePicker: false,
        summary: true,
    }
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => {
                vm.axisOptions = _.union(
                    r.data,
                    [ { id: null, kind: 'DATA_TYPE', name: 'Data Type' }]);
            });

        serviceBroker
            .loadAppData(CORE_API.DrillGridDefinitionStore.findAll)
            .then(r => {
                vm.definitions = r.data;
                vm.visibility.manualPicker = r.data.length === 0;
            });


        if (vm.selectedDefinition) {
            vm.visibility.manualPicker = vm.selectedDefinition.id === null;
        }
    };

    vm.onAxisChange = () => {

        const defn = Object.assign(
            {},
            vm.selectedDefinition,
            {
                name: `${vm.selectedDefinition.xAxis.name} / ${vm.selectedDefinition.yAxis.name}`,
                description: 'Custom dimensions'
            });
        vm.onSelectDefinition(defn);
    };

    vm.onOptionSelect = (d) => {
        vm.onSelectDefinition(d);
        vm.switchToSummary();
    };

    vm.switchToSummary = () => {
        const v = vm.visibility;
        v.manualPicker = false;
        v.choicePicker = false;
        v.summary = true;
    };

    vm.switchToChoicePicker = () => {
        const v = vm.visibility;
        v.manualPicker = false;
        v.choicePicker = true;
        v.summary = false;
    };

    vm.switchToManualPicker = () => {
        const v = vm.visibility;
        v.manualPicker = true;
        v.choicePicker = false;
        v.summary = false;
    };
}

controller.$inject = [ 'ServiceBroker' ];


const component = {
    template,
    controller,
    bindings
};

const id = 'waltzDrillGridDefinitionSelector';

export default {
    component,
    id
};