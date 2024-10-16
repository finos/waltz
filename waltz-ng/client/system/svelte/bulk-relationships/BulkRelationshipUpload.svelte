<script>
    import { displayError } from "../../../common/error-utils";
	import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
	import SubSection from "../../../common/svelte/SubSection.svelte";
	import {entityRelationshipStore} from "../../../svelte-stores/entity-relationship-store.js";
	import _ from "lodash";

    export let selectedRelationshipKind;
    console.log(selectedRelationshipKind)

	const dummy_response = {
        "validatedItems": [
            {
                "parsedItem": {
                    "sourceExternalId": "INV71008",
                    "targetExternalId": "829-1",
                    "comment": "Comment"
                },
                "sourceEntityRef": {
                    "description": "The objective of project \"Banking Repricing\" is to introduce new competitive package-based current account products and increase the general revenue of current accounts.<br>\nFor this, a fundamental change of the underlying<br>\nIT platform is necessary to enable a highly customizable pricing and billing of services.<br>\n2018:<br>\n- Design future state of accounts and pricing of banking products in general (Business)<br>\n- Evaluate business requirements and onboard first product on pricing & billing platform",
                    "kind": "CHANGE_INITIATIVE",
                    "id": 8743518,
                    "name": "Banking Repricing (replaced by INV75686)",
                    "externalId": "INV71008",
                    "entityLifecycleStatus": "ACTIVE"
                },
                "targetEntityRef": {
                    "description": "Payments (ZV - Zahlungsverkehr) and Current Accounts (KK - Kontokorrent ); Real-time processing system for account administration, transaction processing, booking, output management and settlement of German current accounts for PBC (incl. Norisbank, dbEurope), CIB and PWM customers. \r\nDVAG :  a module for the provisioning of DVAG business. \r\nSystem: Mainframe, Midrange, eBranch. Apps usage type: account administration, transaction booking, account settlement, inhouse clearing, reporting. Zahlungsverkehr (payments) part is done within 22954-1 Domestic Payments DE.",
                    "kind": "APPLICATION",
                    "id": 19229,
                    "name": "ZVKK Current Account and Payment Mgmt",
                    "externalId": "829-1",
                    "entityLifecycleStatus": "ACTIVE"
                },
                "comment": "Comment",
                "error": ["SOURCE_NOT_FOUND"],
                "uploadOperation": "NONE"
            },
			{
                "parsedItem": {
                    "sourceExternalId": "INV71008",
                    "targetExternalId": "829-1",
                    "comment": "Comment"
                },
                "sourceEntityRef": {
                    "description": "The objective of project \"Banking Repricing\" is to introduce new competitive package-based current account products and increase the general revenue of current accounts.<br>\nFor this, a fundamental change of the underlying<br>\nIT platform is necessary to enable a highly customizable pricing and billing of services.<br>\n2018:<br>\n- Design future state of accounts and pricing of banking products in general (Business)<br>\n- Evaluate business requirements and onboard first product on pricing & billing platform",
                    "kind": "CHANGE_INITIATIVE",
                    "id": 8743518,
                    "name": "Banking Repricing (replaced by INV75686)",
                    "externalId": "INV71008",
                    "entityLifecycleStatus": "ACTIVE"
                },
                "targetEntityRef": {
                    "description": "Payments (ZV - Zahlungsverkehr) and Current Accounts (KK - Kontokorrent ); Real-time processing system for account administration, transaction processing, booking, output management and settlement of German current accounts for PBC (incl. Norisbank, dbEurope), CIB and PWM customers. \r\nDVAG :  a module for the provisioning of DVAG business. \r\nSystem: Mainframe, Midrange, eBranch. Apps usage type: account administration, transaction booking, account settlement, inhouse clearing, reporting. Zahlungsverkehr (payments) part is done within 22954-1 Domestic Payments DE.",
                    "kind": "APPLICATION",
                    "id": 19229,
                    "name": "ZVKK Current Account and Payment Mgmt",
                    "externalId": "829-1",
                    "entityLifecycleStatus": "ACTIVE"
                },
                "comment": "Comment",
                "error": [],
                "uploadOperation": "NONE"
            },
            {
                "parsedItem": {
                    "sourceExternalId": "PBS0202290",
                    "targetExternalId": "109235-1",
                    "comment": "Comment"
                },
                "sourceEntityRef": {
                    "description": "SatellitenmodellDSL-MA vor Ort im VC zur Verbesserung der Reichweite und Heben von Potentialen\r\nStärkung RepräsentanzmodellUmwandlung von DSL Repräsentanzen und BHW Repräsentanzen in einheitliches Modell\r\nStarpool vor OrtÜbernahme von Tätigkeiten durch Starpool\r\nAusbau DVAG-Kooperation",
                    "kind": "CHANGE_INITIATIVE",
                    "id": 601,
                    "name": "Dummy E_Ausbau Vertriebsoberfläche DSL",
                    "externalId": "PBS0202290",
                    "entityLifecycleStatus": "ACTIVE"
                },
                "targetEntityRef": {
                    "description": "Architecture tool to aggregate information from different source. To provide reporting and visualization across CT for applications, tech and data for both senior management and the architecture community.",
                    "kind": "APPLICATION",
                    "id": 20506,
                    "name": "Waltz",
                    "externalId": "109235-1",
                    "entityLifecycleStatus": "ACTIVE"
                },
                "comment": "Comment",
                "error": [],
                "uploadOperation": "ADD"
            }
        ],
        "parseError": null
  }

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

	async function dummyonBulkPreview() {
		mode = MODES.LOADING;
		previewResponse = dummy_response;
		mode = MODES.PREVIEW;
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
				<form autocomplete="off" on:submit|preventDefault={onBulkPreview}>
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
						<button class="btn btn-danger" on:click|preventDefault={() => onGoBack(MODES.EDIT)}>
							Back
						</button>
					{/if}
					{#if !previewResponse.parseError}
					<div class="help-block">
						The table below shows the preview of what will happen if you apply these changes:
					</div>
					<form autocomplete="off" on:submit={onBulkApply}>
						<div class="waltz-scroll-region-300">
							<table id="preview-table" class="table table-condensed table-striped" style="max-width: inherit;">
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
							<button class="btn btn-skinny" on:click|preventDefault={() => onGoBack(MODES.EDIT)}>Back</button>
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
				<button class="btn btn-skinny" on:click|preventDefault={() => onGoBack(MODES.EDIT)}>Back To Editor</button>
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