import {derived, writable} from "svelte/store";
import _ from "lodash";
import {mkAssessmentDefinitionsIdsBaseKey} from "../../../user";
import {userPreferenceStore} from "../../../svelte-stores/user-preference-store";
import {getIdsFromString} from "../../assessment-utils";

function writePreference(key, definitionIds) {
    const userPreference = {key, value: definitionIds.toString()};
    userPreferenceStore.saveForUser(userPreference);
}

export function createStores(primaryEntityRef) {

    const baseKey = mkAssessmentDefinitionsIdsBaseKey(primaryEntityRef);
    const favouriteIncludedKey = `${baseKey}.included`;
    const favouriteExcludedKey = `${baseKey}.excluded`;

    const defaultPrimaryList = writable([]);
    const favouriteExcludedIds = writable([]);
    const favouriteIncludedIds = writable([]);

    const favouriteIds = derived(
        [defaultPrimaryList, favouriteExcludedIds, favouriteIncludedIds],

        ([$defaultPrimaryList, $favouriteExcludedIds, $favouriteIncludedIds]) => {
            return _.reject(
                _.concat($defaultPrimaryList, $favouriteIncludedIds),
                d => _.includes($favouriteExcludedIds, d));
        })

    favouriteIds.subscribe(() => {
    })

    derived([favouriteIncludedIds], ($favouriteIncludedIds) => {
        writePreference(favouriteIncludedKey, $favouriteIncludedIds);
    }).subscribe(() => {
    });

    derived([favouriteExcludedIds], ($favouriteExcludedIds) => {
        writePreference(favouriteExcludedKey, $favouriteExcludedIds);
    }).subscribe(() => {
    });

    function setFromPreferences(userPreferences) {

        const includedFavouritesString = _.find(userPreferences, d => d.key === favouriteIncludedKey)
        const excludedFavouritesString = _.find(userPreferences, d => d.key === favouriteExcludedKey)

        favouriteIncludedIds.set(getIdsFromString(includedFavouritesString));
        favouriteExcludedIds.set(getIdsFromString(excludedFavouritesString));
    }

    return {
        defaultPrimaryList,
        favouriteIncludedIds,
        favouriteExcludedIds,
        favouriteIds,
        setFromPreferences
    }
}
