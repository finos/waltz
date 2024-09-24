<script>
    import _ from "lodash";
    import { measurableRatingStore } from "../../../svelte-stores/measurable-rating-store";
    import { displayError } from "../../../common/error-utils";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    export let primaryEntityRef;

    const multipleIncludes = (err = [], arr = []) => {
        for(let i = 0; i < arr.length; i++) {
            if(err.includes(arr[i])) {
                return true;
            }
        }

        return false;
    }

    const MODES = {
        EDIT: "EDIT",
        PREVIEW: "PREVIEW",
        RESULT: "RESULT",
        WAITING: "WAITING"
    };

    // Not required as of now
    const CHANGE_OPERATION = {
        ADD: "ADD",
        REMOVE: "REMOVE",
        RESTORE: "RESTORE",
        UPDATE: "UPDATE",
        NONE: "NONE",
    };

    const VALIDATION_ERROR = {
        MEASURABLE_NOT_FOUND: "MEASURABLE_NOT_FOUND",
        APPLICATION_NOT_FOUND: "APPLICATION_NOT_FOUND",
        RATING_NOT_FOUND: "RATING_NOT_FOUND",
        MEASURABLE_NOT_CONCRETE: "MEASURABLE_NOT_CONCRETE",
        RATING_NOT_USER_SELECTABLE: "RATING_NOT_USER_SELECTABLE",
    };

    const CHANGE_FIELD_TYPE = {
        PRIMARY: "PRIMARY",
        RATING: "RATING",
        COMMENT: "COMMENT",
    };

    let mode = MODES.EDIT;

    let uploadData = "";

    let previewResponse = null;
    let applyResponse = null;

    function onPreviewRows() {
        mode = MODES.WAITING;
        measurableRatingStore
            .bulkRatingPreviewByCategory(primaryEntityRef.id, uploadData)
            .then((res) => {
                previewResponse = res.data;
                mode = MODES.PREVIEW;
            })
            .catch((e) => {
                displayError("Could not preview rows", e);
            });
    }

    function onBulkApply() {
        mode = MODES.WAITING;
        measurableRatingStore
            .bulkRatingApplyByCategory(primaryEntityRef.id, uploadData)
            .then((res) => {
                applyResponse = res.data;
                mode = MODES.RESULT;
            })
            .catch((e) => {
                displayError("Could not apply rows", e);
            });
    }

    function onGoBack() {
        previewResponse = null;
        applyResponse = null;
        mode = MODES.EDIT;
    }

</script>

