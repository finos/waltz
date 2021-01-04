import {writable} from "svelte/store";
import {CORE_API} from "../common/services/core-api-utils";


export function mkStaticPanelStore(serviceBroker) {
    const { subscribe, set } = writable([]);

    const load = (force = false) => serviceBroker
        .loadAppData(
            CORE_API.StaticPanelStore.findAll,
            [],
            {force})
        .then(r => set(r.data));

    const save = (panel) => serviceBroker
        .execute(CORE_API.StaticPanelStore.save, [panel])
        .then(() => load(true))

    load();

    return {
        load,
        save,
        subscribe
    };
}
