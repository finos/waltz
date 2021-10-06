<script>
    export let obj;
    export let layout;

    function onMouseEnter() {
        console.log("event:me:", obj);
    }
</script>


<g on:mouseenter={onMouseEnter}>
    <circle cx={layout.width / 2}
            r={layout.width / 1.5}>
    </circle>

    <circle cx={layout.width / 2}
            r={layout.width / 1.8}>
    </circle>

    <foreignObject width={layout.width * 4}
                   height={layout.height}
                   border="1px solid red"
                   transform={`translate(${layout.width * -1}, ${layout.height * 0.5})`}>
        <div style="text-align: left; font-size: smaller">
            {obj.name}
        </div>
    </foreignObject>
</g>


<style>
    circle {
        stroke: #aaa;
        fill: #d3ecde;
    }

    foreignObject div {
        text-align: center;
        font-size: smaller
    }
</style>