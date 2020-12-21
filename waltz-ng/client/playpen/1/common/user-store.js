import { writable } from 'svelte/store';
import {CORE_API} from "../../../common/services/core-api-utils";

export function mkUserStore(serviceBroker) {
    const { subscribe, set } = writable(0);

    const load = (force = false) => serviceBroker
        .loadAppData(CORE_API.UserStore.whoami, [], {force})
        .then(r => set(r.data));

    load();

    return {
        subscribe,
        reload: () => load(true)
    };
}
