import {writable} from "svelte/store";

function createStore() {
    const {subscribe, update} = writable([]);

    const add = (toast) => {
        // Create a unique ID so we can easily find/remove it
        // if it is dismissible/has a timeout.
        const id = Math.floor(Math.random() * 10000);

        // Setup some sensible defaults for a toast.
        const defaults = {
            id,
            type: "info",
            dismissible: true,
            timeout: 3000
        };

        // Push the toast to the top of the list of toasts
        update((all) => [Object.assign(defaults, toast), ...all]);

        // If toast is dismissible, dismiss it after "timeout" amount of time.
        if (toast.timeout) setTimeout(() => dismiss(id), toast.timeout);
    };

    const success = (message) => {
        const success = {
            type: "success",
            dismissible: true,
            timeout: 3000,
            message: message
        };
        add(success);
    };

    const warning = (message) => {
        const warning = {
            type: "warning",
            dismissible: true,
            timeout: 3000,
            message: message
        };
        add(warning);
    }

    const error = (message) => {
        const error = {
            type: "error",
            dismissible: true,
            timeout: 6000,
            message: message
        };
        add(error);
    }

    const info = (message) => {
        const info = {
            type: "info",
            dismissible: true,
            timeout: 3000,
            message: message
        };
        add(info);
    }

    const confirmInfo = (message) => {
        const infoNoTimeout = {
            type: "info",
            dismissible: true,
            message: message
        };
        add(infoNoTimeout);
    }

    const dismiss = (id) => {
        update((all) => all.filter((t) => t.id !== id));
    };


    return {
        subscribe,
        dismiss,
        success,
        warning,
        error,
        info,
        confirmInfo
    }
}

export default createStore();