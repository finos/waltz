<script>
    import {getContext} from "svelte";

    const selectedOverlay = getContext("selectedOverlay");

    let overlayHolder;

    $: {
        if (overlayHolder) {
            overlayHolder.innerHTML = "";
            if ($selectedOverlay?.svg) {
                overlayHolder.appendChild($selectedOverlay.svg.cloneNode(true));
            }
        }
    }
</script>

{#if $selectedOverlay}
    <h3>{$selectedOverlay?.cellName}</h3>
{/if}
<div bind:this={overlayHolder}></div>
