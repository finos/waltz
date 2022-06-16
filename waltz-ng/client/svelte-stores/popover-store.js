import {writable} from "svelte/store";

function createStore() {
    const {subscribe, update} = writable(null);


    /**
     * Creates popover using the following parameters
     * ```
     * {
     *     component: optional svelte component to render
     *     props: optional props to pass to svelte component
     *     content: optional html to render
     *     title: optional title for the popover
     * }
     * ```
     *
     * it is expected that one of `component` or `content` is provides
     * @param popover
     */
    const add = (popover) => {
        // Create a unique ID so we can easily find/remove it
        // if it is dismissible/has a timeout.
        const id = Math.floor(Math.random() * 10000);

        // Setup some sensible defaults for a popover.
        const defaults = {
            id,
            title: null,
        };

        update((store) => Object.assign({}, defaults, popover));

    };

    const dismiss = () => {
        update((store) => null);
    };


    return {
        subscribe,
        dismiss,
        add,
    }
}

export default createStore();