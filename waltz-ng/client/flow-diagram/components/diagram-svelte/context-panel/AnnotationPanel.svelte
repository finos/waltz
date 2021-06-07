<script>
    import {createEventDispatcher} from "svelte";
    import {processor, store} from "../diagram-model-store";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import {toGraphId} from "../../../flow-diagram-utils";

    export let selected;
    const dispatch = createEventDispatcher();

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
    };

    let activeMode = Modes.VIEW;

    function cancel() {
        dispatch("cancel");
    }

    function removeAnnotation() {
        const removeCmd = {command: "REMOVE_ANNOTATION", payload: {id: toGraphId(selected), data: selected}};
        $processor([removeCmd])
    }

    $: isApp = selected.entityReference.kind === 'APPLICATION'

    $: owningEntity = (isApp)
        ? _.find($store?.model?.nodes, d => d.data.id === selected.entityReference.id)
        : _.find($store?.model?.flows, d => d.data.id === selected.entityReference.id)

    $: owningEntityName = (isApp)
        ? owningEntity.data.name
        : owningEntity.data.source.name + " -> " + owningEntity.data.target.name

</script>

<div>
    <strong>
        Annotation linked to <EntityLink ref={selected.entityReference}><span>{owningEntityName}</span></EntityLink>
    </strong>
    <pre>{JSON.stringify(selected, "", null)}</pre>
</div>


{#if activeMode === Modes.VIEW}
<div>
    <span>{selected.note}</span>
</div>
<span>
    <button class="btn btn-link"
            on:click={() => cancel()}>
        Cancel
    </button>
    |
    <button class="btn btn-link"
            on:click={() => activeMode = Modes.EDIT}>
        Edit
    </button>
    |
    <button class="btn btn-link"
            on:click={() => removeAnnotation()}>
        Remove
    </button>
</span>
{:else if activeMode === Modes.EDIT}
    <AddAnnotationSubPanel selected={selected}
                           on:cancel={() => cancel()}/>
{/if}

<style>
</style>