<script>
    import Markdown from "../../common/svelte/Markdown.svelte";
    import _ from "lodash";

    export let text = "";

    let expanded = false;

    $: hasLongDescription = _.size(text) > 350;

</script>

<div class={hasLongDescription && !expanded ? "wdf-description" : ""}>
    <div class={hasLongDescription && !expanded ? "wdf-description-fade" : ""}></div>
    <Markdown text={text}/>
</div>
{#if hasLongDescription}
    <button class="btn btn-skinny small pull-right"
            on:click={() => expanded = !expanded}>
        {expanded ? "Show less" : "Show more"}
    </button>
{/if}


<style>

    .wdf-description {
        position:relative;
        max-height:6em;
        overflow:hidden;
    }

    .wdf-description-fade {
        position:absolute;
        top:2em;
        width:100%;
        height:4em;
        background: -webkit-linear-gradient(transparent, white);
        background: -o-linear-gradient(transparent, white);
        background: -moz-linear-gradient(transparent, white);
        background: linear-gradient(transparent, white);
    }

</style>