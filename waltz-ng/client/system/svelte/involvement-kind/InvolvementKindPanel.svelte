<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import InvolvementListPanel from "./InvolvementListPanel.svelte";
    import InvolvementKindOverview from "./InvolvementKindOverview.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import BulkInvolvementLoader from "./BulkInvolvementLoader.svelte";

    export let parentEntityRef;

    const Modes = {
        VIEW: "VIEW",
        BULK_UPLOAD: "BULK_UPLOAD"
    }

    let activeMode = Modes.VIEW;

    $: involvementKindCall = involvementKindStore.getById(parentEntityRef.id);
    $: involvementKind = $involvementKindCall.data;


    function reload(id) {
        involvementKindCall = involvementKindStore.getById(id, true);
    }

    function bulkUpdate() {

        activeMode = Modes.VIEW;
    }

</script>

<PageHeader icon="id-badge"
            name={involvementKind?.name}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.system.list">System Admin</ViewLink>
            </li>
            <li>
                <ViewLink state="main.system.involvement-kinds">Involvement Kinds</ViewLink>
            </li>
            <li>
                <span>{involvementKind?.name}</span>
            </li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach"
     style="margin-bottom: 5em;">
    <div class="waltz-display-section">
        <InvolvementKindOverview {involvementKind} {reload}/>
    </div>
</div>


<div class="waltz-section">

    <div class="waltz-section-header">
        <div class="waltz-section-header-title">
            Involvements
        </div>
    </div>

    <div class="container-fluid waltz-section-body">

        <div class="waltz-section-actions">
            {#if activeMode === Modes.VIEW}
                <button class="btn btn-xs btn-default"
                        on:click={() => activeMode = Modes.BULK_UPLOAD}>
                    Bulk Insert
                </button>
            {:else}
                <button class="btn btn-xs btn-default"
                        on:click={() => activeMode = Modes.VIEW}>
                    Cancel
                </button>
            {/if}
        </div>

        <div class="row">
            <div class="col-sm-12">
                {#if activeMode === Modes.VIEW}
                    <h4>View table of stats</h4>
                    <InvolvementListPanel/>
                {:else if activeMode === Modes.BULK_UPLOAD}
                    <h4>Upload involvements here!</h4>
                    <BulkInvolvementLoader {involvementKind} onSave={bulkUpdate}/>
                    <div style="padding-top: 1em">
                        <button class="btn btn-skinny"
                                on:click={() => activeMode = Modes.VIEW}>
                            <Icon name="ban"/>
                            Cancel
                        </button>
                    </div>
                {/if}
            </div>
        </div>
    </div>


</div>