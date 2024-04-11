<script>
    import {createEventDispatcher} from "svelte";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import AddAnnotationSubPanel from "./AddAnnotationSubPanel.svelte";
    import {toGraphId} from "../../../flow-diagram-utils";
    import model from "../store/model";

    export let selected;
    export let canEdit
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
        model.removeAnnotation( {id: toGraphId(selected), data: selected});
        cancel();
    }

    const nodeKinds = ["APPLICATION", "ACTOR", "END_USER_APPLICATION"];


    $: isNode = _.includes(nodeKinds, selected.entityReference.kind);
    $: selectedGraphId = toGraphId(selected.entityReference);

    $: owningEntity = (isNode)
        ? _.find($model.nodes, d => d.id === selectedGraphId)
        : _.find($model.flows, d => d.id === selectedGraphId)

    $: owningEntityName = (isNode)
        ? owningEntity.data.name
        : owningEntity.data.source.name + " -> " + owningEntity.data.target.name;

</script>

<div>
    <strong>
        Annotation linked to <EntityLink ref={selected.entityReference}><span>{owningEntityName}</span></EntityLink>
    </strong>
</div>


{#if activeMode === Modes.VIEW}
<div>
    <span>{selected.note}</span>
</div>
<div class="context-panel-footer">
    {#if canEdit}
    <button class="btn btn-skinny"
            on:click={() => activeMode = Modes.EDIT}>
        Edit
    </button>
    |
    <button class="btn btn-skinny"
            on:click={() => removeAnnotation()}>
        Remove
    </button>
    |
    {/if}
    <button class="btn btn-skinny"
            on:click={() => cancel()}>
        Cancel
    </button>

</div>
{:else if activeMode === Modes.EDIT}
    <AddAnnotationSubPanel selected={selected}
                           on:cancel={() => cancel()}/>
{/if}

<style>
    .context-panel-footer {
        border-top: 1px solid #eee;
        margin-top:0.5em;
        padding-top:0.5em;
    }
</style>