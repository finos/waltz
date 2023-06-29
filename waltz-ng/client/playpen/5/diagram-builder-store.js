import {writable} from "svelte/store";

export let selectedGroup = writable(null);
export let workingGroup = writable(null);
export let editing = writable(false);

export let groups = writable([]);
export let items = writable(null);

export const FlexDirections = {
    COLUMN: "column",
    ROW: "row"
}

export const DefaultProps = {
    itemHeight: 5,
    itemWidth: 10,
    flexDirection: FlexDirections.ROW
}
