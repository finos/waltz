<script>
    export let obj;
    export let layoutData;

    function onMouseEnter() {
        console.log("nc:me:", obj);
    }

</script>


<rect width={layoutData.width}
      height={layoutData.height}
      rx="10"
      ry="10"
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
        fill: url(#NavigationCell-gradient);
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>