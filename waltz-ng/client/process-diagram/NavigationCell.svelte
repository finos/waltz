<script>
    export let obj;
</script>


<rect width={obj.width}
      height={obj.height}
      rx="10"
      ry="10">
</rect>

<foreignObject width={obj.width}
               height={obj.height}
               y={obj.height / 4}>
    <div style="text-align: center; font-size: smaller">
        {obj.name}
    </div>
</foreignObject>


<style>
    rect {
        stroke: #ccc;
        fill: #d3e6ec;
    }
</style>