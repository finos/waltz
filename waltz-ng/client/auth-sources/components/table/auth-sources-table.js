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

import _ from "lodash";
import {initialiseData} from "../../../common";
import {nest} from "d3-collection";
import {CORE_API} from "../../../common/services/core-api-utils";
import {ascending} from "d3-array";


const bindings = {
    authSources: '<',
    showDescription: '@?',
    orgUnits: '<',
};


const initialState = {
    showDescription: true
};


const template = require('./auth-sources-table.html');


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);


    const refresh = () => {
        const dataTypesByCode= _.keyBy(vm.dataTypes, 'code');
        const dataTypesById= _.keyBy(vm.dataTypes, 'id');
        const orgUnitsById = _.keyBy(vm.orgUnits, 'id');

        const authSources = _.map(vm.authSources, d => {
            return {
                app: d.applicationReference,
                dataType: Object.assign({}, dataTypesByCode[d.dataType], { kind: 'DATA_TYPE' }),
                appOrgUnit: d.appOrgUnitReference,
                declaringOrgUnit: Object.assign({}, orgUnitsById[d.parentReference.id], { kind: 'ORG_UNIT' }),
                description: d.description,
                rating: d.rating
            };
        });
        const nested = nest()
            .key(d => d.dataType.id)
            .sortKeys((a, b) => ascending(dataTypesById[a].name, dataTypesById[b].name))
            .sortValues((a, b) => ascending(a.app.name, b.app.name))
            .entries(authSources);


        vm.authSourceTable = nested;
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data)
            .then(refresh);

        serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnits = r.data)
            .then(refresh);

        serviceBroker
            .loadAppData(
                CORE_API.StaticPanelStore.findByGroup,
                ['SECTION.AUTH_SOURCES.ABOUT'])
            .then(rs => vm.descriptionPanels = rs.data);
    };

    vm.$onChanges = () => refresh();

}


controller.$inject = ['ServiceBroker'];


const component = {
    bindings,
    controller,
    template
};


export default component;