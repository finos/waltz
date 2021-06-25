import {writable} from "svelte/store";
import dirty from "./dirty";


const initialPositions = {}; // gid -> {  x, y }


function move(state, moveCmd) {
    dirty.set(true);
    // get current position, if not try position of ref
    const currentPos = state[moveCmd.id] || state[moveCmd.refId] || {x:0, y: 0};
    const newPos = {
        x: currentPos.x + moveCmd.dx,
        y: currentPos.y + moveCmd.dy
    };
    return Object.assign({}, state, {[moveCmd.id]: newPos});
}


function setPosition(state, posCmd) {
    dirty.set(true);
    const updatedStateFrag = {[posCmd.id]: { x: posCmd.x, y: posCmd.y}};
    return Object.assign({}, state, updatedStateFrag);
}


function createPositionStore() {
    const {update, subscribe} = writable(initialPositions);

    return {
        subscribe,
        move: (moveCmd) => update(s => move(s, moveCmd)),
        setPosition: (posCmd) => update(s => setPosition(s, posCmd)),
        reset: () => update(s => Object.assign({}, initialPositions))
    };
}


export const positions = createPositionStore();
export const widths = writable({});

export const diagramTransform = writable("translate(0 0)");
