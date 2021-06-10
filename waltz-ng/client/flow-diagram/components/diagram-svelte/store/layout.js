import {writable} from "svelte/store";


const initialPositions = {}; // gid -> {  x, y }

function move(state, moveCmd) {
    const currentPos = state[moveCmd.id] || {x:0, y: 0};
    const newPos = {
        x: currentPos.x + moveCmd.dx,
        y: currentPos.y + moveCmd.dy
    };
    return Object.assign({}, state, {[moveCmd.id]: newPos});
}


function createPositionStore() {
    const {update, subscribe} = writable(initialPositions);

    return {
        subscribe,
        move: (moveCmd) => update(s => move(s, moveCmd))
    };
}


export const positions = createPositionStore();

export const diagramTransform = writable("translate(0 0)");
