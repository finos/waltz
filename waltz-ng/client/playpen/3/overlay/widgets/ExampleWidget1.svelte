<script>

    import {scaleLinear} from "d3-scale";

    export let cellData;

    const rScale = scaleLinear()
        .domain([0, 500])
        .range([0, 50])
        .clamp(true);

</script>

<h1>Hello Mum</h1>

<svg class="widget" viewBox="0 0 300 100">
    <g>
        <circle class=""
                r={rScale(cellData?.currentStateCount) || 10}
                cx="150"
                cy="50"
                fill={cellData?.currentStateCount > 100 ? 'pink' : 'yellow'}
                stroke="red"/>
        <text dx="150" dy="50" text-anchor="middle" font-size="30">{cellData?.currentStateCount}</text>

    </g>
</svg>
