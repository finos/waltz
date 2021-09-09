<script>
    import {calcRectAttrs} from "./process-diagram-utils";

    export let obj;

    function onMouseEnter() {
        console.log("a:me:", obj);
    }

</script>


<rect rx="10"
      ry="10"
      width={obj.width}
      height={obj.height}
      on:mouseenter={onMouseEnter}>
</rect>

<foreignObject width={obj.width}
               height={obj.height}
               style="pointer-events: none"
               y={obj.height / 6}>
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