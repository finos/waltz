<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import {attestationInstanceStore} from "../../../svelte-stores/attestation-instance-store";
    import toasts from "../../../svelte-stores/toast-store";
    import {surveyInstanceStore} from "../../../svelte-stores/survey-instance-store";

    let attestationReassignmentsCall = attestationInstanceStore.getCountsOfRecipientsToReassign();
    $: attestationReassignmentCounts = $attestationReassignmentsCall.data;


    let surveyReassignmentsCall = surveyInstanceStore.getReassignRecipientsCounts();
    $: surveyReassignmentCounts = $surveyReassignmentsCall.data;

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
                console.log({r});
                const counts = r.data;
                toasts.success(`Successfully reassigned recipients. Recipients created: ${counts?.recipientsCreatedCount}, Recipients removed: ${counts?.recipientsRemovedCount}`);
            })
            .then(() => surveyReassignmentsCall = surveyInstanceStore.getReassignRecipientsCounts(true))
            .catch(e => toasts.error("Could not reassign recipients: " + e.error));
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
                    <td>Attestations</td>
                    <td>{attestationReassignmentCounts?.recipientsCreatedCount}</td>
                    <td>{attestationReassignmentCounts?.recipientsRemovedCount}</td>
                </tr>
                <tr>
                    <td>Surveys</td>
                    <td>{surveyReassignmentCounts?.recipientsCreatedCount}</td>
                    <td>{surveyReassignmentCounts?.recipientsRemovedCount}</td>
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
                This will remove all recipients for surveys instances which are 'In Progress', 'Not Started' or
                'Rejected' where the person is
                no longer active and no longer required according to involvement kind ids specified on the run.
                New recipients will be assigned to any survey instances if they currently
                have the required involvement kind.
            </div>
            <button class="btn btn-info"
                    on:click={() => reassignSurveyRecipients()}>
                Reassign recipients
            </button>
        </div>
    </div>
</div>
