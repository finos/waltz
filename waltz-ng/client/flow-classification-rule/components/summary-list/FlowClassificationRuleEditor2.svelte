<script>
    import _ from "lodash";
    import {createEventDispatcher} from "svelte";
    import EntitySearchSelector from "../../../common/svelte/EntitySearchSelector.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    export let formData;

    const dispatch = createEventDispatcher();

    export let inboundFlowClassifications = [];
    export let outboundFlowClassifications = [];

    let selectableOutboundFlowClassifications = [];
    let selectableInboundFlowClassifications = [];
    let isNew = formData && _.isNil(formData.id);
    let invalid = true;

    function anyMissingProps(props = []) {
        return _.some(
            props,
            p => console.log({p, d: formData[p], v: _.isNil(formData[p])}) || _.isNil(formData[p]));
    }

    function doUpdate() {
        dispatch("save", formData);
    }

    function onCancel() {
        dispatch("cancel");
    }

    function clearDataType() {
        formData.dataType = null;
    }

    function onSelectScope(evt) {
        formData.vantagePoint = evt.detail;
    }

    function onSelectDatatype(evt) {
        formData.dataType = evt.detail;
    }

    function onSelectSubject(evt) {
        formData.subject = evt.detail;
    }

    function toSubjectDirection(classification = {}) {
        switch (_.get(classification, "direction")) {
            case "INBOUND":
                return "(Consumer)";
            case "OUTBOUND":
                return "(Producer)";
            default:
                return "";
        }
    }

    function toScopeDirection(classification = {}) {
        switch (_.get(classification, "direction")) {
            case "INBOUND":
                return "(Producer/s)";
            case "OUTBOUND":
                return "(Consumer/s)";
            default:
                return "";
        }
    }

    $: selectableOutboundFlowClassifications = _.filter(outboundFlowClassifications, c => c.userSelectable);
    $: selectableInboundFlowClassifications = _.filter(inboundFlowClassifications, c => c.userSelectable);
    $: {
        invalid = formData && isNew
            ? anyMissingProps(["description", "subject", "vantagePoint", "classification"])
            : anyMissingProps(["description", "classification"]);
    }

    $: console.log({formData, invalid, isNew})

</script>

{#if !_.isNil(formData)}
    <form autocomplete="off"
          on:submit|preventDefault={doUpdate}>

        <label for="classification">Classification</label>
        <select class="form-control"
                id="classification"
                bind:value={formData.classification}>
            {#if !_.isEmpty(outboundFlowClassifications)}
                <optgroup label="Producer Classifications">
                    {#each selectableOutboundFlowClassifications as c}
                        <option value={c}>
                            {c.name}
                        </option>
                    {/each}
                </optgroup>
            {/if}
            {#if !_.isEmpty(inboundFlowClassifications)}
                <optgroup label="Consumer Classifications">
                    {#each selectableInboundFlowClassifications as c}
                        <option value={c}>
                            {c.name}
                        </option>
                    {/each}
                </optgroup>
            {/if}
        </select>

        {#if isNew}
            <label for="source">Subject {toSubjectDirection(formData.classification)}</label>
            <div id="source">
                <EntitySearchSelector on:select={onSelectSubject}
                                      placeholder="Search for subject"
                                      entityKinds={['APPLICATION', 'ACTOR', 'END_USER_APPLICATION']}>
                </EntitySearchSelector>
            </div>
            <p class="text-muted">Start typing to select the subject application, actor or end user application</p>

            <label for="datatype">Datatype</label>
            <div id="datatype">
                {#if formData.dataType}
                    <span>{formData.dataType.name}</span>
                    <button class="btn-link"
                            on:click={clearDataType}>
                        <Icon name="close"/>
                    </button>
                    <p class="text-muted">Datatype for which this application / actor determines flow
                        classifications</p>
                {:else}
                    <DataTypeTreeSelector on:select={onSelectDatatype}/>
                    <p class="text-muted">Select the datatype which this rule will be valid for.  All child data types are included.</p>
                    <div class="waltz-warning">
                        By not selecting a data type this rule will apply to <strong>all</strong> types
                    </div>
                {/if}
            </div>

            <label for="scope">Scope {toScopeDirection(formData.classification)}</label>
            <div id="scope">
                <EntitySearchSelector on:select={onSelectScope}
                                      placeholder="Search for scope"
                                      entityKinds={['ORG_UNIT', 'ACTOR', 'APPLICATION', 'END_USER_APPLICATION']}>
                </EntitySearchSelector>
            </div>
            <p class="text-muted">Start typing to select the selector for which this flow classification rule will apply to. This could be an organisational unit, application, edn user application or actor.</p>


        {/if}


        <label for="description">Description</label>
        <textarea class="form-control"
                  bind:value={formData.description}
                  id="description"
                  required="true"
                  placeholder="Description"/>
        <div class="help-block">
            Description to provide additional data about this rule
        </div>

        <label for="severity">Severity</label>
        <select class="form-control"
                id="severity"
                bind:value={formData.severity}>
            <option value={null}>
                None
            </option>
            <option value="INFORMATION">
                Information
            </option>
            <option value="WARNING">
                Warning
            </option>
            <option value="ERROR">
                Error
            </option>
        </select>
        <div class="help-block">
            Optional, classification rules can display a message to users when configuring data types on flows.
            If a severity is selected the message must be defined below.
        </div>

        {#if formData.severity}
            <label for="message">Message</label>
            <textarea class="form-control"
                      bind:value={formData.message}
                      id="message"
                      required={formData.severity !== null}
                      placeholder="Message"/>
            <div class="help-block">
                Message to show when a user selects a matching data type
            </div>
        {/if}


        <button type="submit"
                disabled={invalid}
                class="btn btn-xs btn-success">
            {isNew ? "Create" : "Save"}
        </button>
        <button class="btn btn-xs btn-primary"
                on:click={onCancel}>
            Cancel
        </button>
    </form>
{/if}

<style>
    form label {
        padding-top: 0.6em;
    }
</style>