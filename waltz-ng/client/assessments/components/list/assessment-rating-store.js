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

    const storesInitialised = writable(false);

    const favouriteIds = derived(
        [defaultPrimaryList, favouriteExcludedIds, favouriteIncludedIds],

        ([$defaultPrimaryList, $favouriteExcludedIds, $favouriteIncludedIds]) => {
            return _.reject(
                _.concat($defaultPrimaryList, $favouriteIncludedIds),
                d => _.includes($favouriteExcludedIds, d));
        })

    favouriteIds.subscribe(() => {
    })


    // check stores initialised
    derived([favouriteIncludedIds, storesInitialised], ([$favouriteIncludedIds, $storesInitialised]) => {
        if ($storesInitialised) {
            console.log("saving included")
            writePreference(favouriteIncludedKey, $favouriteIncludedIds);
        }
    }).subscribe(() => {
    });

    derived([favouriteExcludedIds, storesInitialised], ([$favouriteExcludedIds, $storesInitialised]) => {
        if ($storesInitialised) {
            console.log("saving excluded")
            writePreference(favouriteExcludedKey, $favouriteExcludedIds);
        }
    }).subscribe(() => {
    });

    function setFromPreferences(userPreferences) {

        const includedFavouritesString = _.find(userPreferences, d => d.key === favouriteIncludedKey)
        const excludedFavouritesString = _.find(userPreferences, d => d.key === favouriteExcludedKey)

        favouriteIncludedIds.set(getIdsFromString(includedFavouritesString));
        favouriteExcludedIds.set(getIdsFromString(excludedFavouritesString));

        console.log("favourites set")
        storesInitialised.set(true);
    }

    return {
        defaultPrimaryList,
        favouriteIncludedIds,
        favouriteExcludedIds,
        favouriteIds,
        setFromPreferences
    }
}
