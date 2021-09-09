<script>
    import {calcRectAttrs} from "./process-diagram-utils";

    export let obj;
    export let layoutData;

    function onMouseEnter() {
        console.log("a:me:", obj);
    }

</script>


<rect rx="10"
      ry="10"
      width={layoutData.width}
      height={layoutData.height}
      on:mouseenter={onMouseEnter}>
</rect>

<foreignObject width={layoutData.width}
               height={layoutData.height}
               style="pointer-events: none"
               y={layoutData.height / 6}>
    <div>
        {obj.name}
    </div>
</foreignObject>


<style>
    rect {
        stroke: #ccc;
        fill: url(#Activity-gradient);
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>