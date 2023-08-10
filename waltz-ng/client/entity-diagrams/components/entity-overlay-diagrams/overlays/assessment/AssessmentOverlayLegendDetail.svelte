<script>
    import _ from "lodash";
    import RatingIndicatorCell
        from "../../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import DescriptionFade from "../../../../../common/svelte/DescriptionFade.svelte";
    import {diagramService} from "../../entity-diagram-store";

    const {overlayData} = diagramService;

    $: ratings = _
        .chain(_.values($overlayData))
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