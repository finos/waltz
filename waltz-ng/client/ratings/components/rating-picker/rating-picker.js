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
import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";
import {useBlackAsForeground} from "../../../common/colors";


const bindings = {
    selected: '<',
    editDisabled: '<',
    onSelect: '<',
    onKeypress: '<',
    ratingSchemeId: '<',
};


const template = require('./rating-picker.html');


const initialState = {
    pickerStyle: {},
    onSelect: (rating) => 'No onSelect handler defined for rating-picker: ' + rating,
};


function controller(serviceBroker) {
    const vm = this;

    vm.$onInit = () => initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.ratingSchemeId) {
            serviceBroker
                .loadAppData(CORE_API.RatingSchemeStore.getById, [vm.ratingSchemeId])
                .then(r => vm.options = _
                        .chain(r.data.ratings)
                        .filter(d => d.userSelectable)
                        .map(d => Object.assign({}, d, { foregroundColor: useBlackAsForeground(d.color) ? '#000' : '#fff' }))
                        .value());
        }
        if (c.disabled) {
            vm.pickerStyle = vm.disabled
                ? { opacity: 0.4 }
                : [];
        }
    }

}


controller.$inject = [ 'ServiceBroker' ];


const component = {
    bindings,
    template,
    controller
};


export default component;