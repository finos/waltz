<script>
    export let obj;

    function onMouseEnter() {
        console.log("nc:me:", obj);
    }

</script>


<rect width={obj.width}
      height={obj.height}
      rx="10"
      ry="10"
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
        fill: url(#NavigationCell-gradient);
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>