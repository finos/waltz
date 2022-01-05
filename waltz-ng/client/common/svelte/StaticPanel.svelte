<script>

    import Markdown from "./Markdown.svelte";
    import Icon from "./Icon.svelte";

    export let panel;
    export let showTitle = true;
    export let showIcon = true;

</script>

<div style="margin-bottom: 1em">
    <h4>
        {#if showTitle}
            {#if showIcon}
                <Icon name={panel.icon}/>
            {/if}
            {panel.title}
        {/if}
    </h4>
    <span>
    {#if showIcon && !showTitle}
        <Icon name={panel.icon}/>
    {/if}
        <span>
        <Markdown inline="true"
                  text={panel.content}/>
    </span>
</span>
</div>
