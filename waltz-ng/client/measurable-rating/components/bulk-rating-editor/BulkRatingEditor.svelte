<script>
    import _ from "lodash";
    import { measurableRatingStore } from "../../../svelte-stores/measurable-rating-store";
    import { displayError } from "../../../common/error-utils";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import Tooltip from "../../../common/svelte/Tooltip.svelte";
    import DataCellErrorTooltip from "./DataCellErrorTooltip.svelte";


    export let primaryEntityRef;
    let canApply = false;

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
        ALLOCATION_EXCEEDING: "ALLOCATION_EXCEEDING",
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
                canApply = _.isNil(previewResponse.error) && _.every(previewResponse.validatedItems, d => _.isEmpty(d.errors));
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
                Each row should reflect a assetCode, taxonomyExternalId, ratingCode, isPrimary, allocation and comment
                combination. For example:
            </div>

            <pre>
assetCode	taxonomyExternalId	ratingCode	isPrimary	allocation	comment
EXAMPLE_CODE	EXAMPLE_ID	I	TRUE	100	This is an EXAMPLE_CODE
99999	99999	I	TRUE	100	This is another example
#99999	99999	X	TRUE	100	Lines prefixed by a '#' will be ignored</pre>
        </details>
        <form autocomplete="off" on:submit|preventDefault={onPreviewRows}>
            <fieldset>
                <div class="col">
                    <div>
                        <textarea
                            bind:value={uploadData}
                            placeholder="assetCode	taxonomyExternalId	ratingCode	isPrimary	allocation	comment"
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
                            <th>Action</th>
                            <th>Asset</th>
                            <th>Measurable</th>
                            <th>Rating</th>
                            <th>Primary</th>
                            <th>Allocation</th>
                            <th>Comment</th>
                            <th>Error</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each previewResponse.validatedItems as obj}
                                <tr style="max-width: inherit;">
                                    <td class={mkItemClass(obj)}>
                                        {mkOpLabel(obj)}
                                    </td>
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
                                        { obj.ratingSchemeItem ? obj.ratingSchemeItem.name : obj.parsedItem.ratingCode }
                                    </td>
                                    <td>
                                        {obj.parsedItem.isPrimary}
                                    </td>
                                    <td
                                        class:cell-error={_.includes(
                                            obj.errors,
                                            VALIDATION_ERROR.ALLOCATION_EXCEEDING,
                                        )}
                                        class:positive-result={!_.includes(
                                            obj.errors,
                                            VALIDATION_ERROR.ALLOCATION_EXCEEDING
                                        )}
                                    >
                                        { obj.parsedItem.allocation }
                                    </td>
                                    <td>
                                        {obj.parsedItem.comment}
                                    </td>
                                    <td>
                                        {#if obj.errors.length > 0}
                                            <Tooltip content={DataCellErrorTooltip}
                                                        props={{errors: obj.errors}}>
                                                <svelte:fragment slot="target">
                                                    <Icon name="exclamation-triangle" class="error-cell" color="red"/>
                                                </svelte:fragment>
                                            </Tooltip>
                                        {/if}
                                      </td>
                                </tr>
                        {/each}
                        </tbody>
                </table>
            </div>
        {/if}
        <form autocomplete="off" on:submit|preventDefault={onBulkApply}>
            <button class="btn btn-success"
                    disabled={!canApply}
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

    .positive-result {
        background-color: #caf8ca;
    }
</style>
