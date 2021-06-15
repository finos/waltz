import {writable} from "svelte/store";
import {toGraphId, toGraphNode} from "../../../flow-diagram-utils";
import _ from "lodash";
import dirty from "./dirty";


const initialState = {
    groupOverlays: {}, //overlays grouped by overlayGroupId
    appliedOverlay: null,
    selectedGroup: null
};

// remove the group from this store

function setAppliedOverlay(state, overlay){
    return Object.assign({}, state, {appliedOverlay: overlay})
}


function clearAppliedOverlay(state){
    return Object.assign({}, state, {appliedOverlay: null})
}

function setSelectedGroup(state, group){
    return Object.assign({}, state, {selectedGroup: group})
}


function clearSelectedGroup(state){
    return Object.assign({}, state, {selectedGroup: null})
}


function addOverlay(state, overlay){
    const refId = toGraphId(overlay.groupRef);
    const currentOverlays = state.groupOverlays[refId] || [];

    const overlayItem = toGraphNode(overlay);
    const existing = _.find(currentOverlays, d => d.id === overlayItem.id);

    if (existing) {
        return state;
    } else {
        dirty.set(true);
        const newOverlaysForGroup = [...currentOverlays, overlayItem];
        const newOverlays = Object.assign({}, state.groupOverlays, {[refId]: newOverlaysForGroup});
        return Object.assign({}, state, {groupOverlays: newOverlays});
    }
}

function addGroup(state, group){
    const existing = _.find(state.groups, d => d.id === group.id);

    if (existing) {
        return state;
    } else {
        dirty.set(true);
        return Object.assign({}, state, {groups: [...state.groups, group]});
    }
}

function removeGroup(state, group) {
    const existing = _.find(state.groups, d => d.id === group.id);

    if (existing) {
        dirty.set(true);
        return Object.assign({}, state, {groups: _.without(state.groups, existing)});
    } else {
        return state;
    }
}

function removeOverlay(state, overlay) {
    const refId = toGraphId(overlay.data.groupRef);
    const currentOverlays = state.groupOverlays[refId] || [];

    const existing = _.find(currentOverlays, d => d.id === overlay.id);

    if (existing) {
        dirty.set(true);
        const newOverlaysForGroup = _.reject(currentOverlays, d => d.id === overlay.id);
        const newOverlays = Object.assign({}, state.groupOverlays, {[refId]: newOverlaysForGroup});
        return Object.assign({}, state, {groupOverlays: newOverlays});
    } else {
        return state;
    }
}

function updateOverlay(state, overlay) {
    const refId = toGraphId(overlay.groupRef);
    const currentOverlays = state.groupOverlays[refId] || [];

    const existing = _.find(currentOverlays, d => d.id === overlay.id);

    if (existing) {
        dirty.set(true);
        const newOverlaysMinusOld = _.reject(currentOverlays, d => d.id === overlay.id);
        const newOverlaysForGroup = [...newOverlaysMinusOld, overlay]
        const newOverlays = Object.assign({}, state.groupOverlays, {[refId]: newOverlaysForGroup});
        return Object.assign({}, state, {groupOverlays: newOverlays});
    } else {
        return state;
    }
}

function createStore() {
    const {subscribe, update} = writable(initialState);

    return {
        subscribe,
        addGroup: (group) => update(s => addGroup(s, group)),
        removeGroup: (group) => update(s => removeGroup(s, group)),
        addOverlay: (overlay) => update(s => addOverlay(s, overlay)),
        removeOverlay: (overlay) => update(s => removeOverlay(s, overlay)),
        setAppliedOverlay: (overlay => update(s => setAppliedOverlay(s, overlay))),
        clearAppliedOverlay: (() => update(s => clearAppliedOverlay(s))),
        setSelectedGroup: (group => update(s => setSelectedGroup(s, group))),
        clearSelectedGroup: (() => update(s => clearSelectedGroup(s))),
        reset: () => update(s => console.log("reset") || Object.assign({}, initialState))
    };
}

const store = createStore();

export default store;
