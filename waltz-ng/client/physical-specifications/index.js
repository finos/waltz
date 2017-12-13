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

import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";
import * as physicalSpecDataTypeStore from "./services/physical-spec-data-type-store";
import * as physicalSpecificationStore from "./services/physical-specification-store";
import dataTypeList from "./components/data-type/physical-spec-data-type-list";
import dataTypeSection from "./components/data-type/physical-spec-data-type-section";


function setup() {

    const module = angular.module('waltz.physical.specification', []);

    module
        .config(require('./routes'));

    registerComponents(
        module,
        [dataTypeList, dataTypeSection]);


    module
        .component('waltzPhysicalDataSection', require('./components/physical-data-section/physical-data-section'))
        .component('waltzPhysicalSpecDefinitionCreatePanel', require('./components/create/physical-spec-definition-create-panel'))
        .component('waltzPhysicalSpecificationOverview', require('./components/overview/physical-specification-overview'))
        .component('waltzPhysicalSpecificationConsumers', require('./components/specification-consumers/physical-specification-consumers'))
        .component('waltzPhysicalSpecificationMentions', require('./components/mentions/physical-specification-mentions'))
        .component('waltzPhysicalSpecDefinitionPanel', require('./components/spec-definition/physical-spec-definition-panel'));

    registerStores(
        module,
        [physicalSpecDataTypeStore, physicalSpecificationStore]);

    module
        .service('PhysicalSpecDefinitionStore', require('./services/physical-spec-definition-store'))
        .service('PhysicalSpecDefinitionFieldStore', require('./services/physical-spec-definition-field-store'))
        .service('PhysicalSpecDefinitionSampleFileStore', require('./services/physical-spec-definition-sample-file-store'));


    return module.name;
}


export default setup;
