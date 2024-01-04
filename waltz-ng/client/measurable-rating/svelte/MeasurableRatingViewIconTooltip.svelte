<script>


    import Icon from "../../common/svelte/Icon.svelte";
    import _ from "lodash";

    export let rating;
    export let replacementApps;
    export let plannedDecommission;
    export let allocations;

</script>

<div>

    <table>
        <tbody>
        {#if rating.isPrimary}
            <tr>
                <td><Icon name="star"/></td>
                <td>This rating is marked primary</td>
            </tr>
        {/if}

        {#if !_.isEmpty(replacementApps)}
            <tr>
                <td><Icon name="hand-o-right"/></td>
                <td>This rating is due to be decommissioned and has {_.size(replacementApps)} replacement applications</td>
            </tr>
        {:else if !_.isEmpty(plannedDecommission)}
            <tr>
                <td><Icon name="hand-paper-o"/></td>
                <td>This rating is due to be decommissioned on {plannedDecommission.plannedDecommissionDate}</td>
            </tr>
        {/if}

        {#if !_.isEmpty(allocations)}
            <tr>
                <td><Icon name="pie-chart"/></td>
                <td>This rating has associated allocations</td>
            </tr>
        {/if}

        {#if rating.isReadOnly}
            <tr>
                <td><Icon name="lock"/></td>
                <td>This rating is read only</td>
            </tr>
        {/if}
        </tbody>
    </table>

</div>

<style>

    td {
        vertical-align: top;
    }
</style>