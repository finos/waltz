<script>
    import {entityAliasStore} from "../../svelte-stores/entity-alias-store";
    import TagsInput from "./TagsInput.svelte";
    import toasts from "../../svelte-stores/toast-store";
    import {displayError} from "../error-utils";

    export let parentEntityReference;
    export let editable = false;

    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT"
    };

    let fetchCall;
    let mode = Modes.VIEW;
    let aliases = [];

    function onCancel() {
        mode = Modes.VIEW;
    }

    function onSave(e) {
        entityAliasStore
            .updateForEntityReference(parentEntityReference, e.detail)
            .then(r => {
                aliases = r.data;
                toasts.success("Updated aliases");
                mode = Modes.VIEW;
            })
            .catch(d => displayError("Failed to update aliases", e));
    }

    $: fetchCall = parentEntityReference && entityAliasStore.fetchForEntityReference(parentEntityReference);
    $: aliases = $fetchCall?.data;
</script>

<div class="waltz-alias-list">
    {#if mode === Modes.VIEW && aliases !== undefined}
        <ul class="list-inline">
            {#each aliases as alias}
                <li class="tag">{alias}</li>
            {:else}
                <li class="text-muted">No aliases defined</li>
            {/each}
            {#if editable}
                <li>
                    <button class="btn-skinny"
                            on:click={() => mode = Modes.EDIT}>
                        Edit
                    </button>
                </li>
            {/if}
        </ul>
    {/if}

    {#if mode === Modes.EDIT}
        <TagsInput value={[...aliases]}
                   on:save={onSave}
                   on:cancel={onCancel}/>
    {/if}
</div>

<style>
    .tag {
        margin-right:0.33rem;
        padding: 0.2em;
        border-radius: 0.3em;
        background-color: #edf5fd;
        border: 1px solid #c0defa;
        margin-bottom: 0.2em;
    }
</style>
