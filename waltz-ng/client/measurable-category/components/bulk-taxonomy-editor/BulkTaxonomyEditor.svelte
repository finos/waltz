<script>
    import _ from "lodash";
    import {taxonomyManagementStore} from "../../../svelte-stores/taxonomy-management-store";
    import {displayError} from "../../../common/error-utils";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import {truncateMiddle} from "../../../common/string-utils";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let primaryEntityRef;

    const Modes = {
        EDIT: "EDIT",
        LOADING: "LOADING",
        PREVIEW: "PREVIEW",
        APPLY: "APPLY"
    };

    const UploadModes = {
        ADD_ONLY: "ADD",
        REPLACE: "REPLACE"
    };

    const sample =
`externalId\t parentExternalId\t name\t description\t concrete
a1\t\t A1\t Root node\t false
a1.1\t a1\t A1_1\t First child\t true
a1.2\t a1\t A1_2\t Second child\t true`

    let mode = Modes.EDIT;
    let uploadMode = Modes.ADD_ONLY;
    let rawText = sample;
    let canApply = false;
    let previewData = null;
    let applyData = null;


    function switchMode(newMode) {
        mode = newMode;
        switch (newMode) {
            case Modes.EDIT:
                previewData = null;
                canApply = false;
                break;
        }
    }


    function onPreviewRows() {
        mode = Modes.LOADING;
        taxonomyManagementStore
            .bulkPreview(primaryEntityRef, rawText)
            .then(r => {
                previewData = r.data;
                canApply = _.isNil(previewData.error) && _.every(previewData.validatedItems, d => _.isEmpty(d.errors));
                switchMode(Modes.PREVIEW);
            })
            .catch(e => displayError("Could not preview taxonomy changes", e));
    }

    function onApplyBulkChanges() {
        mode = Modes.LOADING;
        taxonomyManagementStore
            .bulkApply(primaryEntityRef, rawText)
            .then(r => {
                applyData = r.data;
                switchMode(Modes.APPLY);
            })
            .catch(e => displayError("Could not apply taxonomy changes", e));
    }


    function mkItemClass(item) {
        if (! _.isEmpty(item.errors)) {
            return "op-error";
        }
        switch (item.changeOperation) {
            case "ADD":
                return "op-add";
            case "UPDATE":
                return "op-update";
            case "NONE":
                return "op-none";
            case "RESTORE":
                return "op-restore";
            default:
                return "op-error";
        }
    }

    function mkOpLabel(item) {
        if (! _.isEmpty(item.errors)) {
            return "Error";
        }
        switch (item.changeOperation) {
            case "ADD":
                return "Add";
            case "UPDATE":
                return "Update";
            case "NONE":
                return "None";
            case "RESTORE":
                return "Restore";
            default:
                return "??" + item.changeOperation + "??";
        }
    }

    $: console.log({
        previewData,
        applyData
    })

</script>

<div class="help-block">
    The bulk taxonomy editor can be used to upload multiple changes to this taxonomy.
</div>

