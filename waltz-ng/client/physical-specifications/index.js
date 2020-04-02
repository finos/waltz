/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import angular from "angular";
import {registerComponents, registerStores} from "../common/module-utils";
import * as PhysicalSpecificationStore from "./services/physical-specification-store";
import * as PhysicalSpecDefinitionFieldStore from "./services/physical-spec-definition-field-store";
import * as PhysicalSpecDefinitionStore from "./services/physical-spec-definition-store";
import * as PhysicalSpecDefinitionSampleFileStore from "./services/physical-spec-definition-sample-file-store";

import DataTypeList from "./components/data-type/physical-spec-data-type-list";
import PhysicalSpecDefinitionSection from "./components/spec-definition-section/physical-spec-definition-section";
import Routes from './routes';
import PhysicalDataSection from './components/physical-data-section/physical-data-section';
import PhysicalSpecDefinitionCreatePanel from './components/create/physical-spec-definition-create-panel';
import PhysicalSpecificationOverview from './components/overview/physical-specification-overview';
import PhysicalSpecificationConsumers from './components/specification-consumers/physical-specification-consumers';
import PhysicalSpecDefintionPanel from './components/spec-definition/physical-spec-definition-panel';


function setup() {

    const module = angular.module('waltz.physical.specification', []);

    module
        .config(Routes);

    registerComponents(module, [
        DataTypeList,
        PhysicalSpecDefinitionSection
    ]);

    module
        .component('waltzPhysicalDataSection', PhysicalDataSection)
        .component('waltzPhysicalSpecDefinitionCreatePanel', PhysicalSpecDefinitionCreatePanel)
        .component('waltzPhysicalSpecificationOverview', PhysicalSpecificationOverview)
        .component('waltzPhysicalSpecificationConsumers', PhysicalSpecificationConsumers)
        .component('waltzPhysicalSpecDefinitionPanel', PhysicalSpecDefintionPanel);

    registerStores(module, [
        PhysicalSpecDefinitionFieldStore,
        PhysicalSpecificationStore,
        PhysicalSpecDefinitionStore,
        PhysicalSpecDefinitionSampleFileStore
    ]);

    return module.name;
}


export default setup;
