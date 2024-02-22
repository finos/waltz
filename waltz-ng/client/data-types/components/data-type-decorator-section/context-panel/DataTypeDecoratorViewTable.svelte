<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import LastEdited from "../../../../common/svelte/LastEdited.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";

    export let decorator;

</script>

<table class="table table-condensed small">
    <colgroup>
        <col width="30%">
        <col width="70%">
    </colgroup>
    <tbody>
    <tr>
        <td>Data Type</td>
        <td>
            <EntityLink ref={decorator.decoratorEntity}/>
        </td>
    </tr>
    <tr>
        <td>Classification</td>
        <td>
            <RatingIndicatorCell {...decorator.flowClassification}/>
        </td>
    </tr>
    <tr>
        <td>Last Updated</td>
        <td>
            <LastEdited showLabel={false} entity={decorator}/>
        </td>
    </tr>
    <tr>
        <td>Provenance</td>
        <td>
            {decorator.provenance}
        </td>
    </tr>
    {#if decorator.isReadonly}
        <tr>
            <td>Read Only</td>
            <td>
                <Icon name="lock"/> This decorator cannot be edited in Waltz
            </td>
        </tr>
    {/if}
    </tbody>
</table>


<style>

    table {
        margin-bottom: 1em !important;
    }

</style>