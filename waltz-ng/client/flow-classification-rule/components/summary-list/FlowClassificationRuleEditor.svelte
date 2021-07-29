<script>
    import _ from "lodash";

    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";

    import {mode, Modes, selectedClassificationRule} from "./editingFlowClassificationRulesState";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";

    export let doSave;
    export let doUpdate;
    export let doCancel;

    let savePromise;
    let workingCopy = Object.assign(
        {},
        $selectedClassificationRule,
        {classificationId: $selectedClassificationRule.classification?.id});

    const classificationCall = flowClassificationStore.findAll();

    $: classificationsById = _.keyBy($classificationCall.data, d => d.id);

    function getRequiredFields(d) {
        return [d.classificationId, d.app, d.orgUnit, d.dataType];
    }

    function fieldChanged(d) {
        return d.classificationId !== $selectedClassificationRule.classification?.id
            || d.description !== $selectedClassificationRule.description;
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
            classificationId: workingCopy.classificationId,
            applicationId: workingCopy.app.id,
            dataTypeId: workingCopy.dataType.id,
            parentReference: workingCopy.orgUnit
        };

        savePromise = doSave(cmd);
    }

    function submitUpdate() {
        const cmd = {
            description: workingCopy.description || "",
            classificationId: workingCopy.classificationId,
            id: workingCopy.id
        };

        $selectedClassificationRule = workingCopy;
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

    $: classifications = _
        .chain($classificationCall.data)
        .sortBy("position", "display_name")
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
                    <p class="text-muted">Datatype for which this application determines flow classifications</p>
                {:else}
                    <DataTypeTreeSelector on:select={onSelectDatatype}/>
                    <p class="text-muted">Select the datatype for which this application will determine the flow classification for</p>
                {/if}
            </div>
        </div>
        <div class="form-group">
            <label for="scope">Scope:</label>
            <div id="scope">
                <EntitySearchSelector on:select={onSelectScope}
                                      placeholder="Search for scope"
                                      entityKinds={['ORG_UNIT', 'ACTOR', 'APPLICATION']}>
                </EntitySearchSelector>
            </div>
            <p class="text-muted">Start typing to select the selector for which this flow classification rule will apply to</p>
        </div>
    {:else }

        <h3>
            <Icon name="desktop"/>
            {workingCopy.app.name}
        </h3>

        <h4>{workingCopy.dataType.name}</h4>

        <div>
            <strong>Scope:</strong>
            <EntityLabel ref={workingCopy.parentReference}/>
        </div>
        <p class="text-muted">The selector for applications this flow classification rule applies to</p>
    {/if}

    <label for="rating">Classification:</label>
    <div id="rating"
         class="form-group">
        {#each classifications as classification}
            <div class="radio">
                <label>
                    <input type=radio
                           bind:group={workingCopy.classificationId}
                           value={classification.id}>
                    <div class="rating-indicator-block"
                         style="background-color: {classification.color}">&nbsp;</div>
                    {classification.name}
                    <span class="help-block">{classification.description}</span>
                </label>
            </div>
        {/each}
        <p class="text-muted">Select an flow classification for this source</p>
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
                Failed to save flow classification rule. Reason: {e.data.message}
                <button class="btn-link"
                        on:click={() => savePromise = null}>
                    <Icon name="check"/>
                    Okay
                </button>
            </div>
    {/await}
{/if}


<style>
    .rating-indicator-block {
        display: inline-block;
        width: 1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }
</style>