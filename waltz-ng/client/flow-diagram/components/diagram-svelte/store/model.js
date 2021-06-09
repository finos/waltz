import _ from "lodash";
import {writable} from "svelte/store";
import dirty from "./dirty";
import {sameRef} from "../../../../common/entity-utils";
import {toGraphId} from "../../../flow-diagram-utils";

const initialState = {
    nodes: [],
    flows: [],
    annotations: []
};


function addNode(state, node) {
    const existing = _.find(state.nodes, d => d.id === node.id);

    if (existing) {
        return state;
    } else {
        dirty.set(true);
        return Object.assign({}, state, {nodes: [...state.nodes, node]});
    }
}


function removeNode(state, node) {
    const existing = _.find(state.nodes, d => d.id === node.id);

    if (existing) {
        dirty.set(true);
        // TODO: remove flows
        const stateWithoutFlows = _  // and flow annotations!
            .chain(state.flows)
            .filter(d => sameRef(d.data.source, node.data) || sameRef(d.data.target, node.data))
            .reduce(
                (accState, d) => removeFlow(accState, d),
                state)
            .value();

        const stateWithoutAnnotations = _
            .chain(stateWithoutFlows.annotations)
            .filter(d => toGraphId(d.data.entityReference) === node.id)
            .reduce(
                (accState, d) => removeAnnotation(accState, d),
                stateWithoutFlows)
            .value();

        return Object.assign({}, stateWithoutAnnotations, {nodes: _.without(stateWithoutAnnotations.nodes, existing)});
    } else {
        return state;
    }
}


function removeFlow(state, flow) {
    console.log("Remove flow", {state, flow})
    const existing = _.find(state.flows, d => d.id === flow.id);

    if (existing) {
        dirty.set(true);
        const stateWithoutAnnotations = _
            .chain(state.annotations)
            .filter(d => toGraphId(d.data.entityReference) === flow.id)
            .reduce(
                (accState, d) => removeAnnotation(accState, d),
                state)
            .value();

        return Object.assign({}, stateWithoutAnnotations, {flows: _.without(stateWithoutAnnotations.flows, existing)});
    } else {
        return state;
    }
}


function addFlow(state, flow) {
    const existing = _.find(state.flows, d => d.id === flow.id);

    if (existing) {
        return state;
    } else {
        dirty.set(true);
        return Object.assign({}, state, {flows: [...state.flows, flow]});
    }
}


function removeAnnotation(state, annotation) {
    const existing = _.find(state.annotations, d => d.id === annotation.id);

    if (existing) {
        dirty.set(true);
        return Object.assign({}, state, {annotations: _.without(state.annotations, existing)});
    } else {
        return state;
    }
}


function addAnnotation(state, annotation) {
    const existing = _.find(state.annotations, d => d.id === annotation.id);

    if (existing) {
        return state;
    } else {
        dirty.set(true);
        return Object.assign({}, state, {annotations: [...state.annotations, annotation]});
    }
}



function updateAnnotation(state, annotationUpd) {
    const existing = _.find(state.annotations, d => d.id === annotationUpd.id);

    if (existing) {
        dirty.set(true);
        const newAnnotation = Object.assign({}, existing);
        newAnnotation.data.note = annotationUpd.note;

        const newAnnotations  = _
            .chain(state.annotations)
            .without(existing)
            .concat([newAnnotation])
            .value();

        return Object.assign({}, state, {annotations: newAnnotations});
    } else {
        return state;
    }
}


function createStore() {
    const {subscribe, set, update} = writable(initialState);

    return {
        subscribe,
        addNode: (node) => update(s => addNode(s, node)),
        removeNode: (node) => update(s => removeNode(s, node)),
        addFlow: (flow) => update(s => addFlow(s, flow)),
        removeFlow: (flow) => update(s => removeFlow(s, flow)),
        addAnnotation: (annotation) => update(s => addAnnotation(s, annotation)),
        updateAnnotation: (annotation) => update(s => updateAnnotation(s, annotation)),
        removeAnnotation: (annotation) => update(s => removeAnnotation(s, annotation))
    };
}

const store = createStore();

export default store;
