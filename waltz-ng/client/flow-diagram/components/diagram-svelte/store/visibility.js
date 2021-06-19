import {writable} from "svelte/store";

const initialState = {
    flowBuckets: true,
    annotations: true,
    pendingFlows: true,
    removedFlows: true
};

function createStore() {
    const {subscribe, set, update} = writable(initialState);

    return {
        subscribe,
        toggleFlowBuckets: () => update(s => Object.assign({}, s, { flowBuckets: !s.flowBuckets })),
        toggleAnnotations: () => update(s => Object.assign({}, s, { annotations: !s.annotations })),
        togglePendingFlows: () => update(s => Object.assign({}, s, { pendingFlows: !s.pendingFlows })),
        toggleRemovedFlows: () => update(s => Object.assign({}, s, { removedFlows: !s.removedFlows }))
    }
}

const store = createStore();

export default store;
