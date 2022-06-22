<script>
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {fade} from 'svelte/transition';
    export let assessment;
</script>


<table class="waltz-field-table waltz-field-table-border"
       style="width: 100%">
    <colgroup>
        <col width="20%">
        <col width="60%">
    </colgroup>
    <tr>
        <td class="text-muted"
            style="vertical-align: top">
            Rating
        </td>
        <td>
            {#if assessment.ratingItem}
                <RatingIndicatorCell {...assessment.ratingItem}
                                     show-name="true"/>
            {:else}
                Not provided
            {/if}
            {#if assessment.ratingItem?.description}
                <span class="help-block">
                    <Markdown text={assessment.ratingItem?.description}/>
                </span>
            {/if}
        </td>
    </tr>
    <tr>
        <td class="text-muted">Comment</td>
        <td>
            <Markdown inline={true}
                      text={assessment.rating?.comment || ""}/>
        </td>
    </tr>
    {#if assessment.rating?.isReadOnly}
        <tr transition:fade={{ duration: 300 }}>
            <td class="text-muted">Locked By</td>
            <td>
                <Icon name="lock"/>
                {assessment.rating?.lastUpdatedBy}
            </td>
        </tr>
    {/if}
    <tr>
        <td class="text-muted">Last updated by</td>
        <td>
            <LastEdited entity={assessment.rating}/>
        </td>
    </tr>
</table>