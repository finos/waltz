<script>

    import {toGraphId} from "../../../flow-diagram-utils";
    import {createEventDispatcher} from "svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import AddPhysicalFlowSubPanel from "./AddPhysicalFlowSubPanel.svelte";
    import model from "../store/model";

    export let selected;
    export let canEdit;
    const dispatch = createEventDispatcher();

    const Modes = {
        MENU: "MENU",
        ADD_PHYSICAL_FLOW: "ADD_PHYSICAL_FLOW",
        ADD_ANNOTATION: "ADD_ANNOTATION",
        REMOVAL: "REMOVAL"
    };

    let activeMode = Modes.MENU;


    function removeFlow() {
        model.removeFlow({id: toGraphId(selected.flow), data: selected.flow});
        cancel()
    }

    function cancel() {
        dispatch("cancel");
    }

</script>



<div>
    <strong>{selected.flow.source.name} &rarr {selected.flow.target.name}</strong>
    <div>
        {#if selected.decorations.length > 0}
            This flow has {selected.decorations.length} physical flow decorators associated.
        {:else}
            No physical flow decorators have been associated to this flow.
        {/if}
    </div>
</div>

{#if activeMode === Modes.MENU}
<ul>
    {#if canEdit}
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_ANNOTATION}>
            Add annotation
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD_PHYSICAL_FLOW}>
            Edit physical flows
        </button>
    </li>
    <li>
        <button class="btn btn-skinny"
                on:click={() => removeFlow()}>
            Remove
        </button>
    </li>
    {/if}
    <li>
        <button class="btn btn-skinny"
                on:click={() => cancel()}>
            Cancel
        </button>
    </li>
</ul>
{:else if activeMode === Modes.ADD_ANNOTATION}
    <AddAnnotationSubPanel selected={selected.flow}
                           on:cancel={() => activeMode = Modes.MENU}/>
{:else if activeMode === Modes.ADD_PHYSICAL_FLOW}
    <AddPhysicalFlowSubPanel selected={selected.flow}
                             on:cancel={() => activeMode = Modes.MENU}/>
{/if}


<style>
    ul {
        padding: 0;
        margin: 0;
        list-style: none;
    }

    li {
        padding-top: 0;
    }
</style>