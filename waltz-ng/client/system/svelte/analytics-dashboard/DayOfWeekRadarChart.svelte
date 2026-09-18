<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ChartInfo from "./ChartInfo.svelte";
    import * as d3 from "d3";
    import {onDestroy} from "svelte";
    import {analyticsDashboardStore} from "../../../svelte-stores/analytics-dashboard-store";
    import {REMOTE_API_STATUS} from "../../../common/constants";

    export let startDate;
    export let endDate;

    let svgContainer;
    let containerWidth;

    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    // Radar overlays access-log hits and change-log changes, so it needs both endpoints.
    $: accessCall = (startDate && endDate) ? analyticsDashboardStore.findActivityByDay(startDate, endDate) : null;
    $: changeCall = (startDate && endDate) ? analyticsDashboardStore.findChangesByDay(startDate, endDate) : null;
    $: loading = !accessCall || !changeCall
        || $accessCall.status === REMOTE_API_STATUS.LOADING
        || $changeCall.status === REMOTE_API_STATUS.LOADING;
    $: error = (accessCall && $accessCall.error) || (changeCall && $changeCall.error)
        ? "Failed to load day of week data"
        : null;
    $: chartData = toChartData(accessCall ? $accessCall.data : null, changeCall ? $changeCall.data : null);

    onDestroy(() => {
        // Clean up any pending timeouts or DOM references
        if (svgContainer) {
            try {
                d3.select(svgContainer).selectAll("*").remove();
            } catch (e) {
                // Ignore cleanup errors
            }
        }
    });

    // Fold both series onto the 7 days of the week. The api returns db day-of-week
    // (1=Monday..7=Sunday); access log as a list of {dayOfWeek, counts}, change log as a
    // Map<Integer, Long>. A zero-only result collapses to no datasets.
    function toChartData(accessData, changeData) {
        if (!accessData || !changeData) {
            return { title: "Activity by Day of Week", datasets: [] };
        }

        const accessByDay = [];
        const changesByDay = [];

        for (let day = 0; day < 7; day++) {
            // Map JavaScript day (0=Sunday) to database day (1=Monday...7=Sunday)
            const dbDay = day === 0 ? 7 : day; // Convert Sunday from 0 to 7

            // Access log data
            const accessFound = accessData.find(item => item.dayOfWeek === dbDay);
            accessByDay.push({
                day: day,
                dayName: dayNames[day],
                value: accessFound ? accessFound.counts : 0
            });

            // Change log data (note: API returns Map<Integer, Long>)
            const changeFound = changeData[dbDay];
            changesByDay.push({
                day: day,
                dayName: dayNames[day],
                value: changeFound || 0
            });
        }

        // Check if all values are zero
        const hasAccessData = accessByDay.some(item => item.value > 0);
        const hasChangeData = changesByDay.some(item => item.value > 0);

        if (!hasAccessData && !hasChangeData) {
            return { title: "Activity by Day of Week", datasets: [] };
        }

        return {
            title: "Activity by Day of Week",
            datasets: [
                {
                    name: "Hits",
                    data: accessByDay,
                    color: "#2196F3"
                },
                {
                    name: "Changes",
                    data: changesByDay,
                    color: "#FF9800"
                }
            ]
        };
    }

    $: if (svgContainer && chartData && chartData.datasets && chartData.datasets.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.datasets) {
            return;
        }

        // Clear previous chart safely
        try {
            const container = d3.select(svgContainer);
            if (!container.empty()) {
                container.selectAll("svg").remove();
            }
        } catch (e) {
        }

        const margin = {top: 40, right: 120, bottom: 40, left: 120};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;
        const radius = Math.min(width, height) / 2;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left + width/2},${margin.top + height/2})`);

        // Calculate max value for scaling
        const maxValue = d3.max(chartData.datasets, dataset =>
            d3.max(dataset.data, d => d.value)
        );

        // Angle scale
        const angleScale = d3.scaleLinear()
            .domain([0, 7])
            .range([0, 2 * Math.PI]);

        // Radius scale
        const radiusScale = d3.scaleLinear()
            .domain([0, maxValue])
            .range([0, radius]);

        // Draw concentric circles (grid)
        const gridLevels = 5;
        for (let level = 1; level <= gridLevels; level++) {
            svg.append("circle")
                .attr("r", (radius / gridLevels) * level)
                .attr("fill", "none")
                .attr("stroke", "#e0e0e0")
                .attr("stroke-width", 1);
        }

        // Draw axis lines
        dayNames.forEach((day, i) => {
            const angle = angleScale(i) - Math.PI / 2; // Start from top
            const x = Math.cos(angle) * radius;
            const y = Math.sin(angle) * radius;

            svg.append("line")
                .attr("x1", 0)
                .attr("y1", 0)
                .attr("x2", x)
                .attr("y2", y)
                .attr("stroke", "#e0e0e0")
                .attr("stroke-width", 1);
        });

        // Add day labels. Anchor them away from the plot so the left/right days (which sit on the
        // horizontal axis) don't overlap their own data vertex: right-side labels start at the point
        // and extend outward, left-side labels end at the point, top/bottom stay centred.
        dayNames.forEach((day, i) => {
            const angle = angleScale(i) - Math.PI / 2;
            const labelRadius = radius + 12;
            const x = Math.cos(angle) * labelRadius;
            const y = Math.sin(angle) * labelRadius;
            const anchor = Math.abs(x) < 1 ? "middle" : (x > 0 ? "start" : "end");

            svg.append("text")
                .attr("x", x)
                .attr("y", y)
                .attr("text-anchor", anchor)
                .attr("dominant-baseline", "middle")
                .style("font-size", "12px")
                .style("font-weight", "bold")
                .style("fill", "#333")
                .text(day);
        });

        // Draw datasets
        chartData.datasets.forEach((dataset, datasetIndex) => {
            // Create line generator. d3.lineRadial/areaRadial already measure the angle clockwise
            // from 12 o'clock, so use angleScale(i) directly - this matches the axes, day labels and
            // point markers (which apply the -PI/2 offset to the standard trig cos/sin). Applying the
            // offset here as well rotated the polygon off its labels.
            const line = d3.lineRadial()
                .angle((d, i) => angleScale(i))
                .radius(d => radiusScale(d.value))
                .curve(d3.curveLinearClosed);

            // Draw area
            const area = d3.areaRadial()
                .angle((d, i) => angleScale(i))
                .innerRadius(0)
                .outerRadius(d => radiusScale(d.value))
                .curve(d3.curveLinearClosed);

            svg.append("path")
                .datum(dataset.data)
                .attr("d", area)
                .attr("fill", dataset.color)
                .attr("fill-opacity", 0.2)
                .attr("stroke", dataset.color)
                .attr("stroke-width", 2);

            // Draw line
            svg.append("path")
                .datum(dataset.data)
                .attr("d", line)
                .attr("fill", "none")
                .attr("stroke", dataset.color)
                .attr("stroke-width", 3);

            // Draw points
            dataset.data.forEach((d, i) => {
                const angle = angleScale(i) - Math.PI / 2;
                const r = radiusScale(d.value);
                const x = Math.cos(angle) * r;
                const y = Math.sin(angle) * r;

                svg.append("circle")
                    .attr("cx", x)
                    .attr("cy", y)
                    .attr("r", 4)
                    .attr("fill", dataset.color)
                    .attr("stroke", "#fff")
                    .attr("stroke-width", 2)
                    .style("cursor", "pointer")
                    .on("mouseover", function(event) {
                        d3.select(this).attr("r", 6);

                        const tooltip = d3.select("body").append("div")
                            .attr("class", "tooltip")
                            .style("position", "absolute")
                            .style("background", "rgba(0,0,0,0.9)")
                            .style("color", "white")
                            .style("padding", "10px")
                            .style("border-radius", "6px")
                            .style("font-size", "13px")
                            .style("pointer-events", "none")
                            .style("z-index", "1000")
                            .style("box-shadow", "0 4px 8px rgba(0,0,0,0.3)")
                            .html(`<strong>${d.dayName}</strong><br/>${dataset.name}: ${d.value.toLocaleString()}`)
                            .style("left", (event.pageX + 10) + "px")
                            .style("top", (event.pageY - 10) + "px");
                    })
                    .on("mouseout", function() {
                        d3.select(this).attr("r", 4);
                        d3.selectAll(".tooltip").remove();
                    });
            });
        });

        // Add legend
        const legend = svg.append("g")
            .attr("transform", `translate(${radius + 40}, ${-chartData.datasets.length * 15 / 2})`);

        const legendItems = legend.selectAll(".legend-item")
            .data(chartData.datasets)
            .enter().append("g")
            .attr("class", "legend-item")
            .attr("transform", (d, i) => `translate(0, ${i * 25})`);

        legendItems.append("circle")
            .attr("r", 6)
            .attr("fill", d => d.color);

        legendItems.append("text")
            .attr("x", 15)
            .attr("y", 5)
            .style("font-size", "12px")
            .style("fill", "#333")
            .text(d => d.name);

        // Add scale labels
        for (let level = 1; level <= gridLevels; level++) {
            const value = (maxValue / gridLevels) * level;
            svg.append("text")
                .attr("x", 5)
                .attr("y", -(radius / gridLevels) * level)
                .style("font-size", "10px")
                .style("fill", "#666")
                .text(value.toLocaleString());
        }
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Activity by Day of Week"}
        <span class="pull-right"><ChartInfo help="Access-log hits and change-log changes summed by day of the week."/></span>
    </div>
    <div slot="content">
        <div bind:clientWidth={containerWidth} style="position: relative; min-height: 400px;">
            {#if loading}
                <LoadingPlaceholder/>
            {:else if error}
                <div class="alert alert-danger">
                    <Icon name="warning"/>
                    {error}
                </div>
            {:else if !chartData || !chartData.datasets || chartData.datasets.length === 0}
                <NoData/>
            {:else}
                <div bind:this={svgContainer}></div>
            {/if}
        </div>
    </div>
</SubSection>

<style>
    :global(.tooltip) {
        opacity: 0.9;
    }
</style>


