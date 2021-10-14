<script>
    export let obj;
    export let layout;

</script>


<rect width={layout.width}
      height={layout.height}
      rx="10"
      ry="10">
</rect>

<foreignObject width={layout.width}
               height={layout.height}
               style="pointer-events: none"
               y={layout.height / 6}>
    <div>
        {obj.name}
    </div>
</foreignObject>


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