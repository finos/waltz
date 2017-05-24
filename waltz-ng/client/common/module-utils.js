import {checkIsComponentDefinition, checkIsServiceDefinition, checkIsStoreDefinition, ensureNotNull} from "./checks";

export const registerComponent = (module, componentDefinition) => {
    ensureNotNull(module, 'must provide a module');
    checkIsComponentDefinition(componentDefinition);

    module.component(componentDefinition.id, componentDefinition.component);
};


export const registerStore = (module, storeDefinition) => {
    ensureNotNull(module, 'must provide a module');
    checkIsStoreDefinition(storeDefinition);

    module.service(storeDefinition.serviceName, storeDefinition.store);
};


export const registerService = (module, serviceDefinition) => {
    ensureNotNull(module, 'must provide a module');
    checkIsServiceDefinition(serviceDefinition);

    module.service(serviceDefinition.id, serviceDefinition.service);
};
