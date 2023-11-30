<script>
    import Markdown from "./Markdown.svelte";
    import _ from "lodash";
    import Icon from "./Icon.svelte";

    export let text = "";
    export let context = {};
    export let expanderAlignment = "center"; // left | center (default) | right

    let expanded = false;

    $: hasLongDescription = _.size(text) > 350;

</script>

<div class:wdf-description={hasLongDescription && !expanded}> <!-- wdf-description limits the size of the box -->
    <div class:wdf-description-fade={hasLongDescription && !expanded}></div>  <!-- wdf-description-fade overlays the fade effect -->
    <Markdown {text} {context}/>
</div>
{#if hasLongDescription}
    <div class="expander" style:text-align={expanderAlignment}>
        <button class="btn btn-skinny small"
                on:click={() => expanded = !expanded}>
            {expanded ? "Show less" : "Show more"}
            <Icon name={expanded ? "chevron-up" : "chevron-down"}/>
        </button>
    </div>
{/if}


<style>

    .expander {
        text-align: center;
        width: 100%;
    }

    .wdf-description {
        position:relative;
        max-height:6em;
        overflow:hidden;
    }

    .wdf-description-fade {
        position:absolute;
        top:1em;
        width:100%;
        height:5em;
        background: -webkit-linear-gradient(transparent, white);
        background: -o-linear-gradient(transparent, white);
        background: -moz-linear-gradient(transparent, white);
        background: linear-gradient(transparent, white);
    }

</style>