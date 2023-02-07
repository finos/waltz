import {writable} from "svelte/store";
import {assessmentDefinitionStore} from "./assessment-definition";
import {entity} from "../common/services/enums/entity";


function createFavouriteStore() {
    const initialValues = _.reduce(
        entity,
        (acc, e) => { acc[e.key] = []; return acc;},
        {});

    function replaceData(r) {
        return set(_.groupBy(r.data, d => d.entityKind));
    }

    const {subscribe, set} = writable(initialValues);

    assessmentDefinitionStore
        .findFavouritesForUser()
        .subscribe(replaceData);

    return {
        subscribe,
        add: function(defId) {
            assessmentDefinitionStore
                .addFavourite(defId)
                .then(replaceData);
        },
        remove: function(defId) {
            assessmentDefinitionStore
                .removeFavourite(defId)
                .then(replaceData);
        }
    };
}


export const favouriteAssessmentDefinitionStore = createFavouriteStore();

