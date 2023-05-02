import _ from "lodash";
import {writable} from "svelte/store";


function getExistingValueFromLocalStore(key) {
    try {
        return JSON.parse(localStorage.getItem(key))
    } catch (e) {
        return null;
    }
}

/**
 * Creates a writable store that persists to local storage
 *
 * @param key
 * @param initialValue
 * @returns {set, subscribe, reset}
 */
function localWritable(key, initialValue) {
    const {subscribe, set: setStore} = writable(initialValue);

    set(getExistingValueFromLocalStore(key) ?? initialValue);

    function set(value) {
        const valToStore = _.isUndefined(value)
            ? null
            : value;

        setStore(valToStore);
        writeToStorage(key, valToStore);
    }

    function reset() {
        setStore(_.cloneDeep(initialValue));
        writeToStorage(key, initialValue);
    }

    function writeToStorage(key, initialValue) {
        const value = JSON.stringify(initialValue);
        localStorage.setItem(key, value);
    }

    return {
        subscribe,
        set,
        reset,
    };
}

export default localWritable;