<script>
    import {getContext} from "svelte";
    import RatingIndicatorCell
        from "../../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import DescriptionFade from "../../../../../common/svelte/DescriptionFade.svelte";
    import _ from "lodash";

    const overlayData = getContext("overlayData");

    $: ratings = _
        .chain($overlayData.cellData)
        .map(d => d.counts)
        .flatMap()
        .map(d => d.rating)
        .uniqBy(d => d.id)
        .orderBy(["position", "name"])
        .value();

</script>


<h4>Legend</h4>

<dl class="small">
    {#each ratings as rating}
        <dt>
            <RatingIndicatorCell {...rating}/>
        </dt>
        <dd>
            <DescriptionFade text={rating.description}/>
        </dd>
    {/each}
</dl>