{#if mode === Modes.EDIT}
    <details>
        <summary>Help <Icon name="circle-question"/></summary>
        <div class="help-block">
            The bulk change format should look like, column order is not important but the headers are:
            <dl>
                <dt>External Id</dt>
                <dd>This uniquely identifies the item within the category. It should not be changed after it is set.</dd>

                <dt>ParentExternalId</dt>
                <dd>This optionally defines the external id of the parent. If this is changed, the item will be moved accordingly.</dd>

                <dt>Name</dt>
                <dd>Short descriptive name of the item</dd>

                <dt>Description</dt>
                <dd>Short description</dd>

                <dt>Concrete</dt>
                <dd>This determines if the node can be used in mappings against applications. If set to false, it implies the node is abstract and cannot be used in mappings.</dd>
            </dl>
            For example:
        </div>
        <pre style="white-space: pre-line">
            externalId	 parentExternalId	 name	 description	 concrete
            a1		 A1	 Root node	 false
            a1.1	 a1	 A1_1	 First child	 true
            a1.2	 a1	 A1_2	 Second child	 true
        </pre>
        Note, removal of items should be done via the Interactive Taxonomy Editor panel.


    </details>

    <form autocomplete="off"
          on:submit|preventDefault={onPreviewRows}>

        <fieldset>
            <label for="rawText">Raw Data</label>
            <textarea id="rawText"
                      class="form-control"
                      rows="8"
                      bind:value={rawText}></textarea>

            <button type="submit"
                    class="btn btn-success"
                    disabled={!rawText}>
                Preview
            </button>

        </fieldset>

    </form>
{/if}

{#if mode === Modes.LOADING}
    <LoadingPlaceholder/>
{/if}

{#if mode === Modes.PREVIEW}
    {#if !_.isNil(previewData.error)}
        <div class="alert alert-danger">
            <em>
                Could not parse the data, see error message below.
            </em>
            <div style="padding-top: 0.5em"> {previewData.error.message} </div>
        </div>
    {/if}
    {#if _.isNil(previewData.error)}
        <div class="preview-table small">
            <table class="table table-condensed table-striped table">
                <thead>
                <tr>
                    <th>Action</th>
                    <th>External Id</th>
                    <th>Parent External Id</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Concrete</th>
                    <th>Errors</th>
                </tr>
                </thead>
                <tbody>
                {#each previewData.validatedItems as item}
                <tr>
                    <td class={mkItemClass(item)}>
                        {mkOpLabel(item)}
                    </td>
                    <td class:cell-error={_.includes(item.errors, "DUPLICATE_EXT_ID")}>
                        {item.parsedItem.externalId}
                    </td>
                    <td class:cell-error={_.includes(item.errors, "PARENT_NOT_FOUND")}
                        class:cell-update={_.includes(item.changedFields, "PARENT_EXTERNAL_ID") && !_.includes(item.errors, "PARENT_NOT_FOUND")}>
                        {item.parsedItem.parentExternalId}
                    </td>
                    <td class:cell-update={_.includes(item.changedFields, "NAME")}>
                        {item.parsedItem.name}
                    </td>
                    <td class:cell-update={_.includes(item.changedFields, "DESCRIPTION")}>
                        {truncateMiddle(item.parsedItem.description)}
                    </td>
                    <td class:cell-update={_.includes(item.changedFields, "CONCRETE")}>
                        {item.parsedItem.concrete}
                    </td>
                    <td>
                        {item.errors}
                    </td>
                </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {/if}
    <button class="btn-skinny"
            on:click={() => switchMode(Modes.EDIT)}>
        Back
    </button>

    <button class="btn btn-success"
            disabled={!canApply}
            on:click={() => onApplyBulkChanges()}>
        Apply
    </button>
{/if}

{#if mode === Modes.APPLY}
    <table class="table table-condensed table-striped">
        <tbody>
        <tr>
            <td>Added Records</td>
            <td class:positive-result={applyData.recordsAdded > 0}>
                {applyData.recordsAdded}
            </td>
        </tr>
        <tr>
            <td>Updated Records</td>
            <td class:positive-result={applyData.recordsUpdated > 0}>
                {applyData.recordsUpdated}
            </td>
        </tr>
        <tr>
            <td>Removed Records</td>
            <td class:positive-result={applyData.recordsRemoved > 0}>
                {applyData.recordsRemoved}
            </td>
        </tr>
        <tr>
            <td>Restored Records</td>
            <td class:positive-result={applyData.recordsRestored > 0}>
                {applyData.recordsRestored}
            </td>
        </tr>
        <tr>
            <td>Hierarchy Rebuilt</td>
            <td class:positive-result={applyData.hierarchyRebuilt}>
                {applyData.hierarchyRebuilt}
            </td>
        </tr>
        </tbody>
    </table>

    {#if applyData.hierarchyRebuilt}
        <p class="alert alert-warning">
            Please note: This change has altered the hierarchy, you will need to reload this page.
        </p>
    {/if}

    <button class="btn btn-skinny"
        on:click={() => switchMode(Modes.EDIT)}>
        Back to Bulk Editor
    </button>

{/if}

<style>
    textarea {
        width: 100%;
        font-family: monospace !important;
    }

    dt {
        margin-top: 0.3em;
    }
    .op-add {
        background-color: #caf8ca;
    }

    .op-update {
        background-color: #fdfdbd;
    }

    .op-none {
        background-color: #f3f3f3;
    }

    .op-restore {
        background-color: #dcf7fc;
    }

    .op-error {
        background-color: #ffccd7;
    }

    .cell-error {
        background-color: #ffccd7;
    }

    .cell-update {
        background-color: #fdfdbd;
        font-style: italic;
    }

    .positive-result {
        background-color: #caf8ca;
    }


</style>