<script>

    import {createEventDispatcher, getContext} from "svelte";
    import {aggregateOverlayDiagramCalloutStore} from "../../../../svelte-stores/aggregate-overlay-diagram-callout-store";
    import toasts from "../../../../svelte-stores/toast-store"
    import {displayError} from "../../../../common/error-utils";

    const dispatch = createEventDispatcher();

    let selectedCellCallout = getContext("selectedCellCallout");

    let callouts = getContext("callouts");
    let selectedInstance = getContext("selectedInstance");
    let svgDetail = getContext("svgDetail");
    let calloutsCall;


    function deleteCallout() {
        let deletePromise = aggregateOverlayDiagramCalloutStore.remove($selectedCellCallout.id);
        Promise.resolve(deletePromise)
            .then(() => {
                toasts.success("Successfully removed callout");
                calloutsCall = aggregateOverlayDiagramCalloutStore.findCalloutsByDiagramInstanceId($selectedInstance.id, true);
                $callouts = $calloutsCall?.data;
                cancel();
            })
            .catch(e => displayError("Failed to remove callout", e))
    }

    function cancel() {
        dispatch("cancel");
    }


</script>


<h4>Are you sure you want to delete the callout:</h4>

<button class="btn btn-danger"
        on:click={deleteCallout}>
    Delete
</button>
<button class="btn btn-skinny"
        on:click={cancel}>
    Cancel
</button>