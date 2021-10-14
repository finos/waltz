<script>
    import {truncateMiddle} from "../../../../common/string-utils";
    export let obj;
    export let layout;
    export let appCount;
    export let isSelected;

</script>


<rect class={isSelected ? "selected" : ""}
      rx="10"
      ry="10"
      width={layout.width}
      height={layout.height}>
</rect>

<foreignObject width={layout.width}
               height={layout.height}
               style="pointer-events: none; padding: 2px;"
               y="4">
    <div title={obj.name}>
            {truncateMiddle(obj.name, 64)}
    </div>
</foreignObject>

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

    foreignObject div {
        text-align: center;
        font-size: 11px;
    }

    .app-count circle{
        fill: #d5fffc;
        stroke: #84a5a4;
    }
</style>