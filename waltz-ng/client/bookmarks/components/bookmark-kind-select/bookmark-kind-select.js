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
import template from './bookmark-kind-select.html'
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    selected: '<',
    disabledCondition: '<',
    onSelect: '&'
};


function createKinds(serviceBroker) {
    return serviceBroker
        .loadAppData(CORE_API.EnumValueStore.findAll)
        .then(r => {
            return _
                .chain(r.data)
                .filter(d => d.type === 'BookmarkKind')
                .sortBy('name')
                .map(d => ({
                    code: d.key,
                    name: d.name,
                    icon: d.icon
                }))
                .value();
        });
}

function controller(serviceBroker) {

    const vm = this;

    vm.$onInit = () => {
        createKinds(serviceBroker)
            .then(kinds => vm.kinds = kinds);
    };

    vm.onChange = (kind) => {
        if (!vm.onSelect()) {
            console.warn('bookmark-kind-select: No handler for change notification is registered.  Please provide an on-select callback')
            return;
        }
        vm.onSelect()(kind.code, kind);
    };
}


controller.$inject = ['ServiceBroker'];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: 'waltzBookmarkKindSelect'
};

