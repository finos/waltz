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
    import Markdown from "../../../common/svelte/Markdown.svelte";

    export let relationshipKindId;
    export let onDone = () => console.log("Done, close and reload rels");

    let resolveCall;
    let saveCall;
    let allowSaveWithErrors = false;

    let helpText =
        "Please ensure the minimum required columns of 'Entity External Id' and 'Legal Entity External Id' are populated." +
        " You can copy and paste from an existing excel sheet or use the example layout below." +
        " If you with to remove an assessment you must mark with an `X` or `Y` in the 'Remove Relationship' column.\n\n" +
        "\n" +
        "**Example Input:**\n" +
        "| Entity External Id | Legal Entity External Id | Comment    | Remove Relationship   |\n" +
        "|--------------------|--------------------------|------------|-----------------------|\n" +
        "| `ENTITY_EXT_ID`    | `LEGAL_ENTITY_EXT_ID`    | `COMMENT_TEXT` (optional) | `X` or `Y` (optional) |\n" +
        "\n" +
        "\n" +
        "There are multiple ways to designate assessment ratings for a relationship:\n" +
        "1) Use the `ASSESSMENT_DEFN_EXT_ID / RATING_EXT_ID` format in the header, this gives one column per rating outcome and can be used to provide comments per assessment rating.\n" +
        "2) Use the `ASSESSMENT_DEFN_EXT_ID` format in the header, the values for each of the rows is then interpreted as a `;` separated list of rating codes."

    function verifyEntries() {

        $activeMode = Modes.LOADING;

        const resolveParams = {
            inputString: $inputString,
            updateMode: $uploadMode,
            legalEntityRelationshipKindId: relationshipKindId,
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
            legalEntityRelationshipKindId: relationshipKindId,
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

    <Markdown text={helpText}/>

    <hr>

    <form on:submit|preventDefault={verifyEntries}>
        <div class="form-group">
            <label for="relationships">
                Relationships Input
            </label>
            <textarea id="relationships"
                      class="form-control"
                      rows="6"
                      placeholder="Please insert data as comma or tab separated values split by newline or pipe characters"
                      bind:value={$inputString}></textarea>
        </div>

        <div class="form-group">
            <label>
                <input xxstyle="display: inline-block;"
                       type="radio"
                       bind:group={$uploadMode}
                       name="uploadMode"
                       value={UploadModes.ADD_ONLY}>
                Add Only
            </label>
            <span class="text-muted"> - This will only add or update values for relationships and assessments specified in the input</span>
            <br>
            <label>
                <input type="radio"
                       bind:group={$uploadMode}
                       name="uploadMode"
                       value={UploadModes.REPLACE}>
                Replace
                <span class="text-muted"> - This will replace any assessment ratings for relationships specified in the input</span>
            </label>
        </div>

        <button type="submit"
                class="btn btn-success"
                disabled={_.isNil($inputString) || _.isNull(relationshipKindId)}>
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
