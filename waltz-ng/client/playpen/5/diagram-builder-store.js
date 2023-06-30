import {derived, writable} from "svelte/store";
import {mkGroup} from "./diagram-builder-utils";
import _ from "lodash";


export let selectedGroup = writable(null);
export let editing = writable(false);

export let groups = writable([mkGroup("Diagram Title", 1, null)]);

export let items = writable([]);

export let groupsWithItems = derived(
    [groups, items],
    ([$groups, $items]) => {

        const itemsByGroupId = _.groupBy($items, d => d.groupId);

        return _.map($groups, d => Object.assign({}, d, {items: _.get(itemsByGroupId, d.id, [])}));
    });