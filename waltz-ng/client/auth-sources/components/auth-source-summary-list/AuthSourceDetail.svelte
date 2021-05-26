<script>
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    import {mode, Modes, selectedAuthSource} from "./editingAuthSourcesState";
    import {enumValueStore} from "../../../svelte-stores/enum-value-store";


    export let doCancel;
    export let doDelete;
    export let canEdit = false;

    const enumCall = enumValueStore.load();

    $: ratingsByKey = _
        .chain($enumCall.data)
        .filter(d => d.type === "AuthoritativenessRating")
        .keyBy(d => d.key)
        .value();

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
    <div>
        <strong>Application:</strong>
        <EntityLink ref={$selectedAuthSource.app}/>
    </div>
    <p class="text-muted">The authoritative application</p>


    <div>
        <strong>Data Type:</strong>
        <EntityLink ref={$selectedAuthSource.dataType}/>
    </div>
    <p class="text-muted">The data type this application is authoritative for</p>

    <div>
        <strong>Scope:</strong>
        <EntityLink ref={$selectedAuthSource?.parentReference}/>
    </div>
    <p class="text-muted">The selector for which this authority statement will apply to</p>

    <div>
        <strong>Rating:</strong>
        <span>{_.get(ratingsByKey, [selected?.rating, "name"], "-")}</span>
        <p class="text-muted">{_.get(ratingsByKey, [selected?.rating, "description"], "-")}</p>
    </div>

    <div>
        <strong>Notes:</strong>
        <span>{selected?.description || "None provided"}</span>
        <p class="text-muted">Additional notes</p>
    </div>
{/if}

{#if canEdit}
<button class="btn btn-success"
        on:click={editAuthSource}>
    Edit
</button>
<button class="btn btn-danger"
        on:click={deleteAuthSource}>
    Delete
</button>
{/if}


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