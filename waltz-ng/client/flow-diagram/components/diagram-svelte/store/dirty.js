import {writable} from "svelte/store";

const dirty = writable(false);

export default dirty;