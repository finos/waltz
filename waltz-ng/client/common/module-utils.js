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

import {
    checkIsArray, checkIsComponentDefinition, checkIsServiceDefinition, checkIsStoreDefinition,
    ensureNotNull
} from "./checks";

export const registerComponent = (module, componentDefinition) => {
    ensureNotNull(module, "must provide a module");
    checkIsComponentDefinition(componentDefinition);

    module.component(componentDefinition.id, componentDefinition.component);
};


export const registerComponents = (module, componentDefinitions = []) => {
    componentDefinitions.forEach(defn => registerComponent(module, defn));
};


export const registerStore = (module, storeDefinition) => {
    ensureNotNull(module, "must provide a module");
    checkIsStoreDefinition(storeDefinition);

    module.service(storeDefinition.serviceName, storeDefinition.store);
};


export const registerStores = (module, storeDefinitions = []) => {
    checkIsArray(storeDefinitions, "store definitions must be an array");
    storeDefinitions.forEach(storeDefinition => registerStore(module, storeDefinition));
};


export const registerService = (module, serviceDefinition) => {
    ensureNotNull(module, "must provide a module");
    checkIsServiceDefinition(serviceDefinition);

    module.service(serviceDefinition.serviceName, serviceDefinition.service);
};


export const registerServices = (module, serviceDefinitions = []) => {
    serviceDefinitions.forEach(defn => registerService(module, defn));
};
