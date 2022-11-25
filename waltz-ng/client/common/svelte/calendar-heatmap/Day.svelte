<script>

    import {dimensions} from "./calendar-heatmap-utils";

    export let data;
    export let color = "#E2F0FD";
    export let stroke = "#1F7FE0";
    export let onSelect = (x) => console.log("selecting date", x);

    const today = new Date();
    const noData = data.value === 0;

</script>


<circle class:future={data.date > today}
        class="clickable"
        r={dimensions.circleRadius}
        fill={noData ? "#eee" : color}
        stroke={noData ? "#bbb" : stroke}
        on:click={() => onSelect(data.date)}
        on:keydown={() => onSelect(data.date)}>
</circle>


<style>

    .future {
        stroke-dasharray: 2, 2;
        fill: #f9f9f9
    }

</style>