<script>
    export let obj;
    export let layout;

</script>


<rect rx="10"
      ry="10"
      width={layout.width}
      height={layout.height}>
</rect>

<foreignObject width={layout.width}
               height={layout.height}
               style="pointer-events: none"
               y="12">
    <div>
        {obj.name}
    </div>
</foreignObject>


<style>
    rect {
        opacity: 0.8;
        stroke: #ccc;
        fill: none;
        stroke-dasharray: 8,4;
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>