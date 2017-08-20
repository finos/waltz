import {
    checkIsArray, checkIsComponentDefinition, checkIsServiceDefinition, checkIsStoreDefinition,
    ensureNotNull
} from "./checks";

export const registerComponent = (module, componentDefinition) => {
    ensureNotNull(module, 'must provide a module');
    checkIsComponentDefinition(componentDefinition);

    module.component(componentDefinition.id, componentDefinition.component);
};


export const registerComponents = (module, componentDefinitions = []) => {
    componentDefinitions.forEach(defn => registerComponent(module, defn));
};


export const registerStore = (module, storeDefinition) => {
    ensureNotNull(module, 'must provide a module');
    checkIsStoreDefinition(storeDefinition);

    module.service(storeDefinition.serviceName, storeDefinition.store);
};


export const registerStores = (module, storeDefinitions = []) => {
    checkIsArray(storeDefinitions, 'store definitions must be an array');
    storeDefinitions.forEach(storeDefinition => registerStore(module, storeDefinition));
};


export const registerService = (module, serviceDefinition) => {
    ensureNotNull(module, 'must provide a module');
    checkIsServiceDefinition(serviceDefinition);

    module.service(serviceDefinition.serviceName, serviceDefinition.service);
};


export const registerServices = (module, serviceDefinitions = []) => {
    serviceDefinitions.forEach(defn => registerService(module, defn));
};
