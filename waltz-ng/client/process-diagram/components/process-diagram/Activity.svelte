<script>
    export let obj;
    export let layout;

    function onMouseEnter() {
        console.log("activity:me:", obj);
    }

</script>


<rect rx="10"
      ry="10"
      width={layout.width}
      height={layout.height}
      on:mouseenter={onMouseEnter}>
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
        opacity: 0.8;
        stroke: #ccc;
        fill: url(#Activity-gradient);
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>