import _ from "lodash";
import {writable} from "svelte/store";
import dirty from "./dirty";
import {sameRef} from "../../../../common/entity-utils";
import {toGraphId, toGraphNode} from "../../../flow-diagram-utils";

const initialState = {
    nodes: [],
    flows: [],
    annotations: [],
    decorations: {} // logicalFlowGraphId  -> [ physFlowGraphObjs...]
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


function removeDecoration(state, decoration) {
    const refId = toGraphId(decoration.ref);
    const currentDecorations = state.decorations[refId] || [];

    const decorationId = toGraphId(decoration.decoration);
    const existing = _.find(currentDecorations, d => d.id === decorationId);

    if (existing) {
        dirty.set(true);
        const newDecorationsForFlow = _.reject(currentDecorations, d => d.id === decorationId);
        const newDecorations = Object.assign({}, state.decorations, {[refId]: newDecorationsForFlow});
        return Object.assign({}, state, {decorations: newDecorations});
    } else {
        return state;
    }
}


function addDecoration(state, decoration) {
    const refId = toGraphId(decoration.ref);
    const currentDecorations = state.decorations[refId] || [];

    const decorationNode = toGraphNode(decoration.decoration);
    const existing = _.find(currentDecorations, d => d.id === decorationNode.id);

    if (existing) {
        return state;
    } else {
        dirty.set(true);
        const newDecorationsForFlow = [...currentDecorations, decorationNode];
        const newDecorations = Object.assign({}, state.decorations, {[refId]: newDecorationsForFlow});
        return Object.assign({}, state, {decorations: newDecorations});
    }
}


function createStore() {
    const {subscribe, update} = writable(initialState);

    return {
        subscribe,
        addNode: (node) => update(s => addNode(s, node)),
        removeNode: (node) => update(s => removeNode(s, node)),
        addFlow: (flow) => update(s => addFlow(s, flow)),
        removeFlow: (flow) => update(s => removeFlow(s, flow)),
        addAnnotation: (annotation) => update(s => addAnnotation(s, annotation)),
        updateAnnotation: (annotation) => update(s => updateAnnotation(s, annotation)),
        removeAnnotation: (annotation) => update(s => removeAnnotation(s, annotation)),
        addDecoration: (decoration) => update(s => addDecoration(s, decoration)),
        removeDecoration: (decoration) => update(s => removeDecoration(s, decoration)),
        reset: () => update(s => console.log("reset") || Object.assign({}, initialState))
    };
}



const store = createStore();

export default store;
