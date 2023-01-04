import {derived, writable} from "svelte/store";
import _ from "lodash";

export let defaultPrimaryList = writable([]);
export let favouriteExcludedIds = writable([]);
export let favouriteIncludedIds = writable([]);


export let favouriteIds = derived(
    [defaultPrimaryList, favouriteExcludedIds, favouriteIncludedIds],

    ([$defaultPrimaryList, $favouriteExcludedIds, $favouriteIncludedIds]) => {

        console.log({$defaultPrimaryList, $favouriteIncludedIds, $favouriteExcludedIds});

        return _.reject(
            _.concat($defaultPrimaryList, $favouriteIncludedIds),
            d => _.includes($favouriteExcludedIds, d));
    });
