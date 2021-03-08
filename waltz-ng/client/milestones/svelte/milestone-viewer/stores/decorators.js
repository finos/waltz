import {writable} from "svelte/store";

//Scales
export const commonYScale = writable(null);
export const useCommonYScale = writable(false);
export const dateScale = writable(null);

//Colors
export const backgroundColors = writable(null);
export const foregroundColors = writable(null);
