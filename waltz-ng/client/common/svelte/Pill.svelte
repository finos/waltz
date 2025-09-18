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

    $: pillColor = pillDefs[pillKey].color;
    $: pillText = pillDefs[pillKey].name;
</script>

{#if pillDefs && pillKey}
    <div class:pill={!cleanPill}
         class:pill-clean={cleanPill}
         style="--pillColor: {pillColor}">
        {pillText}
        <small>
            { smallText ?? '' }
        </small>
    </div>
{/if}

<style>
    .pill {
        background: var(--pillColor);
        border: 0.15rem solid var(--pillColor);
        border-radius: 0rem;
        padding: 0.25rem 0.5rem;
        width: max-content;
        color: white;
        cursor: pointer;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .pill-clean {
        background: white;
        border: 0.15rem solid var(--pillColor);
        border-radius: 0rem;
        padding: 0.25rem 0.5rem;
        width: max-content;
        color: var(--pillColor);
        cursor: pointer;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
</style>