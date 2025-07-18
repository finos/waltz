<script>
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import { accessLogStore } from "../../../svelte-stores/access-log-store";
    import { changeLogStore } from "../../../change-log/services/change-log-store";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import BarChart from "./BarChart.svelte";
    import { debounce } from "../utils/debounce";
    import YearOnYearUsersChart from "./YearOnYearUsersChart.svelte";
    import YoYChangeLogsChart from "./YoYChangeLogsChart.svelte";

    const DEBOUNCE_TIMEOUT = 500; //seconds
    let mostViewedPagesSinceWeeksLive  = 1;

    let mostViewedPagesSinceWeeks = mostViewedPagesSinceWeeksLive;

    $: mostViewedPagesSinceDays = mostViewedPagesSinceWeeks * 7;

    $: mostViewedPagesCall = accessLogStore.findAccessLogsSince(mostViewedPagesSinceDays);

    $: mostViewedPages = $mostViewedPagesCall.data;

    $: mostViewedPagesChartData = mostViewedPages
        ? mostViewedPages
            .map(d => ({ name: d.state, value: d.counts }))
            .sort((a, b) => b.value - a.value)
            .slice(0, 10)
        : [];

    $: console.log(mostViewedPagesChartData);

    const refreshState = () => {
        mostViewedPagesSinceWeeks = mostViewedPagesSinceWeeksLive;
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
            <YearOnYearUsersChart/>
        </div>
    </div>
    <hr/>
    <div class="row">
        <div class="col-sm-12">
            <div class="col-sm-6">
                <SubSection>
                    <div slot="header">
                        Most Viewed Pages [Top 10]
                    </div>
                    <div slot="content">
                        <div class="row">
                            <div class="col-sm-12">
                                <form class="form-inline pull-right">
                                    <div class="form-group">
                                        <label for="weeks-input">Number of weeks </label>
                                        <input
                                            type="number"
                                            min="1"
                                            max="52"
                                            class="form-control input-sm text-left"
                                            id="weeks-input"
                                            aria-label="Number of weeks"
                                            bind:value={mostViewedPagesSinceWeeksLive}
                                            on:input={debouncedRefreshState}
                                        />
                                    </div>
                                </form>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-sm-12">
                                <BarChart chartData={mostViewedPagesChartData} />
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-sm-12">
                                {#if mostViewedPagesChartData.length > 0}
                                    <div>
                                        {#each mostViewedPagesChartData as d}
                                            <ViewLink state={d.name.split("|")[0]} ctx={{id: Math.floor(Math.random() * 100)}}>{d.name}</ViewLink>
                                            <span>({d.value}) </span>
                                        {/each}
                                    </div>
                                {/if}
                            </div>
                        </div>
                    </div>
                </SubSection>
            </div>
            <div class="col-sm-6">
                <YoYChangeLogsChart/>
            </div>
        </div>
    </div>
</div>