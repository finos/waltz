<script>
  import _ from "lodash";
  import {displayError} from "../../../common/error-utils";
  import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
  import {assessmentRatingStore} from "../../../svelte-stores/assessment-rating-store";
  import {truncateMiddle} from "../../../common/string-utils";
  import Icon from "../../../common/svelte/Icon.svelte";
  import Tooltip from "../../../common/svelte/Tooltip.svelte";
  import DataCellErrorTooltip from "./DataCellErrorTooltip.svelte";

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

  let mode = Modes.EDIT;
  let uploadMode = Modes.ADD_ONLY;
  let rawText = "";
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
      assessmentRatingStore
          .bulkPreview(primaryEntityRef, rawText)
          .then(r => {
              previewData = r.data;
              canApply = _.isNil(previewData.error) && _.every(previewData.validatedItems, d => _.isEmpty(d.errors));
              switchMode(Modes.PREVIEW);
          })
          .catch(e => {
            displayError("Could not preview assessment rating changes", e);
            switchMode(Modes.PREVIEW);
          });
  }

  function onApplyBulkChanges() {
      mode = Modes.LOADING;
      assessmentRatingStore
          .bulkApply(primaryEntityRef, rawText)
          .then(r => {
              applyData = r.data;
              switchMode(Modes.APPLY);
          })
          .catch(e => {
            displayError("Could not apply assessment rating changes", e);
            switchMode(Modes.PREVIEW);
          });
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

<div class="help-block">
  The bulk assessment rating editor can be used to upload multiple ratings to this assessment defination.
</div>

{#if mode === Modes.EDIT}
  <details>
      <summary>Help <Icon name="circle-question"/></summary>
      <div class="help-block">
          <dl>
              <dt>External Id</dt>
              <dd>This uniquely identifies the item within the entity. It should not be changed after it is set.</dd>

              <dt>Rating Code</dt>
              <dd>This defines the rating code.</dd>

              <dt>Is ReadOnly</dt>
              <dd>This defines the item is read only.</dd>

              <dt>Comment</dt>
              <dd>Short description</dd>

          </dl>
          For example:
      </div>
      <pre>externalID	ratingCode	isReadOnly	comment
INV0737	L	TRUE	description
      </pre>
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
                  <th>Rating</th>
                  <th>Read Only</th>
                  <th>Comments</th>
                  <th>Errors</th>
              </tr>
              </thead>
              <tbody>
              {#each previewData.validatedItems as item}
              <tr>
                  <td class={mkItemClass(item)}>
                      {mkOpLabel(item)}
                  </td>
                  <td class:cell-error={_.includes(item.errors, "ENTITY_NOT_FOUND")}
                      class:cell-update={_.includes(item.changedFields, "ENTITY_KIND") && !_.includes(item.errors, "ENTITY_NOT_FOUND")}>
                      {item.parsedItem.externalId}
                  </td>
                  <td class:cell-error={_.includes(item.errors, "RATING_NOT_FOUND", "RATING_NOT_USER_SELECTABLE")}
                      class:cell-update={_.includes(item.changedFields, "RATING") && !_.includes(item.errors, "RATING_NOT_FOUND", "RATING_NOT_USER_SELECTABLE")}>
                      {item.parsedItem.ratingCode}
                  </td>
                  <td class:cell-update={_.includes(item.changedFields, "READ_ONLY")}>
                      {truncateMiddle(item.parsedItem.isReadOnly)}
                  </td>
                  <td class:cell-update={_.includes(item.changedFields, "COMMENTS")}>
                      {item.parsedItem.comment}
                  </td>
                  <td>
                    {#if item.errors.length > 0}
                        <Tooltip content={DataCellErrorTooltip}
                                    props={{errors: item.errors}}>
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
          <td>Skipped Records</td>
          <td class:positive-result={applyData.skippedRows > 0}>
              {applyData.skippedRows}
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

  .error-cell {
    color: red;
  }
</style>