<script>
    import {createEventDispatcher, getContext} from "svelte";
    import {determineForegroundColor} from "../../../../common/colors";

    export let callout;
    export let label;
    export let hoveredCallout;

    const dispatch = createEventDispatcher();

    function hover() {
        dispatch("hover", callout);
    }

    function leave() {
        dispatch("leave", callout);
    }

    function determineFill(hc, callout) {
        if (hc?.id === callout.id) {
            return "#ecd243"
        } else {
            return `url(#content-gradient-${callout.id})`;
        }
    }

    function determineRadius(hc, callout) {
        if (hc?.id === callout?.id) {
            return 12
        } else {
            return 10;
        }
    }

    $: radius = determineRadius(hoveredCallout, callout)

</script>


{#if callout}
    <svg class="content"
         viewBox={`0 0 24 24`}>
        <defs>
            <linearGradient id={`content-gradient-${callout.id}`}
                            x1="0"
                            x2="0"
                            y1="0"
                            y2="1">
                <stop stop-color={callout.startColor} offset="0%"/>
                <stop stop-color={callout.startColor} offset="40%"/>
                <stop stop-color={callout.endColor} offset="60%"/>
                <stop stop-color={callout.endColor} offset="100%"/>
            </linearGradient>
        </defs>
        <g on:mouseenter={() => hover()}
           on:mouseleave={() => leave()}>
            <circle r={radius}
                    cx="12"
                    cy="12"
                    fill={determineFill($hoveredCallout, callout)}>
                <title>{callout.title}</title>
            </circle>
            <text text-anchor="middle"
                  dx="12"
                  fill={determineForegroundColor(callout.startColor)}
                  dy="17">
                {label}
            </text>
        </g>
    </svg>
{/if}


<style>

    .content circle {
        transition: fill 1s, r 1s;
    }

    .content text {
        pointer-events: none;
    }
</style>