<div class="container-lg">
    {#if mode === MODES.WAITING}
        <LoadingPlaceholder/>
    {/if}
    {#if mode === MODES.EDIT}
        <div class="help-block">
            This will add measurable category ratings for applications
        </div>
        <details>
            <summary>Help <Icon name="circle-question"/></summary>
            <div class="help-block" style="margin-top: 0px;">
                Each row should reflect a assetCode, taxonomyExternalId, ratingCode, isPrimary, and comment
                combination. For example:
            </div>

            <pre>
assetCode	taxonomyExternalId	ratingCode	isPrimary	comment
EXAMPLE_CODE	EXAMPLE_ID	X	TRUE	This is an example
99999	99999	?	TRUE	This is another example
#99999	99999	X	TRUE	Lines prefixed by a '#' will be ignored</pre>
        </details>
        <form autocomplete="off" on:submit|preventDefault={onPreviewRows}>
            <fieldset>
                <div class="col">
                    <div>
                        <textarea
                            bind:value={uploadData}
                            placeholder="assetCode  taxonomyExternalId  ratingCode  isPrimary   comment"
                            rows="10"
                            cols="70"
                        ></textarea>
                    </div>
                </div>
            </fieldset>
            <button
                type="submit"
                class="btn btn-success"
                disabled={!uploadData}
            >
                Preview
            </button>
        </form>
    {/if}
    {#if mode === MODES.PREVIEW}
        {#if previewResponse.error }
                <div class="alert alert-danger">
                    There was a problem generating the preview:
                    <pre>{previewResponse.error.message}</pre>
                </div>
        {/if}
        {#if !previewResponse.error}
            <div class="help-block">
                The table below shows the preview of what will happen if you apply these changes:
            </div>
            <div class="waltz-scroll-region-300">
                <table id="preview-table" class="table table-condensed small" style="max-width: inherit;">
                    <thead>
                        <tr>
                            <th>Asset</th>
                            <th>Measurable</th>
                            <th>Rating</th>
                            <th>Primary</th>
                            <th>Comment</th>
                            <th>Error</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each previewResponse.validatedItems as obj}
                                <tr style="max-width: inherit;">
                                    <td
                                        class:cell-error={_.includes(
                                            obj.errors,
                                            VALIDATION_ERROR.APPLICATION_NOT_FOUND,
                                        )}
                                        class:positive-result={!_.includes(
                                            obj.errors,
                                            VALIDATION_ERROR.APPLICATION_NOT_FOUND
                                        )}>{ obj.application ? obj.application.name : obj.parsedItem.assetCode }</td
                                    >
                                    <td
                                        class:cell-error={multipleIncludes(
                                            obj.errors,
                                            [VALIDATION_ERROR.MEASURABLE_NOT_FOUND, VALIDATION_ERROR.MEASURABLE_NOT_CONCRETE],
                                        )}
                                        class:positive-result={!multipleIncludes(
                                            obj.errors,
                                            [VALIDATION_ERROR.MEASURABLE_NOT_FOUND, VALIDATION_ERROR.MEASURABLE_NOT_CONCRETE],
                                        )}>{ obj.measurable ? obj.measurable.name : obj.parsedItem.taxonomyExternalId }</td
                                    >
                                    <td
                                        class:positive-result={!multipleIncludes(
                                            obj.errors,
                                            [VALIDATION_ERROR.RATING_NOT_FOUND, VALIDATION_ERROR.RATING_NOT_USER_SELECTABLE],
                                        )}
                                        class:cell-error={multipleIncludes(
                                            obj.errors,
                                            [VALIDATION_ERROR.RATING_NOT_FOUND, VALIDATION_ERROR.RATING_NOT_USER_SELECTABLE],
                                        )}
                                    >
                                    { obj.ratingSchemeItem ? obj.ratingSchemeItem.name : obj.parsedItem.ratingCode }</td>
                                    <td>
                                        {obj.parsedItem.isPrimary}
                                    </td>
                                    <td>
                                        {obj.parsedItem.comment}
                                    </td>
                                    <td class:cell-error={obj.errors.length != 0}>
                                        <span>
                                            {#each obj.errors as err}
                                                {err}{" "}
                                            {/each}
                                        </span>
                                    </td>
                                </tr>
                        {/each}
                        </tbody>
                </table>
            </div>
        {/if}
        <form autocomplete="off" on:submit|preventDefault={onBulkApply}>
            <button class="btn btn-success"
                    disabled={ previewResponse.error }
                    type="submit">
                Apply
            </button>
            <button class="btn btn-skinny" on:click={onGoBack}>Back</button>
        </form>
    {/if}
    {#if mode === MODES.RESULT}
        <div class="alert alert-success">
            <p>Bulk Upload Successful!</p>
            <p>Records Added: {applyResponse.recordsAdded}</p>
            <p>Records Updated: {applyResponse.recordsUpdated}</p>
            <p>Records Removed: {applyResponse.recordsRemoved}</p>
            <p>Skipped Rows: {applyResponse.skippedRows}</p>
        </div>
        <button class="btn btn-skinny" on:click={onGoBack}>Back To Editor</button>
    {/if}
</div>

<style>
    .cell-error {
        background-color: #ffccd7;
    }

    .positive-result {
        background-color: #caf8ca;
    }
</style>
