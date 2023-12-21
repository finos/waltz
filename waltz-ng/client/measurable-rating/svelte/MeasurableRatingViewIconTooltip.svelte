<script>


    import Icon from "../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let rating;
    export let replacementApps;
    export let plannedDecommission;
    export let allocations;

</script>

<div>
    {#if rating.isPrimary}
        <div>
            <Icon name="star"/> This rating is marked primary
        </div>
    {/if}

    {#if !_.isEmpty(replacementApps)}
        <div>
            <Icon name="hand-o-right"/> This rating is due to be decommissioned and has {_.size(replacementApps)} replacement applications
        </div>
    {:else if !_.isEmpty(plannedDecommission)}
        <div>
            <Icon name="hand-paper-o"/> This rating is due to be decommissioned on {plannedDecommission.plannedDecommissionDate}
        </div>
    {/if}

    {#if !_.isEmpty(allocations)}
        <div>
            <Icon name="pie-chart"/> This rating has associated allocations
        </div>
    {/if}

    {#if rating.isReadOnly}
        <div>
            <Icon name="lock"/> This rating is read only
        </div>
    {/if}
</div>