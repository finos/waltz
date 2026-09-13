<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import LoadingPlaceholder from "../../../common/svelte/LoadingPlaceholder.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import ChartInfo from "./ChartInfo.svelte";
    import * as d3 from "d3";
    import {analyticsDashboardStore} from "../../../svelte-stores/analytics-dashboard-store";
    import {REMOTE_API_STATUS} from "../../../common/constants";
    import {formatPeriodLabel} from "./analytics-utils";

    export let startDate;
    export let endDate;
    export let period;

    let svgContainer;
    let containerWidth;

    $: call = (startDate && endDate && period)
        ? analyticsDashboardStore.findAccessLogTrends(period, startDate, endDate)
        : null;
    $: loading = call ? $call.status === REMOTE_API_STATUS.LOADING : true;
    $: error = call && $call.error ? "Failed to load chart data" : null;
    $: chartData = toChartData(call ? $call.data : null, period);

        // Transform the api response into the chart's `{title, data}` shape. Zero-only
        // series are collapsed to an empty series so the "no data" message is shown.
        function toChartData(data, period) {
            let rows = [];

            if (Array.isArray(data)) {
                rows = data.map(item => ({
                    name: formatPeriodLabel(item.period || item.date || item.key, period),
                    value: item.counts || item.count || item.value || item.total || 0,
                    distinctUserCount: item.distinctUserCount || 0
                }));
            } else if (data && typeof data === 'object') {
                rows = Object.entries(data).map(([key, value]) => ({
                    name: formatPeriodLabel(key, period),
                    value: value || 0,
                    distinctUserCount: 0
                }));
            }

            const hasNonZeroData = rows.some(item => item.value > 0);
            return {
                title: "Access Log Analytics",
                data: hasNonZeroData ? rows : []
            };
        }

    $: if (svgContainer && chartData && chartData.data && chartData.data.length > 0 && containerWidth) {
        renderChart();
    }

    function renderChart() {
        if (!svgContainer || !chartData || !chartData.data) {
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

        const margin = {top: 40, right: 30, bottom: 40, left: 60};
        const width = containerWidth - margin.left - margin.right;
        const height = 400 - margin.top - margin.bottom;

        const svg = d3.select(svgContainer)
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left},${margin.top})`);

        // X scale (band for grouped bars)
        const x0 = d3.scaleBand()
            .domain(chartData.data.map(d => d.name))
            .range([0, width])
            .padding(0.2);

        const x1 = d3.scaleBand()
            .domain(['accesses', 'users'])
            .range([0, x0.bandwidth()])
            .padding(0.05);

        // Y scale (linear) - use max of both values
        const y = d3.scaleLinear()
            .domain([0, d3.max(chartData.data, d => Math.max(d.value, d.distinctUserCount))])
            .nice()
            .range([height, 0]);

        // Add X axis
        svg.append("g")
            .attr("transform", `translate(0, ${height})`)
            .call(d3.axisBottom(x0))
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(-45)");

        // Add Y axis
        svg.append("g")
            .call(d3.axisLeft(y));

        // Add legend
        const legend = svg.append("g")
            .attr("transform", `translate(${width - 150}, -25)`);

        legend.append("rect")
            .attr("x", 0)
            .attr("width", 15)
            .attr("height", 15)
            .attr("fill", "#2196F3");

        legend.append("text")
            .attr("x", 20)
            .attr("y", 12)
            .style("font-size", "12px")
            .text("Hits Count");

        legend.append("rect")
            .attr("x", 92)
            .attr("width", 15)
            .attr("height", 15)
            .attr("fill", "#FF9800");

        legend.append("text")
            .attr("x", 114)
            .attr("y", 12)
            .style("font-size", "12px")
            .text("Users Count");

        // Create groups for each period
        const groups = svg.selectAll(".group")
            .data(chartData.data)
            .enter().append("g")
            .attr("class", "group")
            .attr("transform", d => `translate(${x0(d.name)}, 0)`);

        // Add access count bars
        groups.append("rect")
            .attr("class", "bar-accesses")
            .attr("x", d => x1('accesses'))
            .attr("width", x1.bandwidth())
            .attr("y", d => y(d.value))
            .attr("height", d => height - y(d.value))
            .attr("fill", "#2196F3")
            .attr("stroke", "#1565C0")
            .attr("stroke-width", 1)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("fill", "#42A5F5");
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
                    .html(`<strong>${d.name}</strong><br/>Hits Count: ${d.value.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("fill", "#2196F3");
                d3.selectAll(".tooltip").remove();
            });

        // Add distinct user bars
        groups.append("rect")
            .attr("class", "bar-users")
            .attr("x", d => x1('users'))
            .attr("width", x1.bandwidth())
            .attr("y", d => y(d.distinctUserCount))
            .attr("height", d => height - y(d.distinctUserCount))
            .attr("fill", "#FF9800")
            .attr("stroke", "#F57C00")
            .attr("stroke-width", 1)
            .style("cursor", "pointer")
            .on("mouseover", function(event, d) {
                d3.select(this).attr("fill", "#FFB74D");
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
                    .html(`<strong>${d.name}</strong><br/>Users Count: ${d.distinctUserCount.toLocaleString()}`)
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px");
            })
            .on("mouseout", function(event, d) {
                d3.select(this).attr("fill", "#FF9800");
                d3.selectAll(".tooltip").remove();
            });

        // Add value labels on top of access count bars
        groups.append("text")
            .attr("class", "label")
            .attr("x", d => x1('accesses') + x1.bandwidth() / 2)
            .attr("y", d => y(d.value) - 5)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => d.value.toLocaleString());

        // Add value labels on top of distinct user bars
        groups.append("text")
            .attr("class", "label")
            .attr("x", d => x1('users') + x1.bandwidth() / 2)
            .attr("y", d => y(d.distinctUserCount) - 5)
            .attr("text-anchor", "middle")
            .style("font-size", "10px")
            .style("fill", "#333")
            .text(d => d.distinctUserCount.toLocaleString());
    }
</script>

<SubSection>
    <div slot="header">
        {chartData?.title || "Access Log Trends"}
        <span class="pull-right"><ChartInfo help="Total page hits (blue) and distinct users (orange) for each period in the selected range."/></span>
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
            {:else if !chartData || !chartData.data || chartData.data.length === 0}
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


