<script>

    import InvolvementListPanel from "./InvolvementListPanel.svelte";
    import BulkInvolvementLoader from "./BulkInvolvementLoader.svelte";
    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";

    export let primaryEntityRef;
    let involvementKind;
    let involvementKindCall;

    const Modes = {
        VIEW: "VIEW",
        BULK_UPLOAD: "BULK_UPLOAD"
    }

    let activeMode = Modes.VIEW;

    function bulkUpdate() {

        activeMode = Modes.VIEW;
        return reload(primaryEntityRef.id)
    }

    function reload(id) {
        involvementKindCall = involvementKindStore.getById(id, true);
    }

    $: involvementKindCall = involvementKindStore.getById(primaryEntityRef.id);
    $: involvementKind = $involvementKindCall?.data;

</script>

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
            <InvolvementListPanel {involvementKind}/>
        {:else if activeMode === Modes.BULK_UPLOAD}
            <BulkInvolvementLoader {involvementKind}
                                   onSave={bulkUpdate}/>
        {/if}
    </div>
</div>