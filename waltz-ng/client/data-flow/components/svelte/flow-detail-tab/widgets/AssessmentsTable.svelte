<script>
    import _ from "lodash";
    import RatingIndicatorCell from "../../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import EntityIcon from "../../../../../common/svelte/EntityIcon.svelte";

    export let ratingsByDefId = {};
    export let assessmentDefinitionsById = {};

</script>

<table class="table table-condensed small">
    <thead>
    <tr>
        <th>Assessment</th>
        <th>Rating</th>
    </tr>
    </thead>
    <tbody>
    {#each _.keys(ratingsByDefId) as defnId}
        {@const ratings = _.get(ratingsByDefId, [defnId], [])}
        {@const defn = _.get(assessmentDefinitionsById, [defnId], null)}
        <tr>
            <td>
                <EntityIcon kind={_.get(defn, ["entityKind"])}/>
                {_.get(defn, ["name"], "Unknown")}
            </td>
            <td>
                <ul class="list-inline">
                    {#each ratings as rating}
                        <li>
                            <RatingIndicatorCell {...rating}/>
                        </li>
                    {/each}
                </ul>
            </td>
        </tr>
    {/each}
    </tbody>
</table>
