<script>

    import {entity} from "../../../common/services/enums/entity";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";
    import {
        involvements,
        rawInvolvements,
        resolutionErrors,
        resolvedRows,
        selectedKind,
        uploadMode,
        UploadModes
    } from "./bulk-involvement-loader-store";
    import _ from "lodash";
    import {mkRef} from "../../../common/entity-utils";
    import {bulkUploadStore} from "../../../svelte-stores/bulk-upload-store";
    import {displayError} from "../../../common/error-utils";
    import {bulkLoadResolutionStatus} from "../../../common/services/enums/bulk-load-resolution-status";
    import Icon from "../../../common/svelte/Icon.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import Tooltip from "../../../common/svelte/Tooltip.svelte";
    import LoaderErrorTooltipContent from "./LoaderErrorTooltipContent.svelte";

    export let involvementKind;
    export let onSave;

    let errorsIgnored = false;

    let Modes = {
        INPUT: "INPUT",
        RESOLVE: "RESOLVE"
    };

    let activeMode = Modes.INPUT;
    let resolveCall;

    const items = [
        entity.APPLICATION,
        entity.CHANGE_INITIATIVE,
        entity.ORG_UNIT
    ];

    function selectEntityKind(kind) {
        $selectedKind = kind.key;
    }

    function verifyEntries() {

        const resolveParams = {
            inputString: $rawInvolvements,
            targetDomain: mkRef(involvementKind.kind, involvementKind.id),
            rowSubjectKind: $selectedKind
        };

        return resolveCall = bulkUploadStore.resolve(resolveParams)
            .then(d => {
                $resolvedRows = d.data;
                [$resolutionErrors, $involvements] = _.partition(d.data, d => d.status === bulkLoadResolutionStatus.ERROR.key);
                activeMode = Modes.RESOLVE;
            })
            .catch(e => displayError("Could not resolve rows", e));
    }


    function saveInvolvements() {

        const validInvs = _
            .chain($involvements)
            .filter(d => d.status === bulkLoadResolutionStatus.NEW.key)
            .map(i => _.join(i.inputRow, ","))
            .value();

        toasts.info(`Saving ${_.size(validInvs)} involvements...`);

        const uploadParams = {
            inputString: _.join(validInvs, "\n"),
            targetDomain: mkRef(involvementKind.kind, involvementKind.id),
            rowSubjectKind: $selectedKind,
            uploadMode: $uploadMode
        };

        bulkUploadStore.upload(uploadParams)
            .then(r => {
                toasts.success(`Successfully created ${r.data} new involvements`);
                return onSave();
            })
            .catch(e => displayError("Could not bulk store involvements", e));
    }

    function acceptErrors() {
        errorsIgnored = true;
        toasts.info("This set of involvements can now be saved. Any errors will be ignored.")
    }

</script>


{#if activeMode === Modes.INPUT}
    <h4>Upload Involvements:</h4>
    <div style="padding: 1em 0">
        Select the entity kind you wish to load involvements for from the dropdown picker.
    </div>
    <DropdownPicker {items}
                    onSelect={selectEntityKind}
                    defaultMessage="Select an entity kind"/>
    <div style="padding: 3em 0 1em 0">
        Use the text box below to provide involvements as comma or tab separated values
        e.g. external identifier, email.
    </div>
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
                disabled={_.isEmpty($rawInvolvements) || _.isNull($selectedKind)}>
            Search
        </button>
    </form>
{:else if activeMode === Modes.RESOLVE}
    <h4>Upload Summary:</h4>

    <div class:waltz-scroll-region-350={_.size($resolvedRows) > 10}>
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
                    <td>
                        {bulkLoadResolutionStatus[row.status].name}
                        {#if row.status === bulkLoadResolutionStatus.ERROR.key}
                            <Tooltip content={LoaderErrorTooltipContent}
                                     props={{resolvedRow: row}}>
                                <svelte:fragment slot="target">
                                    <Icon name="exclamation-triangle"/>
                                </svelte:fragment>
                            </Tooltip>
                        {/if}
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>

    {#if _.isEmpty($resolutionErrors)}
        <div style="padding: 1em 0">
            <span style="color: lightgreen">
                <Icon name="check"/>
            </span>
            All identifiers found, ready to save {_.size($involvements)} involvements
        </div>
    {:else}
        <div style="padding: 1em 0">
            <span style="color: lightcoral">
                <Icon name="times"/>
            </span>
            There are {_.size($resolutionErrors)} errors found, please
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.INPUT}>
                edit the data
            </button>
            or
            <button class="btn btn-skinny"
                    on:click={() => acceptErrors()}>
                save ignoring errors
            </button>
        </div>
    {/if}

    <div>
        <div class="form-group">
            <label>
                <input style="display: inline-block;"
                       disabled={true}
                       type="radio"
                       bind:group={$uploadMode}
                       name="uploadMode"
                       value={UploadModes.ADD_ONLY}>
                Add Only
            </label>

            <label>
                <input style="display: inline-block;"
                       disabled={true}
                       type="radio"
                       bind:group={$uploadMode}
                       name="uploadMode"
                       value={UploadModes.REPLACE}>
                Replace
            </label>
        </div>
        {#if $uploadMode === UploadModes.REPLACE}
            <div>
                <span style="color: orange">
                    <Icon name="exclamation-triangle"/>
                </span>
                This will remove any involvements for this entity kind not listed above
            </div>
        {/if}
    </div>
    <div>
        <button class="btn btn-success"
                disabled={!_.isEmpty($resolutionErrors) && !errorsIgnored}
                on:click={saveInvolvements}>
            Save
        </button>
        <button class="btn btn-default"
                on:click={() => activeMode = Modes.INPUT}>
            Edit
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