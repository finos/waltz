<script>

    import DefaultOverlay from "./overlays/DefaultOverlay.svelte";

    export let data;
    export let overlay;

    let comp;

    function determinePanel(overlay) {
        switch (overlay) {
        default:
            return DefaultOverlay;
        }
    }

    $: comp = determinePanel(overlay);

</script>


{#if data}
    <svelte:component this={comp} {data}/>
{/if}