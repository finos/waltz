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
        uploadMode,
        UploadModes,
        sortedHeaders
    } from "./bulk-upload-relationships-store";
    import _ from "lodash";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let relationshipKind;

    let resolveCall;
    let erroredHeaders;
    let erroredRelationships;

    function verifyEntries() {

        $activeMode = Modes.LOADING;

        console.log({is: $inputString, rel: relationshipKind, mode: $uploadMode});

        const resolveParams = {
            inputString: $inputString,
            updateMode: $uploadMode,
            legalEntityRelationshipKindId: relationshipKind.id,
        };

        return resolveCall = bulkUploadLegalEntityRelationshipStore.resolve(resolveParams)
            .then(d => {
                console.log({d});
                $resolveResponse = d.data;
                $activeMode = Modes.RESOLVED;
                $sortedHeaders = _.sortBy(d.data.assessmentHeaders, d => d.columnId);
                $resolvedRows = _.sortBy(d.data.rows, d => d.rowNumber);
            })
            .catch(e => {
                displayError("Could not resolve rows", e);
                $activeMode = Modes.INPUT;
            });
    }


    function saveRelationships() {

        const saveParams = {
            inputString: $inputString,
            updateMode: $uploadMode,
            legalEntityRelationshipKindId: relationshipKind.id,
        };

        const saveCall = bulkUploadLegalEntityRelationshipStore.save(saveParams)
            .then(d => {
                console.log({data: d.data});
                $activeMode = Modes.REPORT;
            })
            .catch(e => {
                displayError("Could not save rows", e);
                $activeMode = Modes.RESOLVED
            });

    }

    function cancel() {
        $activeMode = Modes.INPUT;
        console.log("Saving");
    }

    $: {


        if ($resolveResponse) {

            erroredHeaders = _.filter($resolveResponse.assessmentHeaders, d => d.status !== "HEADER_FOUND");
            erroredRelationships = _.filter($resolveResponse.rows, d => d.legalEntityRelationship.status === "ERROR");

        }

    }

    $: console.log({
        resolved: $resolveResponse,
        mode: $activeMode,
        rows: $resolvedRows,
        input: $inputString,
        rel: relationshipKind
    });

</script>

<div class="help-block">
    Please ensure that the minimum columns of 'Entity External Id' and 'Legal Entity External Id', if you wish to add a
    'Comment' this will appear on the relationship
</div>

{#if $activeMode === Modes.INPUT}
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

    <div class="help-block">

        <div>Resolved {$resolvedRows.length} rows of data.</div>

        {#if !_.isEmpty(erroredHeaders)}
            <div>There are {erroredHeaders.length} assessment header/s which could not be resolved.</div>
        {/if}

        {#if !_.isEmpty(erroredRelationships)}
            <div>There are {erroredRelationships.length} relationship/s which could not be resolved.</div>
        {/if}

    </div>

    <div class="waltz-scroll-region-300" style="margin-bottom: 2em">
        <table class="table table-condensed small">
            <colgroup>
                <col width="10%"/>
                <col width="15%"/>
                <col width="15%"/>
                <col width="20%"/>
            </colgroup>
            <thead>
            <tr>
                <th>Operation</th>
                <th>Target Entity</th>
                <th>Legal Entity</th>
                <th>Comment</th>
                {#each $sortedHeaders as header}
                    <th>
                        {header.inputString}
                        {#if header.status !== "HEADER_FOUND"}
                            <span style="color: red"><Icon name="exclamation-triangle"/></span>
                        {/if}
                    </th>
                {/each}
            </tr>
            </thead>
            <tbody>
            {#each $resolvedRows as row}
                {@const assessmentByColumnId = _.keyBy(row.assessmentRatings, d => d.columnId)}
                <tr class:relationship-error={row.legalEntityRelationship.operation === "ERROR"}>
                    <td class="relationship-cell">
                        <span>
                            {row.legalEntityRelationship.operation}
                        </span>
                    </td>
                    <td class="relationship-cell">
                        <span>
                            {row.legalEntityRelationship.targetEntityReference.inputString}
                            {#if _.isNil(row.legalEntityRelationship.targetEntityReference.resolvedEntityReference)}
                                <span style="color: red"><Icon name="exclamation-triangle"/></span>
                            {/if}
                        </span>
                    </td>
                    <td class="relationship-cell">
                        <span>{row.legalEntityRelationship.legalEntityReference.inputString}</span>
                        {#if _.isNil(row.legalEntityRelationship.legalEntityReference.resolvedEntityReference)}
                            <span style="color: red"><Icon name="exclamation-triangle"/></span>
                        {/if}
                    </td>
                    <td class="relationship-cell"
                        style="border-right: 1px dashed #ccc">
                        {row.legalEntityRelationship.comment || ""}
                    </td>
                    {#each $sortedHeaders as header}
                        {@const assessmentRating = _.get(assessmentByColumnId, header.columnId)}
                        <td class:assessment-error={_.includes(assessmentRating?.statuses, "ERROR") || header.status !== "HEADER_FOUND"}>
                            <span>
                                {#if assessmentRating}
                                    {assessmentRating?.inputString}
                                    {#if _.includes(assessmentRating?.statuses, "ERROR")}
                                        <span style="color: red"><Icon name="exclamation-triangle"/></span>
                                    {/if}
                                {/if}
                            </span>
                        </td>
                    {/each}
                </tr>
            {/each}
            </tbody>
        </table>
    </div>

    <button class="btn btn-default"
            on:click={cancel}>
        Edit Input
    </button>

    <button class="btn btn-success"
            on:click={saveRelationships}>
        Save Relationships
    </button>
{/if}


<style>


    .relationship-error .relationship-cell {
        background-color: #ffcdcd;
    }

    .assessment-error {
        /*background-color: #ffcdcd;*/
    }


</style>