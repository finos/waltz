<script>
    import {createEventDispatcher} from "svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {diagram} from "../store/diagram";
    import {flowDiagramStore} from "../../../../svelte-stores/flow-diagram-store";

    export let diagramId;

    const dispatch = createEventDispatcher();

    let removePromise;

    function cancel() {
        dispatch("cancel");

    }

    function removeDiagram(){
        return removePromise = flowDiagramStore.deleteForId(diagramId);
    }


</script>


<h4>
    {$diagram.name}
</h4>
<p>Are you sure you want to remove this diagram?</p>
<br>
<button class="btn btn-danger" on:click={removeDiagram}>
    Remove
</button>
<button class="btn btn-default"
        on:click={cancel}>
    Cancel
</button>
{#if removePromise}
    {#await removePromise}
        removing...
    {:then r}
        removed!
    {:catch e}
        <div class="alert alert-warning">
            Failed to remove diagram. Reason: {e.error}
            <button class="btn-link"
                    on:click={() => removePromise = null}>
                <Icon name="check"/>
                Okay
            </button>
        </div>
    {/await}
{/if}

<style>

</style>