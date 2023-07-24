import {derived, writable} from "svelte/store";
import {defaultBgColors, defaultColors, mkGroup} from "./diagram-builder-utils";
import _ from "lodash";


export const initialGroup = mkGroup("Diagram Title", 1, null, 1);

export let selectedGroup = writable(null);
export let editing = writable(false);
export let groups = writable([initialGroup]);
export let movingGroup = writable(null);
export let hoveredGroupId = writable(null);

export let titleColors = writable(defaultColors);
export let backgroundColors = writable(defaultBgColors);

export let groupsWithItems = derived(
    [groups],
    ([$groups]) => {
        return _.orderBy($groups,d => d.position || d.id);
    });