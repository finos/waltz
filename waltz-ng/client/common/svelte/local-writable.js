import _ from "lodash";
import {writable} from "svelte/store";


/**
 * Creates a writable store that persists to local storage
 *
 * @param key
 * @param initialValue
 * @returns {set, subscribe, reset}
 */
function localWritable(key, initialValue) {
    const {subscribe, set: setStore} = writable(initialValue);

    set(JSON.parse(localStorage.getItem(key)) ?? initialValue);

    function set(value) {
        setStore(value);
        writeToStorage(key, value);
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