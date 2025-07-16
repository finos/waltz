<script>
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import { accessLogStore } from "../../../svelte-stores/access-log-store";
    import _ from "lodash";
    import AccessCountBarChart from "./AccessCountBarChart.svelte";
    import AccessCountLineChart from "./AccessCountLineChart.svelte";
    import { debounce } from "../utils/debounce";

    const DEBOUNCE_TIMEOUT = 500; //seconds
    let mostViewedPagesSinceWeeksLive  = 1;
    let dailyViewershipSinceWeeksLive = 5;
    let dailyUniqueUsersSinceWeeksLive = 2;

    let mostViewedPagesSinceWeeks = mostViewedPagesSinceWeeksLive;
    let dailyViewershipSinceWeeks = dailyViewershipSinceWeeksLive;
    let dailyUniqueUsersSinceWeeks = dailyUniqueUsersSinceWeeksLive;

    $: mostViewedPagesSinceDays = mostViewedPagesSinceWeeks * 7;
    $: dailyViewershipSinceDays = dailyViewershipSinceWeeks * 7;
    $: dailyUniqueUsersSinceDays = dailyUniqueUsersSinceWeeks * 7;

    $: mostViewedPagesCall = accessLogStore.findAccessLogsSince(mostViewedPagesSinceDays);
    $: dailyViewershipsCall = accessLogStore.findAccessLogCountsSince(dailyViewershipSinceDays);
    $: dailyUniqueUsersCall = accessLogStore.findDailyActiveUserCountsSince(dailyUniqueUsersSinceDays);

    $: mostViewedPages = $mostViewedPagesCall.data
        ? _( $mostViewedPagesCall.data )
            .groupBy(d => d.state.split("|")[0])
            .mapValues(arr => _.sumBy(arr, d => d.counts ?? 1))
            .value()
        : undefined;

    $: dailyViewerships = $dailyViewershipsCall.data;

    $: dailyUniqueUsers = $dailyUniqueUsersCall.data;

    $: mostViewedPagesChartData = mostViewedPages
        ? Object.entries(mostViewedPages)
            .map(([key, value]) => ({ name: key, value: value }))
            .sort((a, b) => b.value - a.value)
            .slice(0, 10)
        : [];

    $: dailyViewershipsChartData = dailyViewerships
        ? dailyViewerships
            .map(d => ({
                name: `${d.year}-W${d.week}`,
                value: d.counts
            }))
            .sort((a, b) => {
                const [yearA, weekA] = a.name.split('-W').map(Number);
                const [yearB, weekB] = b.name.split('-W').map(Number);
                return yearA !== yearB
                    ? yearA - yearB
                    : weekA - weekB;
            })
        : [];

    $: dailyUniqueUsersChartData = dailyUniqueUsers
        ? dailyUniqueUsers
        .map(d => (
            {name: d.createdAt.split("T")[0], value: d.counts}
        ))
        .sort((a, b) => new Date(a.name) - new Date(b.name))
        : [];

    $: console.log(mostViewedPagesChartData);
    $: console.log(dailyViewershipsChartData);
    $: console.log(dailyUniqueUsers);

    const refreshState = () => {
        dailyViewershipSinceWeeks = dailyViewershipSinceWeeksLive;
        mostViewedPagesSinceWeeks = mostViewedPagesSinceWeeksLive;
        dailyUniqueUsersSinceWeeks = dailyUniqueUsersSinceWeeksLive;
    };

    const debouncedRefreshState = debounce(refreshState, DEBOUNCE_TIMEOUT);

</script>

<PageHeader icon="bar-chart"
            name="Analytics Dashboard">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Analytics Dashboard</li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-sm-12">
            <div class="col-sm-6">
                <h4>Most Viewed Pages [Top 10]</h4>
                <div class="row">
                    <div class="col-sm-2">Weeks</div>
                    <div class="btn-group btn-group-xs" role="group" >
                        <button class="btn" on:click={() => {mostViewedPagesSinceWeeksLive--; debouncedRefreshState();}}>-</button>
                        <button class="btn">{mostViewedPagesSinceWeeksLive}</button>
                        <button class="btn" on:click={() => {mostViewedPagesSinceWeeksLive++; debouncedRefreshState();}}>+</button>
                    </div>
                </div>
                <AccessCountBarChart chartData={mostViewedPagesChartData} />
                {#if mostViewedPagesChartData.length > 0}
                <div>
                    {#each mostViewedPagesChartData as d}
                    <ViewLink state={d.name} ctx={{id: Math.floor(Math.random() * 100)}}>{d.name}</ViewLink>
                    <span>({d.value}) </span>
                    {/each}
                </div>
                {/if}
            </div>
            <div class="col-sm-6">
                <h4>Accesses to waltz UI</h4>
                <div class="row">
                    <div class="col-sm-2">Weeks</div>
                    <div class="btn-group btn-group-xs" role="group">
                        <button class="btn" on:click={() => {dailyViewershipSinceWeeksLive--; debouncedRefreshState();}}>-</button>
                        <button class="btn">{dailyViewershipSinceWeeksLive}</button>
                        <button class="btn" on:click={() => {dailyViewershipSinceWeeksLive++; debouncedRefreshState();}}>+</button>
                    </div>
                </div>
                <AccessCountLineChart chartData={dailyViewershipsChartData} />
            </div>
        </div>
    </div>
    <hr/>
    <div class="row">
        <div class="col-sm-12">
            <div class="col-sm-12">
                <h4>Daily Unique Users</h4>
                <div class="row">
                    <div class="col-sm-1">Weeks</div>
                    <div class="btn-group btn-group-xs" role="group">
                        <button class="btn" on:click={() => {dailyUniqueUsersSinceWeeksLive--; debouncedRefreshState();}}>-</button>
                        <button class="btn">{dailyUniqueUsersSinceWeeksLive}</button>
                        <button class="btn" on:click={() => {dailyUniqueUsersSinceWeeksLive++; debouncedRefreshState();}}>+</button>
                    </div>
                </div>
                <AccessCountBarChart chartData={dailyUniqueUsersChartData} />
            </div>
        </div>
    </div>
</div>