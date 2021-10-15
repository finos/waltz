<script>
    import {truncateMiddle} from "../../../../common/string-utils";
    import {select} from "d3-selection";
    import {wrapText} from "../../../../common/d3-utils";
    export let obj;
    export let layout;
    export let appCount;
    export let isSelected;

    let elem;

    $: select(elem)
        .text(truncateMiddle(obj.name, 64))
        .call(wrapText, layout.width - 10);

</script>


<rect class={isSelected ? "selected" : ""}
      rx="10"
      ry="10"
      width={layout.width}
      height={layout.height}>
</rect>

<text transform="translate({layout.width / 2}, 15)"
      style="pointer-events: none"
      dominant-baseline="middle"
      text-anchor="middle"
      font-size="11"
      fill="#332B23"
      bind:this={elem}>
</text>

{#if appCount > 0}
    <g  class="app-count"
        transform="translate({layout.width})">
        <circle cx="0" cy="5" r="12">
        </circle>
        <text dx="0" dy="10" text-anchor="middle">
            {appCount}
        </text>
    </g>
{/if}

<style>
    rect {
        opacity: 0.8;
        stroke: #ccc;
        fill: url(#Activity-gradient);
        transition: stroke ease-in-out 0.4s;
    }

    rect.selected {
        opacity: 1;
        stroke: #2b98ff;
        stroke-width: 3;
        fill: url(#Activity-gradient);
    }

    .app-count circle{
        fill: #d5fffc;
        stroke: #84a5a4;
    }
</style>