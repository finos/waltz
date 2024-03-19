<script>

    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import RatingIndicatorCell from "../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";

    export let flowClassificationRule;
    export let flowClassification;
    export let dataType;


    function goToRule(rule) {
        $pageInfo = {
            state: "main.flow-classification-rule.view",
            params: {
                id: rule.id
            }
        };
    }

</script>

<table class="table table-condensed small">
    <colgroup>
        <col width="30%">
        <col width="70%">
    </colgroup>
    <tbody>
    <tr>
        <td>Source</td>
        <td>
            <EntityLink ref={flowClassificationRule.subjectReference}/>
        </td>
    </tr>
    <tr>
        <td>Scope</td>
        <td>
            <EntityLink ref={flowClassificationRule.vantagePointReference}/>
        </td>
    </tr>
    <tr>
        <td>Data Type</td>
        <td>
            {#if dataType}
                <EntityLink ref={dataType}/>
            {:else}
                -
            {/if}
        </td>
    </tr>
    <tr>
        <td>Classification</td>
        <td>
            <RatingIndicatorCell {...flowClassification}/>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <button class="btn btn-skinny" on:click={() => goToRule(flowClassificationRule)}>
                Visit page
            </button>
        </td>
    </tr>
    </tbody>
</table>


<style>

    table {
        margin-bottom: 1em !important;
    }

</style>