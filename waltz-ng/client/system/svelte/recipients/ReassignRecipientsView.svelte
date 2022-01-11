<script>

import ViewLink from "../../../common/svelte/ViewLink.svelte";
import PageHeader from "../../../common/svelte/PageHeader.svelte";
import {attestationInstanceStore} from "../../../svelte-stores/attestation-instance-store";

function reassignRecipients() {
    attestationInstanceStore.reassignRecipients()
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
            <h4>Attestations</h4>
            <div class="small help-block">
                This will remove all recipients for attestations which are incomplete that
                were originally assigned via an involvement which they no longer hold.
                New recipients will be assigned to the attestation if they currently
                have the required involvement kind.
            </div>
            <button class="btn btn-info"
                    on:click={() => reassignRecipients()}>
                Reassign recipients
            </button>
        </div>
    </div>
</div>
