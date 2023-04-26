import {writable} from "svelte/store";
import _ from "lodash";

function resettable(initialValue) {
    const {subscribe, set, update} = writable(initialValue);
    return {
        subscribe,
        set,
        update,
        reset: () => set(_.cloneDeep(initialValue))
    };
}

export default resettable;