<script>

    export let ratingCharacteristics;
    export let hasDecorator = false;

</script>

<div style="display: inline-block"
     class="waltz-visibility-parent">
    <div class:waltz-visibility-child-30={!hasDecorator}>
        <div style="display: inline-block; width: 12px;">
            <svg width="100%"
                 height="100%"
                 preserveAspectRatio="xMidYMin"
                 style="vertical-align: middle"
                 viewbox="0 0 100 100">
                {#if ratingCharacteristics.sourceOutboundRating === "NO_OPINION"}
                    <polygon stroke="black"
                             stroke-width="5"
                             fill="none"
                             points="0,0 0,100 100,0" />
                {:else}
                    <polygon fill={ratingCharacteristics.sourceOutboundClassification.color}
                             points="0,0 0,100 100,0" />
                {/if}
                {#if ratingCharacteristics.targetInboundRating === "NO_OPINION"}
                    <polygon stroke="black"
                             stroke-width="5"
                             fill="none"
                             points="0,100 100,100 100,0" />
                {:else}
                    <polygon fill={ratingCharacteristics.targetInboundClassification.color}
                             points="0,100 100,100 100,0" />
                {/if}
            </svg>
        </div>
    </div>
</div>
