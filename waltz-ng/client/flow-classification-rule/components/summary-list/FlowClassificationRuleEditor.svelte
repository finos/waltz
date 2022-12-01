<script>
    import _ from "lodash";

    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import ToastStore from "../../../svelte-stores/toast-store"

    import {mode, Modes, selectedClassificationRule} from "./editingFlowClassificationRulesState";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import {displayError} from "../../../common/error-utils";

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
        return [d.classificationId, d.subjectReference, d.orgUnit, d.dataType];
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
            submitUpdate()
                .then(() => ToastStore.success("Flow classification rule updated"))
                .catch(e => displayError("Failed to update flow classification rule", e));

        } else {
            submitCreate()
                .then(() => ToastStore.success("Flow classification rule created"))
                .catch(e => displayError("Failed to create new flow classification rule", e));
        }
    }

    function submitCreate() {

        const cmd = {
            description: workingCopy.description || "",
            classificationId: workingCopy.classificationId,
            subjectReference: workingCopy.subjectReference,
            dataTypeId: workingCopy.dataType.id,
            parentReference: workingCopy.orgUnit
        };

        savePromise = doSave(cmd);

        return savePromise
    }

    function submitUpdate() {
        const cmd = {
            description: workingCopy.description || "",
            classificationId: workingCopy.classificationId,
            id: workingCopy.id
        };

        $selectedClassificationRule = workingCopy;
        savePromise = doUpdate(cmd);
        return savePromise;
    }

    function onSelectSource(evt) {
        workingCopy.subjectReference = evt.detail;
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
        .filter(c => c.userSelectable || c.id === $selectedClassificationRule?.classification?.id)
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
                                      entityKinds={['APPLICATION', 'ACTOR']}>
                </EntitySearchSelector>
            </div>
            <p class="text-muted">Start typing to select the source application or actor</p>
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
                    <p class="text-muted">Datatype for which this application / actor determines flow
                        classifications</p>
                {:else}
                    <DataTypeTreeSelector on:select={onSelectDatatype}/>
                    <p class="text-muted">Select the datatype for which this application / actor will determine the flow
                        classification for</p>
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
            <EntityLabel ref={workingCopy.subjectReference}/>
        </h3>

        <h4>
            <EntityLabel ref={workingCopy.dataType}/>
        </h4>

        <div>
            <strong>Scope:</strong>
            <EntityLabel ref={workingCopy.vantagePointReference}/>
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
                           disabled={!classification.userSelectable}
                           bind:group={workingCopy.classificationId}
                           value={classification.id}>
                    <div class="rating-indicator-block"
                         style="background-color: {classification.color}">&nbsp;</div>
                    {classification.name}
                    <span class="help-block">{classification.description}</span>
                    {#if !classification.userSelectable}
                        <span class="help-block warning-icon">
                            <div style="display: inline-block"><Icon name="exclamation-triangle"/></div>
                            This classification is not user selectable
                        </span>
                    {/if}
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


<style type="text/scss">

  @import "../../../../style/_variables";

    .rating-indicator-block {
        display: inline-block;
        width: 1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }

    .warning-icon div {
        color: $waltz-amber
    }
</style>