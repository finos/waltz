<script>


    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import {displayError} from "../../../common/error-utils";
    import {
        bulkUploadLegalEntityRelationshipStore
    } from "../../../svelte-stores/bulk-upload-legal-entity-relationship-store";
    import {
        activeMode,
        inputString,
        Modes,
        resolvedRows,
        resolveResponse,
        saveResponse,
        sortedHeaders,
        uploadMode,
        UploadModes,
        anyErrors
    } from "./bulk-upload-relationships-store";
    import _ from "lodash";
    import toasts from "../../../svelte-stores/toast-store";
    import SaveUploadReport from "./SaveUploadReport.svelte";
    import ResolvedUploadReport from "./ResolvedUploadReport.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let relationshipKind;
    export let onDone = () => console.log("Done, close and reload rels");

    let resolveCall;
    let saveCall;
    let allowSaveWithErrors = false;

    function verifyEntries() {

        $activeMode = Modes.LOADING;

        const resolveParams = {
            inputString: $inputString,
            updateMode: $uploadMode,
            legalEntityRelationshipKindId: relationshipKind.id,
        };

        return resolveCall = bulkUploadLegalEntityRelationshipStore.resolve(resolveParams)
            .then(d => {
                $resolveResponse = d.data;
                $activeMode = Modes.RESOLVED;
                toasts.success("Resolved relationships, please review before saving");
            })
            .catch(e => {
                displayError("Could not resolve rows", e);
                $activeMode = Modes.INPUT;
            });
    }


    function saveRelationships() {

        $activeMode = Modes.LOADING;

        const saveParams = {
            inputString: $inputString,
            updateMode: $uploadMode,
            legalEntityRelationshipKindId: relationshipKind.id,
        };

        return saveCall = bulkUploadLegalEntityRelationshipStore.save(saveParams)
            .then(d => {
                $saveResponse = d.data;
                $activeMode = Modes.REPORT;
                toasts.success("Saved relationships and assessments");
            })
            .catch(e => {
                displayError("Could not save rows", e);
                $activeMode = Modes.RESOLVED;
            });

    }

    function cancel() {
        $activeMode = Modes.INPUT;
        allowSaveWithErrors = false;
    }

    function clearData() {
        $inputString = null;
    }

    function done() {
        cancel();
        clearData();
        onDone();
    }

</script>

{#if $activeMode === Modes.INPUT}

    <div class="help-block">
        Please ensure that the minimum columns of 'Entity External Id' and 'Legal Entity External Id', if you wish to
        add a
        'Comment' this will appear on the relationship.
    </div>

    <form on:submit|preventDefault={verifyEntries}>
        <div class="form-group">
            <label for="involvements">
                Relationships
            </label>
            <textarea id="involvements"
                      class="form-control"
                      rows="6"
                      placeholder="Please insert external identifiers and email as comma or tab separated values split by newline or pipe characters"
                      bind:value={$inputString}></textarea>
        </div>

        <div class="form-group">
            <label>
                <input style="display: inline-block;"
                       type="radio"
                       bind:group={$uploadMode}
                       name="uploadMode"
                       value={UploadModes.ADD_ONLY}>
                Add Only
            </label>

            <label>
                <input style="display: inline-block;"
                       type="radio"
                       bind:group={$uploadMode}
                       name="uploadMode"
                       value={UploadModes.REPLACE}>
                Replace
            </label>
        </div>

        <button type="submit"
                class="btn btn-success"
                disabled={_.isNil($inputString) || _.isNull(relationshipKind)}>
            Search
        </button>
    </form>
{:else if $activeMode === Modes.LOADING}
    <LoadingPlaceholder>
        Resolving rows...
    </LoadingPlaceholder>
{:else if $activeMode === Modes.RESOLVED}

    <ResolvedUploadReport/>

    <div style="padding-bottom: 1em">
        {#if $anyErrors && !allowSaveWithErrors}
            <Icon name="exclamation-triangle"/>
            There are some relationships with errors, please review the input or
            <button class="btn btn-skinny"
                    on:click={() => allowSaveWithErrors = true}>
                enable save
            </button>
            to proceed.
        {:else if $anyErrors}
            <Icon name="exclamation-triangle"/>
            There are some relationships with errors, save has been enabled. Any rows with errors will be ignored.
        {/if}
    </div>

    <button class="btn btn-default"
            on:click={cancel}>
        Edit Input
    </button>

    <button class="btn btn-success"
            disabled={$anyErrors && !allowSaveWithErrors}
            on:click={saveRelationships}>
        Save Relationships
    </button>
{:else if $activeMode === Modes.REPORT}
    <SaveUploadReport/>
    <button class="btn btn-success"
            on:click={done}>
        Done
    </button>
{/if}
