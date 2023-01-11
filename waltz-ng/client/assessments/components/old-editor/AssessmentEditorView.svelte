<script>
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import RatingIndicatorCell from "../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {fade} from 'svelte/transition';
    import MiniActions from "../../../common/svelte/MiniActions.svelte";

    export let rating;
    export let actions;

    $: console.log({rating, actions});
</script>


<table class="waltz-field-table waltz-field-table-border"
       style="width: 100%; margin-bottom: 2em">
    <colgroup>
        <col width="20%">
        <col width="20%">
        <col width="60%">
    </colgroup>
    <tbody>
    <tr>
        <th class="text-muted"
            style="vertical-align: top">
            Rating
        </th>
        <td>
            {#if rating.ratingItem}
                {#if rating.ratingItem.ratingGroup}
                        <span class="text-muted">
                            {rating.ratingItem.ratingGroup} /
                        </span>
                {/if}
                <RatingIndicatorCell {...rating.ratingItem}
                                     show-name="true"/>
            {:else}
                Not provided
            {/if}
            {#if rating.ratingItem?.description}
                    <span class="help-block">
                        <Markdown text={rating.ratingItem?.description}/>
                    </span>
            {/if}
        </td>
    </tr>
    <tr>
        <th class="text-muted">Comment</th>
        <td>
            <Markdown inline={true}
                      text={rating.rating?.comment || ""}/>
        </td>
    </tr>
    {#if rating.rating?.isReadOnly}
        <tr transition:fade={{ duration: 300 }}>
            <th class="text-muted">Locked By</th>
            <td>
                <Icon name="lock"/>
                {rating.rating?.lastUpdatedBy}
            </td>
        </tr>
    {/if}
    <tr>
        <th class="text-muted">Last updated by</th>
        <td>
            <LastEdited entity={rating.rating}/>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <MiniActions {actions}/>
        </td>
    </tr>
    </tbody>
</table>