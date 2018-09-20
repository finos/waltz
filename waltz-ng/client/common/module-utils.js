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
