<script>
    import _ from "lodash";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Icon from "../../common/svelte/Icon.svelte";

    export let assessmentRatings = [];

</script>

<table class="table table-condensed small">
    <thead>
        <tr>
            <th></th>
            <th>AssessmentRating</th>
            <th>Code</th>
        </tr>
    </thead>
    <tbody>
    {#each _.orderBy(assessmentRatings, d => d.ratingSchemeItem.name) as rating}
        <tr>
            <td>
                {#if rating.isReadOnly}
                    <Icon title="This rating has been marked as read only" name="lock"/>
                {/if}
            </td>
            <td>
                <RatingIndicatorCell {...rating.ratingSchemeItem}/>
            </td>
            <td>
                {rating.ratingSchemeItem.rating}
            </td>
        </tr>
    {/each}
    </tbody>
</table>


<style>

    table {
        width: 100%;
    }

    table td {
        padding-bottom: 0.2em;
        padding-right: 0.6em;
        border-bottom: 1px solid #eee;
        vertical-align: top;
    }
</style>