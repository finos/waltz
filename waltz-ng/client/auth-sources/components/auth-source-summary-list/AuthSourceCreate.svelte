<script>
    import EntitySearchSelector from "./EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let doSave;

    let savePromise;

    let newAuthSource = {
        description: null,
        rating: null,
        application: null,
        dataType: null,
        orgUnit: null
    };

    function onUpdateRating(event) {
        newAuthSource.rating = event.currentTarget.value;
    }

    function submitCreate() {

        const cmd = {
            description: newAuthSource.description || "",
            rating: newAuthSource.rating,
            applicationId: newAuthSource.application.id,
            dataTypeId: newAuthSource.dataType.id,
            orgUnitId: newAuthSource.orgUnit.id
        };

        savePromise = doSave(cmd);
    };

    function onSelectSource(evt) {
        newAuthSource.application = evt.detail;
    }

    function onSelectDatatype(evt) {
        newAuthSource.dataType = evt.detail;
    }

    function onSelectScope(evt) {
        newAuthSource.orgUnit = evt.detail;
    }


</script>

<div class="form-group">
    <label for="source">Source:</label>
    <div id="source">
        <EntitySearchSelector on:select={onSelectSource}
                              entityKinds={['APPLICATION']}>
        </EntitySearchSelector>
    </div>
</div>
<div class="form-group">
    <label for="datatype">Datatype:</label>
    <div id="datatype">
        <EntitySearchSelector on:select={onSelectDatatype}
                              entityKinds={['DATA_TYPE']}>
        </EntitySearchSelector>
    </div>
</div>
<div class="form-group">
    <label for="scope">Scope:</label>
    <div id="scope">
        <EntitySearchSelector on:select={onSelectScope}
                              entityKinds={['APPLICATION', 'ORG_UNIT']}>
        </EntitySearchSelector>
    </div>
</div>
<label for="rating">Rating:</label>
<div id="rating" class="form-group">
    <label>
        <input type=radio
               checked={newAuthSource.rating==='PRIMARY'}
               on:change={onUpdateRating}
               value={'PRIMARY'}>
        RAS
    </label>
    <label>
        <input type=radio
               checked={newAuthSource.rating==='SECONDARY'}
               on:change={onUpdateRating}
               value={'SECONDARY'}>
        Non-RAS
    </label>
</div>
<div class="form-group">
    <label for="description">Description:</label>
    <textarea class="form-control"
              id="description"
              bind:value={newAuthSource.description}/>
</div>

<button class="btn-link"
        on:click={submitCreate}>
    Save
</button>

{#if savePromise}
    {#await savePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
            <span class="alert alert-warning">
                Failed to save authority statement. Reason: {e.error}
                <button class="btn-link"
                        on:click={() => savePromise = null}>
                    <Icon name="check"/>
                    Okay
                </button>
            </span>
    {/await}
{/if}