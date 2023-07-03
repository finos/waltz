import {derived, writable} from "svelte/store";
import {mkGroup} from "./diagram-builder-utils";
import _ from "lodash";


export let selectedGroup = writable(null);
export let editing = writable(false);

export let groups = writable([mkGroup("Diagram Title", 1, null, 1)]);
export let movingGroup = writable(null);
export let hoveredGroupId = writable(null);

export let items = writable([]);

export let groupsWithItems = derived(
    [groups, items],
    ([$groups, $items]) => {

        const itemsByGroupId = _.groupBy($items, d => d.groupId);

        return _.orderBy($groups,d => d.position || d.id);
    });