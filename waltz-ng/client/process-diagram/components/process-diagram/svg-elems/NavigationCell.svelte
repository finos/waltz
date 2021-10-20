<script>

    import {select} from "d3-selection";
    import {wrapText} from "../../../../common/d3-utils";
    export let obj;
    export let layout;

    let elem;

    $: select(elem)
        .text(obj.name)
        .call(wrapText, layout.width - 10);

</script>


<rect width={layout.width}
      height={layout.height}
      rx="10"
      ry="10">
</rect>

<text transform="translate({layout.width / 2}, 15)"
      style="pointer-events: none"
      dominant-baseline="middle"
      text-anchor="middle"
      font-size="11"
      fill="#332B23"
      bind:this={elem}>
</text>

<style>
    rect {
        stroke: #ccc;
        fill: url(#NavigationCell-gradient);
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>