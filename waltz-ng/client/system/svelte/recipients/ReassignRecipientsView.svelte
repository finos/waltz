<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import {attestationInstanceStore} from "../../../svelte-stores/attestation-instance-store";
    import toasts from "../../../svelte-stores/toast-store";
    import {surveyInstanceStore} from "../../../svelte-stores/survey-instance-store";

    let attestationReassignmentsCall = attestationInstanceStore.getCountsOfRecipientsToReassign();
    $: attestationReassignmentCounts = $attestationReassignmentsCall.data;

    let surveyRecipientReassignmentsCall = surveyInstanceStore.getReassignRecipientsCounts();
    $: surveyRecipientReassignmentCounts = $surveyRecipientReassignmentsCall.data;

    let surveyOwnerReassignmentsCall = surveyInstanceStore.getReassignOwnersCounts();
    $: surveyOwnerReassignmentCounts = $surveyOwnerReassignmentsCall.data;

    function reassignAttestationRecipients() {
        let reassignPromise = attestationInstanceStore.reassignRecipients();

        reassignPromise
            .then(r => {
                const counts = r.data;
                toasts.success(`Successfully reassigned recipients. Recipients created: ${counts?.recipientsCreatedCount}, Recipients removed: ${counts?.recipientsRemovedCount}`);
            })
            .then(r => attestationReassignmentsCall = attestationInstanceStore.getCountsOfRecipientsToReassign(true))
            .catch(e => toasts.error("Could not reassign recipients: " + e.error));
    }


    function reassignSurveyRecipients() {
        let reassignPromise = surveyInstanceStore.reassignRecipients();

        reassignPromise
            .then(r => {
                const counts = r.data;
                toasts.success(`Successfully reassigned recipients. Recipients created: ${counts?.recipientsCreatedCount}, Recipients removed: ${counts?.recipientsRemovedCount}`);
            })
            .then(() => surveyRecipientReassignmentsCall = surveyInstanceStore.getReassignRecipientsCounts(true))
            .catch(e => toasts.error("Could not reassign recipients: " + e.error));
    }

    function reassignSurveyOwners() {
        let reassignPromise = surveyInstanceStore.reassignOwners();

        reassignPromise
            .then(r => {
                const counts = r.data;
                toasts.success(`Successfully reassigned owners. Owners created: ${counts?.recipientsCreatedCount}, Owners removed: ${counts?.recipientsRemovedCount}`);
            })
            .then(() => surveyOwnerReassignmentsCall = surveyInstanceStore.getReassignOwnersCounts(true))
            .catch(e => toasts.error("Could not reassign owners: " + e.error));
    }

</script>


<PageHeader icon="user"
            name="Reassign Recipients">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Reassign Recipients</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <table class="table table-condensed">
                <thead>
                    <tr>
                        <th>Entity</th>
                        <th title="People who have an involvement for an entity with an open attestation but who are not assigned to the attestation">
                            Missing Recipients
                        </th>
                        <th title="Attestation recipients who have since lost the involvement for that entity or are no longer active">
                            Invalid Recipients
                        </th>
                    </tr>
                </thead>
                <tbody>
                <tr>
                    <td>Attestation Recipients</td>
                    <td>{attestationReassignmentCounts?.recipientsCreatedCount}</td>
                    <td>{attestationReassignmentCounts?.recipientsRemovedCount}</td>
                </tr>
                <tr>
                    <td>Survey Recipients</td>
                    <td>{surveyRecipientReassignmentCounts?.recipientsCreatedCount}</td>
                    <td>{surveyRecipientReassignmentCounts?.recipientsRemovedCount}</td>
                </tr>
                <tr>
                    <td>Surveys Owners</td>
                    <td>{surveyOwnerReassignmentCounts?.recipientsCreatedCount}</td>
                    <td>{surveyOwnerReassignmentCounts?.recipientsRemovedCount}</td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <h4>Attestations</h4>
            <div class="small help-block">
                This will remove all recipients for attestations which are incomplete that
                were originally assigned via an involvement which they no longer hold.
                New recipients will be assigned to the attestation if they currently
                have the required involvement kind.
            </div>
            <button class="btn btn-info"
                    on:click={() => reassignAttestationRecipients()}>
                Reassign recipients
            </button>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <h4>Surveys</h4>
            <div class="small help-block">
                This will remove all recipients / owners for surveys instances which are 'In Progress', 'Not Started' or
                'Rejected' where the person is
                no longer active and no longer required according to involvement kind ids specified on the run.
                New recipients / owners will be assigned to any survey instances if they currently
                have the required involvement kind.
            </div>
            <button class="btn btn-info"
                    on:click={() => reassignSurveyRecipients()}>
                Reassign recipients
            </button>
            <button class="btn btn-info"
                    on:click={() => reassignSurveyOwners()}>
                Reassign owners
            </button>
        </div>
    </div>
</div>
