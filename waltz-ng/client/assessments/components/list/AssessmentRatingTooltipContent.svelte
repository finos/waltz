<script>

    import Markdown from "../../../common/svelte/Markdown.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";

    export let rating;
    export let ratingItem;

</script>


<h4>{ratingItem.name}</h4>
<div>
    {ratingItem.description}
</div>
{#if rating.comment}
    <hr>
    <span>
        <Icon name="sticky-note-o"/>
        <Markdown inline={true}
                  text={rating.comment}/>
    </span>
{/if}
<div class="small">
    Last Modified:
    <LastEdited entity={rating}/>
</div>