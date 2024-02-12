<script>

    import DescriptionFade from "../../common/svelte/DescriptionFade.svelte";
    import RatingIndicatorCell from "../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";

    export let rating;
    export let ratingSchemeItem;
    export let measurable;

</script>


{#if measurable}
    <div>
        <h4>{measurable.name}</h4>
        <span class="text-muted">{measurable.externalId}</span>
        <DescriptionFade text={measurable.description}/>
    </div>
    <hr>
{/if}

<div>
    <RatingIndicatorCell {...ratingSchemeItem}
                         showName={true}/>
    <div>
        {ratingSchemeItem.description}
    </div>
</div>

{#if rating.description}
    <hr>
    <div>
        <div>
            Rating Comment:
        </div>
        <DescriptionFade text={rating.description || ""}/>
    </div>
{:else }
    <br>
{/if}