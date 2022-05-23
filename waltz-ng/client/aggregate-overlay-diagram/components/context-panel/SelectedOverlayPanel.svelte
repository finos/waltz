<script>
    import {getContext} from "svelte";

    const selectedOverlayCell = getContext("selectedOverlay");
    const widget = getContext("widget");

    let overlayHolder;

    $: {
        if (overlayHolder && $widget) {
            overlayHolder.innerHTML = "";
            if ($selectedOverlayCell?.props) {

                const component = $widget.overlay;

                new component({
                    target: overlayHolder,
                    props: Object.assign({}, $selectedOverlayCell.props, {isContext: true})
                });

            }
        }
    }
</script>

{#if $selectedOverlayCell}
    <h4>{$selectedOverlayCell?.cellName}</h4>
{/if}
<div bind:this={overlayHolder}></div>
