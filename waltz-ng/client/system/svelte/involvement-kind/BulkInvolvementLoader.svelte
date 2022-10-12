<script>

    import {entity} from "../../../common/services/enums/entity";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";
    import {
        rawInvolvements,
        selectedKind,
        resolvedRows,
        resolutionErrors,
        involvements
    } from "./bulk-involvement-loader-store";
    import _ from "lodash";
    import {mkRef} from "../../../common/entity-utils";
    import {bulkUploadStore} from "../../../svelte-stores/bulk-upload-store";
    import {displayError} from "../../../common/error-utils";
    import {bulkLoadResolutionStatus} from "../../../common/services/enums/bulk-load-resolution-status";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let involvementKind;

    let Modes = {
        INPUT: "INPUT",
        RESOLVE: "RESOLVE"
    }

    let activeMode = Modes.INPUT;
    let resolveCall;

    const items = [
        entity.APPLICATION,
        entity.CHANGE_INITIATIVE,
        entity.ORG_UNIT];

    function selectEntityKind(kind) {
        console.log({kind});
        $selectedKind = kind.key;
    }

    function verifyEntries() {
        console.log({re: $rawInvolvements});

        const resolveParams = {
            inputString: $rawInvolvements,
            targetDomain: mkRef(involvementKind.kind, involvementKind.id),
            rowSubjectKind: $selectedKind
        }

        return resolveCall = bulkUploadStore.resolve(resolveParams)
            .then(d => {
                $resolvedRows = d.data;
                [$resolutionErrors, $involvements] = _.partition(d.data, d => d.status === bulkLoadResolutionStatus.ERROR.key);
                console.log({d});
                console.log("done loading");
                activeMode = Modes.RESOLVE;
            })
            .catch(e => displayError("Could not resolve rows", e));
    }


    function saveNewInvolvements() {

        const uploadParams = {
            inputString: $rawInvolvements,
            targetDomain: mkRef(involvementKind.kind, involvementKind.id),
            rowSubjectKind: $selectedKind
        }

        bulkUploadStore.upload(uploadParams);
    }


    $: console.log({resolvedRows: $resolvedRows});


</script>

<DropdownPicker {items}
                onSelect={selectEntityKind}
                defaultMessage="Select an entity kind"/>


<div style="padding-top: 1em">
    Use the text box below to provide involvements as comma or tab separated value
    e.g. external identifier, email.
</div>

{#if activeMode === Modes.INPUT}
    <form on:submit|preventDefault={verifyEntries}>
        <div class="form-group">
            <label for="involvements">
                Involvements
            </label>
            <textarea id="involvements"
                      class="form-control"
                      rows="6"
                      placeholder="Please insert involvement external identifiers and email as comma or tab separated values split by newline or pipe characters"
                      bind:value={$rawInvolvements}></textarea>
        </div>
        <button type="submit"
                class="btn btn-success"
                disabled={_.isEmpty($rawInvolvements) || _.isNull($selectedKind)}>
            Search
        </button>
    </form>
{:else if activeMode === Modes.RESOLVE}
    {#if _.isEmpty($resolutionErrors)}
        <div style="padding: 1em 0">
            <span style="color: lightgreen">
                <Icon name="check"/>
            </span>
            All identifiers found, ready to save selection
        </div>
    {:else}
        <div style="padding: 1em 0">
            <span style="color: lightcoral">
                <Icon name="times"/>
            </span>
            There are {_.size($resolutionErrors)} errors found, please resolve before uploading
        </div>
    {/if}
    <table class="table table-condensed">
        <thead>
        <tr>
            <th>Entity Identifier</th>
            <th>Person Identifier</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        {#each $resolvedRows as row}
            <tr class:new={row.status === bulkLoadResolutionStatus.NEW.key}
                class:error={row.status === bulkLoadResolutionStatus.ERROR.key}>
                <td>{row.inputRow[0]}</td>
                <td>{row.inputRow[1]}</td>
                <td>{bulkLoadResolutionStatus[row.status].name}</td>
            </tr>
        {/each}
        </tbody>
    </table>
    <div>
        <button class="btn btn-success"
                disabled={!_.isEmpty($resolutionErrors)}
                on:click={saveNewInvolvements}>
            Save
        </button>
    </div>
{/if}

<style>

    .new {
        background-color: #ddffdd;
    }

    .error {
        background-color: #ffc9c9;
    }

</style>