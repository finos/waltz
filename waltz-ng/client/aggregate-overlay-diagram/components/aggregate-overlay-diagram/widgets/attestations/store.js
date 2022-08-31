import {writable} from "svelte/store";
import moment from "moment";

export const threshold = writable(90);
export const cutoff = writable();

export function resetParameters() {
    threshold.set(90);
    cutoff.set(moment().subtract(1, "year"));
}