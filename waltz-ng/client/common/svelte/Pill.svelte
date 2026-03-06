<script>
    // pillDefs = {
    // KEY: {
    // name: "Key",
    // color: "#126372"
    // }, ....
    export let pillDefs;
    export let pillKey;
    export let smallText;
    export let cleanPill = false; // toggles between dark and light text
    export let clickable = false;

    $: pillBorderColor = pillDefs[pillKey].borderColor;
    $: pillText = pillDefs[pillKey].name;
    $: pillTextColor = pillDefs[pillKey].textColor;
    $: pillBgColor = pillDefs[pillKey].bgColor;
</script>

{#if pillDefs && pillKey}
    <div class:pill={!cleanPill}
         class:pill-clean={cleanPill}
         class:clickable={clickable}
         style="--pillBgColor: {pillBgColor} ; --pillTextColor: {pillTextColor}; --pillBorderColor: {pillBorderColor}"
         title={pillText}>
        {pillText}
        <small>
            { smallText ?? '' }
        </small>
    </div>
{/if}

<style>
    .pill {
        background: var(--pillBgColor);
        border: 0.15rem solid var(--pillBgColor);
        border-radius: 0rem;
        padding: 0.25rem 0.5rem;
        width: max-content;
        color: white;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .pill-clean {
        background: white;
        border: 0.15rem solid var(--pillBorderColor);
        border-radius: 0rem;
        padding: 0.25rem 0.5rem;
        width: max-content;
        color: var(--pillTextColor);
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
</style>