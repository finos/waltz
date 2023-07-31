import {writable} from "svelte/store";
import {defaultBgColors, defaultColors, mkGroup} from "../entity-diagram-utils";
import {generateUUID} from "../../../../system/svelte/nav-aid-builder/custom/builderStore";

export function createInitialGroup() {
    return mkGroup("Diagram Title", generateUUID(), null, 1);
}


export let initialGroup = mkGroup("Diagram Title", generateUUID(), null, 1);

export let movingGroup = writable(null);

export let titleColors = writable(defaultColors);
export let backgroundColors = writable(defaultBgColors);
