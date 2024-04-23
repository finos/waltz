<script>

    import _ from "lodash";

    export let sourceOutboundClassification;
    export let targetInboundClassification;

</script>

<div style="display: inline-block; width: 10px;">
    <svg width="100%"
         height="100%"
         preserveAspectRatio="xMidYMin"
         style="vertical-align: middle"
         viewbox="0 0 100 100">
        {#if _.isEmpty(sourceOutboundClassification) || sourceOutboundClassification.code === "NO_OPINION"}
            <polygon stroke="black"
                     stroke-width="5"
                     fill="none"
                     points="0,0 0,100 100,0" />
        {:else}
            <polygon fill={sourceOutboundClassification.color}
                     stroke="black"
                     stroke-width="5"
                     points="0,0 0,100 100,0" />
        {/if}
        {#if _.isEmpty(targetInboundClassification) || targetInboundClassification.code === "NO_OPINION"}
            <polygon stroke="black"
                     stroke-width="5"
                     fill="none"
                     points="0,100 100,100 100,0" />
        {:else}
            <polygon fill={targetInboundClassification.color}
                     stroke="black"
                     stroke-width="5"
                     points="0,100 100,100 100,0" />
        {/if}
    </svg>
</div>
