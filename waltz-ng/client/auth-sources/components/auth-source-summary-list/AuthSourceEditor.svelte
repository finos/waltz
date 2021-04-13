<script>
    import _ from "lodash";

    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";

    import {mode, Modes, selectedAuthSource} from "./editingAuthSourcesState";
    import {enumValueStore} from "../../../svelte-stores/enum-value-store";

    export let doSave;
    export let doUpdate;
    export let doCancel;

    let savePromise;
    let workingCopy = Object.assign({}, $selectedAuthSource);

    const enumCall = enumValueStore.load();

    function getRequiredFields(d) {
        return [d.rating, d.app, d.orgUnit, d.dataType];
    }

    function fieldChanged(d) {
        return d.rating !== $selectedAuthSource.rating || d.description !== $selectedAuthSource.description;
    }

    $: invalid = (workingCopy.id)
        ? !fieldChanged(workingCopy)
        : _.some(getRequiredFields(workingCopy), v => _.isNil(v));

    function save() {
        if (workingCopy.id) {
            submitUpdate();
        } else {
            submitCreate()
        }
    }

    function submitCreate() {

        const cmd = {
            description: workingCopy.description || "",
            rating: workingCopy.rating,
            applicationId: workingCopy.app.id,
            dataTypeId: workingCopy.dataType.id,
            orgUnitId: workingCopy.orgUnit.id
        };

        savePromise = doSave(cmd);
    }

    function submitUpdate() {
        const cmd = {
            description: workingCopy.description || "",
            rating: workingCopy.rating,
            id: workingCopy.id
        };

        $selectedAuthSource = workingCopy;
        savePromise = doUpdate(cmd);
    }

    function onSelectSource(evt) {
        workingCopy.app = evt.detail;
    }

    function onSelectDatatype(evt) {
        workingCopy.dataType = evt.detail;
    }

    function onSelectScope(evt) {
        workingCopy.orgUnit = evt.detail;
    }

    function cancel() {
        if (workingCopy.id) {
            $mode = Modes.DETAIL;
        } else {
            doCancel();
        }
    }

    function clearDataType() {
        workingCopy.dataType = null;
    }

    $: ratingsByKey = _
        .chain($enumCall.data)
        .filter(d => d.type === "AuthoritativenessRating")
        .keyBy(d => d.key)
        .value();

</script>


<form autocomplete="off"
      on:submit|preventDefault={save}>

    {#if !workingCopy.id}
        <div class="form-group">
            <label for="source">Source:</label>
            <div id="source">
                <EntitySearchSelector on:select={onSelectSource}
                                      placeholder="Search for source"
                                      entityKinds={['APPLICATION']}>
                </EntitySearchSelector>
            </div>
            <p class="text-muted">Start typing to select the source application</p>
        </div>
        <div class="form-group">
            <label for="datatype">Datatype:</label>
            <div id="datatype">
                {#if workingCopy.dataType}
                    <span>{workingCopy.dataType.name}</span>
                    <button class="btn-link"
                            on:click={clearDataType}>
                        <Icon name="close"/>
                    </button>
                    <p class="text-muted">Datatype for which this application is authoritative</p>
                {:else}
                    <DataTypeTreeSelector on:select={onSelectDatatype}/>
                    <p class="text-muted">Select the datatype for which this application is an authoritative source</p>
                {/if}
            </div>
        </div>
        <div class="form-group">
            <label for="scope">Scope:</label>
            <div id="scope">
                <EntitySearchSelector on:select={onSelectScope}
                                      placeholder="Search for scope"
                                      entityKinds={['ORG_UNIT']}>
                </EntitySearchSelector>
            </div>
            <p class="text-muted">Start typing to select the selector for applications this authority statement will apply to</p>
        </div>
    {:else }

        <h3>
            <Icon name="desktop"/>
            {workingCopy.app.name}
        </h3>

        <h4>{workingCopy.dataType.name}</h4>

        <div>
            <strong>Scope:</strong>
            <EntityLabel ref={workingCopy.declaringOrgUnit}/>
        </div>
        <p class="text-muted">The selector for applications this authority statement applies to</p>
    {/if}

    <label for="rating">Rating:</label>
    <div id="rating"
         class="form-group">
        <label>
            <input type=radio
                   bind:group={workingCopy.rating}
                   value='PRIMARY'>
            {_.get(ratingsByKey, ["PRIMARY", "name"], "-")}
        </label>
        <label>
            <input type=radio
                   bind:group={workingCopy.rating}
                   value='SECONDARY'>
            {_.get(ratingsByKey, ["SECONDARY", "name"], "-")}
        </label>
        <p class="text-muted">Select an authority statement for this source</p>
    </div>

    <div class="form-group">
        <label for="description">Notes:</label>
        <textarea class="form-control"
                  id="description"
                  bind:value={workingCopy.description}/>
    </div>
    <p class="text-muted">Additional notes</p>


    <button class="btn btn-success"
            type="submit"
            disabled={invalid || savePromise}>
        Save
    </button>
    <button class="btn-link"
            on:click|preventDefault={cancel}>
        Cancel
    </button>
</form>

{#if savePromise}
    {#await savePromise}
        Saving...
    {:then r}
        Saved!
    {:catch e}
            <div class="alert alert-warning">
                Failed to save authority statement. Reason: {e.data.message}
                <button class="btn-link"
                        on:click={() => savePromise = null}>
                    <Icon name="check"/>
                    Okay
                </button>
            </div>
    {/await}
{/if}