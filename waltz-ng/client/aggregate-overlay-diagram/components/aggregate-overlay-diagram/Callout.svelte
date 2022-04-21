<script>
    import {createEventDispatcher, getContext} from "svelte";

    export let callout;
    export let label;

    let hoveredCallout = getContext("hoveredCallout");

    const dispatch = createEventDispatcher();

    function hover() {
        dispatch("hover", callout);
    }

    function leave() {
        dispatch("leave", callout);
    }

    function determineFill(hc, callout) {
        if (hc?.id === callout.id) {
            return "#fffbdc"
        } else {
            return `url(#content-gradient-${callout.id})`;
        }
    }

    function determineRadius(hc, callout) {
        if (hc?.id === callout.id) {
            return 12
        } else {
            return 10;
        }
    }

    $: radius = determineRadius($hoveredCallout, callout)

</script>


<svg class="content"
     viewBox={`0 0 24 24`}>
    <defs>
        <linearGradient id={`content-gradient-${callout.id}`}
                        x1="0"
                        x2="0"
                        y1="0"
                        y2="1">
            <stop stop-color={callout.startColor} offset="0%"/>
            <stop stop-color={callout.startColor} offset="50%"/>
            <stop stop-color={callout.endColor} offset="50%"/>
            <stop stop-color={callout.endColor} offset="100%"/>
        </linearGradient>
    </defs>
    <g on:mouseenter={() => hover()}
       on:mouseleave={() => leave()}>
        <circle r={radius}
                cx="12"
                cy="12"
                fill={determineFill($hoveredCallout, callout)}/>
        <text pointer-events="none"
              text-anchor="middle"
              dx="12"
              dy="17">
            {label}
        </text>
    </g>
</svg>


<style>

    .content circle {
        transition: fill 0.5s;
        transition: r 1s;
    }
</style>