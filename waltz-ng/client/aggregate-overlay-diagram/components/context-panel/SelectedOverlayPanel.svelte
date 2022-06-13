<script>
    import {getContext} from "svelte";
    import Icon from "../../../common/svelte/Icon.svelte";

    const selectedOverlayCell = getContext("selectedOverlay");
    const widget = getContext("focusWidget");

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
{:else}
    <div class="help-block">
        <Icon name="info-circle"/>
        Select a cell on the diagram to view the overlay in more detail
    </div>
{/if}
<div bind:this={overlayHolder}></div>
