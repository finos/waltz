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
        UploadModes,
        uploadMode
    } from "./bulk-upload-relationships-store";
    import _ from "lodash";

    export let relationshipKind;

    let resolveCall;

    function verifyEntries() {

        console.log({is: $inputString, rel: relationshipKind, mode: $uploadMode});

        const resolveParams = {
            inputString: $inputString,
            legalEntityRelationshipKindId: relationshipKind.id,
            uploadMode: $uploadMode
        };

        return resolveCall = bulkUploadLegalEntityRelationshipStore.resolve(resolveParams)
            .then(d => {
                console.log({d});
                $activeMode = Modes.RESOLVED;
                $resolvedRows = d.data.resolvedRows;
            })
            .catch(e => displayError("Could not resolve rows", e));
    }


    $: console.log({resolved: $resolvedRows});

</script>

<h4>svelte here</h4>

{#if $activeMode = Modes.INPUT}
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
{:else if $activeMode = Modes.LOADING}
    <LoadingPlaceholder>
        Resolving rows...
    </LoadingPlaceholder>
{:else if $activeMode = Modes.RESOLVED}
    <h4>Has this worked?</h4>
{/if}