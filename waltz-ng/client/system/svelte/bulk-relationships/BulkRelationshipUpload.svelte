<script>
    import { displayError } from "../../../common/error-utils";
	import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
	import SubSection from "../../../common/svelte/SubSection.svelte";
	import {entityRelationshipStore} from "../../../svelte-stores/entity-relationship-store.js";
	import _ from "lodash";

    export let selectedRelationshipKind;

	const MODES = {
		EDIT: "EDIT",
		PREVIEW: "PREVIEW",
		LOADING: "LOADING",
		RESULT: "RESULT"
	}

	const uploadOperation = {
		ADD: "ADD",
		NONE: "NONE",
		UPDATE: "UPDATE"
	}

	const VALIDATION_ERRORS = {
		SOURCE_NOT_FOUND: "SOURCE_NOT_FOUND",
		TARGET_NOT_FOUND: "TARGET_NOT_FOUND"
	}

	let mode = MODES.EDIT;
    let rawText = "";
	let previewResponse = null;
	let applyResponse = null;

    function onBulkPreview() {
		mode = MODES.LOADING;
		entityRelationshipStore
			.bulkUploadRelationshipsPreview(selectedRelationshipKind.id, rawText)
			.then((res) => {
				previewResponse = res.data;
				mode = MODES.PREVIEW;
			})
			.catch((e) => {
				displayError("Could not preview rows", e);
				onGoBack(MODES.EDIT);
			});
    }

	function onBulkApply() {
		mode = MODES.LOADING;
		entityRelationshipStore
			.bulkUploadRelationshipsApply(selectedRelationshipKind.id, rawText)
			.then((res) => {
				applyResponse = res.data;
				mode = MODES.RESULT;
			})
			.catch((e) => {
				displayError("Could not apply changes", e);
				onGoBack(MODES.PREVIEW);
			})
	}

	function onGoBack(givenMode = MODES.EDIT) {
		if(givenMode === MODES.EDIT) {
			// reset any responses recieved from the server -> start afresh
			previewResponse = null;
			applyResponse = null;
		}

		if(givenMode === MODES.PREVIEW) {
			// on an error while applying changes go back to the preview and reset applyResponse
			applyResponse = null;
		}

		// set mode to given mode
		mode = givenMode;
	}
</script>

<div style="margin-top: 2rem;">
    
		<SubSection>
			<div slot="header">
				Bulk Upload Relationships
			</div>
			<div slot="content">
				{#if mode === MODES.EDIT}
				<div class="help-block">
					The bulk relationship editor can be used to upload multiple changes to the selected relationship.
				</div>
				<details>
					<summary>Help</summary>
					<div class="help-block">
						<p>The bulk upload format should look like, column order is not important but the headers are:</p>
						<dl>
							<dt>Source External Id</dt>
							<dd>This uniquely identifies the source (from) entity for that relationship.</dd>
			
							<dt>Target External Id</dt>
							<dd>This uniquely identifies the target (to) entity for that relationship.</dd>
			
							<dt>Description</dt>
							<dd>This optionally adds a comment to the change.</dd>
						</dl>
						For Example:
					</div>
					<pre>
sourceExternalId	 targetExternalId	 description
12312	22312	This is a description
#2234	357325	This line will be ignored
66723	172452-1313-2424	This is another example
12233	8242	This is another example</pre>
				</details>
				<form autocomplete="off" 
				on:submit|preventDefault={onBulkPreview}>
					<textarea 
					style="width: 100%;" 
					bind:value={rawText}
					rows=10></textarea>
					<button class="btn btn-success" 
							type="submit" 
							disabled={!rawText}>Submit</button>
				</form>
				{/if}
				{#if mode === MODES.LOADING}
					<LoadingPlaceholder/>
				{/if}
				{#if mode === MODES.PREVIEW}
					{#if previewResponse.parseError}
						<div class="alert alert-danger">
							There was a problem generating the preview:
							<pre>{previewResponse.parseError.message}</pre>
						</div>
						<button class="btn btn-danger" 
								on:click|preventDefault={() => onGoBack(MODES.EDIT)}>
							Back
						</button>
					{/if}
					{#if !previewResponse.parseError}
					<div class="help-block">
						The table below shows the preview of what will happen if you apply these changes:
					</div>
					<form autocomplete="off" on:submit={onBulkApply}>
						<div class="waltz-scroll-region-300">
							<table id="preview-table" 
									class="table table-condensed table-striped" 
									style="max-width: inherit;">
								<thead>
									<tr>
										<th>Source Entity</th>
										<th>Target Entity</th>
										<th>Description</th>
										<th>Operation</th>
										<th>Error</th>
									</tr>
								</thead>
								<tbody>
										{#each previewResponse.validatedItems as obj }
											<tr style="max-width: inherit;">
												<td class:cell-error={_.includes(
													obj.error,
													VALIDATION_ERRORS.SOURCE_NOT_FOUND)}>{obj.sourceEntityRef ? obj.sourceEntityRef.name : obj.parsedItem.sourceExternalId}</td>
												<td class:cell-error={_.includes(
													obj.error,
													VALIDATION_ERRORS.TARGET_NOT_FOUND)}>{obj.targetEntityRef? obj.targetEntityRef.name : obj.parsedItem.targetExternalId}</td>
												<td>{obj.description}</td>
												<td class:positive-result={
													obj.uploadOperation == uploadOperation.ADD}
													class:cell-update={
													obj.uploadOperation == uploadOperation.UPDATE}>{obj.uploadOperation}</td>
												<td class:cell-error={obj.error.length != 0}>{obj.error.length != 0? obj.error : ""}</td>
											</tr>
										{/each}
								</tbody>
							</table>
						</div>
						<br>
						<span>
							<button class="btn btn-success"
									disabled={ previewResponse.parseError }
									type="submit">Apply
							</button>
							<button class="btn btn-skinny" 
									on:click|preventDefault={() => onGoBack(MODES.EDIT)}>Back
							</button>
						</span>
					</form>
					{/if}
				{/if}
				{#if mode === MODES.RESULT}
				<div class="help-block">
					The table below shows the result of your applied changes:
				</div>
				<table class="table table-condensed table-striped">
					<tbody>
						<tr>
							<td>Added Records</td>
							<td class:positive-result={applyResponse.recordsAdded > 0}>
								{applyResponse.recordsAdded}
							</td>
						</tr>
						<tr>
							<td>Updated Records</td>
							<td class:positive-result={applyResponse.recordsUpdated > 0}>
								{applyResponse.recordsUpdated}
							</td>
						</tr>
						<tr>
							<td>Skipped Records</td>
							<td class:positive-result={applyResponse.skippedRows > 0}>
								{applyResponse.skippedRows}
							</td>
						</tr>
					</tbody>
				</table>
				<button class="btn btn-skinny" 
				on:click|preventDefault={() => onGoBack(MODES.EDIT)}>Back To Editor
				</button>
				{/if}
			</div>
		</SubSection>
		
    
</div>

<style>
    .cell-error {
        background-color: #ffccd7;
    }

    .positive-result {
        background-color: #caf8ca;
		font-style: italic;
    }
	.cell-update {
        background-color: #fdfdbd;
        font-style: italic;
    }

</style>