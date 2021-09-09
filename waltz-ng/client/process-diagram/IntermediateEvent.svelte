<script>
    export let obj;

    function onMouseEnter() {
        console.log("ie:me:", obj);
    }
</script>


<g transform={`translate(0 ${obj.height * 1.4})`} on:mouseenter={onMouseEnter}>
    <circle cx={obj.width / 2}
            r={obj.width / 1.5}>
    </circle>

    <circle cx={obj.width / 2}
            r={obj.width / 1.8}>
    </circle>

    <foreignObject width={obj.width * 4}
                   height="200"
                   transform={`translate(${obj.width * -1}, ${obj.width * 1.5})`}>
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
</style>