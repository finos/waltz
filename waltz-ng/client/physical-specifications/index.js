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
import * as PhysicalSpecDataTypeStore from "./services/physical-spec-data-type-store";
import * as PhysicalSpecificationStore from "./services/physical-specification-store";
import * as PhysicalSpecDefinitionFieldStore from "./services/physical-spec-definition-field-store";
import * as PhysicalSpecDefinitionStore from "./services/physical-spec-definition-store";
import * as PhysicalSpecDefinitionSampleFileStore from "./services/physical-spec-definition-sample-file-store";

import DataTypeList from "./components/data-type/physical-spec-data-type-list";
import DataTypeSection from "./components/data-type/physical-spec-data-type-section";
import PhysicalSpecDefinitionSection from "./components/spec-definition-section/physical-spec-definition-section";
import Routes from './routes';
import PhysicalDataSection from './components/physical-data-section/physical-data-section';
import PhysicalSpecDefinitionCreatePanel from './components/create/physical-spec-definition-create-panel';
import PhysicalSpecificationOverview from './components/overview/physical-specification-overview';
import PhysicalSpecificationConsumers from './components/specification-consumers/physical-specification-consumers';
import PhysicalSpecificationMentions from './components/mentions/physical-specification-mentions';
import PhysicalSpecDefintionPanel from './components/spec-definition/physical-spec-definition-panel';


function setup() {

    const module = angular.module('waltz.physical.specification', []);

    module
        .config(Routes);

    registerComponents(module, [
        DataTypeList,
        DataTypeSection,
        PhysicalSpecDefinitionSection
    ]);

    module
        .component('waltzPhysicalDataSection', PhysicalDataSection)
        .component('waltzPhysicalSpecDefinitionCreatePanel', PhysicalSpecDefinitionCreatePanel)
        .component('waltzPhysicalSpecificationOverview', PhysicalSpecificationOverview)
        .component('waltzPhysicalSpecificationConsumers', PhysicalSpecificationConsumers)
        .component('waltzPhysicalSpecificationMentions', PhysicalSpecificationMentions)
        .component('waltzPhysicalSpecDefinitionPanel', PhysicalSpecDefintionPanel);

    registerStores(module, [
        PhysicalSpecDataTypeStore,
        PhysicalSpecDefinitionFieldStore,
        PhysicalSpecificationStore,
        PhysicalSpecDefinitionStore,
        PhysicalSpecDefinitionSampleFileStore
    ]);

    return module.name;
}


export default setup;
