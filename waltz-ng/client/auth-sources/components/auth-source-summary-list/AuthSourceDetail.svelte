<script>
    import {mode, Modes, selectedAuthSource} from "./editingAuthSources";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";


    export let doCancel;
    export let doDelete;

    let deletePromise;
    let selected = Object.assign({}, $selectedAuthSource);

    function onCancelDetailView() {
        doCancel();
    }

    function editAuthSource() {
        $mode = Modes.EDIT;
    }

    function deleteAuthSource() {
        deletePromise = doDelete($selectedAuthSource.id);
    }

</script>

{#if $mode === Modes.DETAIL}
    <h3>
        <Icon name="desktop"/>
        {$selectedAuthSource.app.name}
    </h3>

    <h4>{$selectedAuthSource.dataType.name}</h4>

    <div>
        <strong>Scope:</strong>
        <EntityLabel ref={$selectedAuthSource?.declaringOrgUnit}></EntityLabel>
    </div>
    <p class="text-muted">The selector for applications this authority statement will apply to</p>

    <div>
            <strong>Rating:</strong>
            <span>{selected?.rating === 'PRIMARY' ? 'RAS' : 'Non RAS' }</span>
            <p class="text-muted">{$selectedAuthSource?.ratingValue.description}</p>
    </div>

    <div>
            <strong>Notes:</strong>
            <span>{selected?.description || "None provided"}</span>
            <p class="text-muted">Additional notes</p>
    </div>
{/if}

<button class="btn btn-success"
        on:click={editAuthSource}>
    Edit
</button>
<button class="btn btn-danger"
        on:click={deleteAuthSource}>
    Delete
</button>
<button class="btn-link"
        on:click={onCancelDetailView}>
    Cancel
</button>


{#if deletePromise}
    {#await deletePromise}
        Deleting...
    {:then r}
        Deleted!
    {:catch e}
            <div class="alert alert-warning">
                Failed to delete authority statement. Reason: {e.data.message}
                <button class="btn-link"
                        on:click={() => {
                            deletePromise = null}}>
                    <Icon name="check"/>
                    Okay
                </button>
            </div>
    {/await}
{/if}