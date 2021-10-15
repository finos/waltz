<script>
    import {lookupSubTypeComponent} from "../process-diagram-utils";
    import {select} from "d3-selection";
    import {wrapText} from "../../../../common/d3-utils";

    export let obj;
    export let layout;
    export let isSelected;

    let elem;
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

    $: select(elem)
        .text(obj.name)
        .call(wrapText, layout.width)

    $: subTypeComponent = lookupSubTypeComponent(obj.objectSubType);

</script>


<g>

    <polyline class={isSelected ? "selected" : ""}
              stroke-linecap="square"
              {points}>
    </polyline>

    <text transform={`translate(${layout.width / 2.2}, ${layout.height})`}
          style="pointer-events: none"
          text-anchor="end"
          font-size="10"
          fill="#332B23"
          bind:this={elem}>
    </text>


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
        opacity: 0.8;
        transition: stroke ease-in-out 0.4s;
    }

    polyline.selected {
        stroke: #2b98ff;
        stroke-width: 3;
        fill: #eee;
    }
</style>