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
            return callout.startColor;
        }
    }

</script>


<svg class="content"
     viewBox="0 0 20 20">
    <g on:mouseenter={() => hover()}
       on:mouseleave={() => leave()}>
        <circle r="10"
                cx="10"
                cy="10"
                fill={determineFill($hoveredCallout, callout)}/>
        <text pointer-events="none"
              text-anchor="middle"
              dx="10"
              dy="15">
            {label}
        </text>
    </g>
</svg>


<style>

    .content circle {
        transition: fill 2s;
    }
</style>