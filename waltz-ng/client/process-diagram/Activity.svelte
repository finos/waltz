<script>
    import {calcRectAttrs} from "./process-diagram-utils";

    export let obj;
</script>


<rect rx="10"
      ry="10"
      width={obj.width}
      height={obj.height}>
</rect>

<foreignObject width={obj.width}
               height={obj.height}
               y={obj.height / 6}>
    <div style="text-align: center; font-size: smaller">
        {obj.name}
    </div>
</foreignObject>


<style>
    rect {
        stroke: #ccc;
        fill: url(#Activity-gradient);
    }
</style>