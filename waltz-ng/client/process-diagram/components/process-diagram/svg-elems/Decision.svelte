<script>
    import {lookupSubTypeComponent} from "../process-diagram-utils";

    export let obj;
    export let layout;
    export let isSelected;

    function onMouseEnter() {
        console.log("decision:me:", obj);
    }

    let points = "";
    $: {
        const w = layout.width;
        const h = layout.height;
        points = `
            ${w/2},0
            0,${h/2}
            ${w/2},${h}
            ${w},${h/2}
            ${w/2},0
        `;
    }

    $: subTypeComponent = lookupSubTypeComponent(obj.objectSubType);

</script>


<g on:mouseenter={onMouseEnter}>

    <polyline class={isSelected ? "selected" : ""}
              stroke-linecap="square"
              {points}>
    </polyline>

    <foreignObject width={layout.width * 2}
                   height="200"
                   transform={`translate(${layout.width * -0.7}, ${layout.height * 1})`}>
        <div style="text-align: left; font-size: 10px;">
            {obj.name}
        </div>
    </foreignObject>


    <!-- subtype -->
    {#if subTypeComponent}
        <g transform="translate(0 {layout.height / 2 * -1})"
           class="subtype">
            <svelte:component this={subTypeComponent}
                              width={layout.width}
                              height={layout.height}/>
        </g>
    {/if}

</g>


<style>
    polyline {
        stroke: #999;
        fill: #eee;
        transition: stroke ease-in-out 0.4s;
    }

    polyline.selected {
        stroke: #2b98ff;
        stroke-width: 3;
        fill: #eee;
    }
</style>