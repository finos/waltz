<script>
    import {selectedAuthSource} from "./editingAuthSources";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let doUpdate;

    let updatePromise;
    let editing = false
    let selected = Object.assign({}, $selectedAuthSource);

    function onUpdateRating(event) {
        selected.rating = event.currentTarget.value;
    }

    function submitUpdate() {

        const cmd = {
            description: selected.description || "",
            rating: selected.rating,
            id: selected.id
        };

        updatePromise = doUpdate(cmd);

        selectedAuthSource.set(selected);
        editing = false;

    }

</script>

{#if $selectedAuthSource}
    <h3>{$selectedAuthSource.app.name}
        <span class="text-muted small">({$selectedAuthSource.appOrgUnit.name} - {$selectedAuthSource.appOrgUnit.id})</span>
    </h3>

    <h4>{$selectedAuthSource.dataType.name}</h4>

    <div>
        <strong>Scope:</strong>
        <EntityLabel ref={$selectedAuthSource?.declaringOrgUnit}></EntityLabel>
    </div>
    <p class="small text-muted">The selector for applications this authority statement will apply to</p>

    <div>
        {#if editing}
            <label for="rating">Rating:</label>
            <div id="rating" class="form-group">
                <label>
                    <input type=radio
                           checked={selected.rating==='PRIMARY'}
                           on:change={onUpdateRating}
                           value={'PRIMARY'}>
                    RAS
                </label>
                <label>
                    <input type=radio
                           checked={selected.rating==='SECONDARY'}
                           on:change={onUpdateRating}
                           value={'SECONDARY'}>
                    Non-RAS
                </label>
            </div>
            <div class="form-group">
            <label for="description">Notes:</label>
            <textarea class="form-control"
                      id="description"
                      bind:value={selected.description}/>
        </div>
        {:else}
            <strong>Rating:</strong>
            <span>{selected?.rating === 'PRIMARY' ? 'RAS' : 'Non RAS' }</span>
            <p class="small text-muted">{$selectedAuthSource?.ratingValue.description}</p>

            <strong>Notes:</strong>
            <span>{selected?.description || "None provided"}</span>
            <p class="small text-muted">Additional notes</p>
        {/if}
    </div>
{/if}

{#if !editing}
    <button class="btn-link"
            on:click={() => editing = true}>
        Edit
    </button>
{:else}
    <button class="btn-link"
            on:click={submitUpdate}>
        Save
    </button>
{/if}



{#if updatePromise}
    {#await updatePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
            <span class="alert alert-warning">
                Failed to update authority statement. Reason: {e.error}
                <button class="btn-link"
                        on:click={() => updatePromise = null}>
                    <Icon name="check"/>
                    Okay
                </button>
            </span>
    {/await}
{/if}