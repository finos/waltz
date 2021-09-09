<script>
    export let obj;
    export let layoutData;

    function onMouseEnter() {
        console.log("ie:me:", obj);
    }
</script>


<g on:mouseenter={onMouseEnter}>
    <circle cx={layoutData.width / 2}
            r={layoutData.width / 1.5}>
    </circle>

    <circle cx={layoutData.width / 2}
            r={layoutData.width / 1.8}>
    </circle>

    <foreignObject width={layoutData.width * 4}
                   height="200"
                   transform={`translate(${layoutData.width * -1}, ${layoutData.width * 1.5})`}>
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