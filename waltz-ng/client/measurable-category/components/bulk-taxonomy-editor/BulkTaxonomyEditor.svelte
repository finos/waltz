<script>
    import _ from "lodash";
    import {taxonomyManagementStore} from "../../../svelte-stores/taxonomy-management-store";
    import {displayError} from "../../../common/error-utils";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import {truncateMiddle} from "../../../common/string-utils";

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
        console.log(`Switching modes from ${mode} to ${newMode}`);
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
                canApply = _.every(previewData.validatedItems, d => _.isEmpty(d.errors));
                switchMode(Modes.PREVIEW);
            })
            .catch(e => displayError("Could not preview taxonomy changes", e));
    }

    function onApplyBulkChanges() {
        mode = Modes.LOADING;
        taxonomyManagementStore
            .bulkApply(primaryEntityRef, rawText)
            .then(r => {
                console.log("Success: ", r);
                applyData = r;
                switchMode(Modes.APPLY);
            })
            .catch(e => displayError("Could not apply taxonomy changes", e));
    }


    function mkItemClass(item) {
        if (! _.isEmpty(item.errors)) {
            return "op-error";
        }
        if (item.changeOperation === "ADD") {
            return "op-add";
        }
        if (item.changeOperation === "UPDATE") {
            return "op-update";
        }
    }

    function mkOpLabel(item) {
        if (! _.isEmpty(item.errors)) {
            return "Error";
        }
        if (item.changeOperation === "ADD") {
            return "Add";
        }
        if (item.changeOperation === "UPDATE") {
            return "Update";
        }
    }


    $: console.log({
        primaryEntityRef,
        sample,
        mode,
        uploadMode,
        rawText,
        previewData
    });



</script>

<div class="help-block">
    The bulk taxonomy editor can be used to upload multiple changes to this taxonomy.
</div>

{#if mode === Modes.EDIT}
    <details>
        <summary>Help</summary>
        <div class="help-block">
            The file format should look like:
        </div>
        <pre style="white-space: pre-line">
            externalId	 parentExternalId	 name	 description	 concrete
            a1		 A1	 Root node	 false
            a1.1	 a1	 A1_1	 First child	 true
            a1.2	 a1	 A1_2	 Second child	 true
        </pre>
        Note, column order is not important, but the headers are.
    </details>

    <form autocomplete="off"
          on:submit|preventDefault={onPreviewRows}>

        <fieldset>
            <label for="rawText">Raw Data</label>
            <textarea id="rawText"
                      class="form-control"
                      rows="8"
                      bind:value={rawText}></textarea>

            <div class="btn-group">
                <div class="radio">
                    <label>
                        <input style="display: inline-block;"
                               type="radio"
                               bind:group={uploadMode}
                               name="uploadMode"
                               value={UploadModes.ADD}>
                        Add Only
                    </label>
                    <div class="help-block">
                        Add either update existing items or add new ones.
                        It will <em>not</em> attempt to remove any items.
                    </div>
                </div>

                <div class="radio">
                    <label>
                        <input style="display: inline-block;"
                               type="radio"
                               bind:group={uploadMode}
                               name="uploadMode"
                               value={UploadModes.REPLACE}>
                        Replace
                    </label>
                    <p class="help-block">
                        Replace will remove all items not in the given raw data.
                        This should be used with <em>extreme</em> caution.
                    </p>
                </div>
            </div>

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
                <td class:cell-error={_.includes(item.errors, "PARENT_NOT_FOUND")}>
                    {item.parsedItem.parentExternalId}
                </td>
                <td>
                    {item.parsedItem.name}
                </td>
                <td>
                    {truncateMiddle(item.parsedItem.description)}
                </td>
                <td>
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


<style>
    textarea {
        width: 100%;
        font-family: monospace !important;
    }

    .op-add {
        background-color: #caf8ca;
    }

    .op-update {
        background-color: #fdfdbd;
    }

    .op-error {
        background-color: #ffccd7;
    }

    .cell-error {
        background-color: #ffccd7;
    }


</style>