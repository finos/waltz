<script>
import SubSection from "../../../common/svelte/SubSection.svelte";
import LineChart from "./LineChart.svelte";
import {accessLogStore} from "../../../svelte-stores/access-log-store";

const CHART_MODES = {
    distinct: "distinct",
    all: "all"
}

const TIME_FRAMES = {
    YEARLY: "Yearly",
    MONTHLY: "Monthly"
}

export let chartTimeFrame = TIME_FRAMES.YEARLY;
export let year;
export let numToMonthMap;

$: yearOnYearUsersCall = chartTimeFrame === TIME_FRAMES.MONTHLY ? accessLogStore.findMonthOnMonthUsers(CHART_MODES.distinct, year)
    : accessLogStore.findYearOnYearUsers(CHART_MODES.distinct);

$: yearOnYearUsers = $yearOnYearUsersCall.data;
$: yearOnYearUsersChartData = yearOnYearUsers
    ? yearOnYearUsers
        .map(d => (
            chartTimeFrame === TIME_FRAMES.MONTHLY ? {name: d.month, value: d.counts}
                : {name: d.year, value: d.counts}
        ))
        .sort((a, b) => a.name - b.name)
        .map(d => chartTimeFrame === TIME_FRAMES.MONTHLY ? {name: numToMonthMap[d.name], value: d.value} : d)
    : [];

$: yearOnYearHitsCall = chartTimeFrame === TIME_FRAMES.MONTHLY ? accessLogStore.findMonthOnMonthUsers(CHART_MODES.all, year)
    : accessLogStore.findYearOnYearUsers(CHART_MODES.all)


$: yearOnYearHits = $yearOnYearHitsCall.data;
$: yearOnYearHitsChartData = yearOnYearHits
    ? yearOnYearHits
        .map(d => (
            chartTimeFrame === TIME_FRAMES.MONTHLY ? {name: d.month, value: d.counts} : {name: d.year, value: d.counts}
        ))
        .sort((a, b) => a.name - b.name)
        .map(d => chartTimeFrame === TIME_FRAMES.MONTHLY ? {name: numToMonthMap[d.name], value: d.value} : d)
    : [];

$: timeFramePlaceholderText = chartTimeFrame ?? TIME_FRAMES.YEARLY;
$: yearPlaceholderText = chartTimeFrame === TIME_FRAMES.MONTHLY && year ? `(${year})` : "";

</script>

<div class="col-sm-6">
<SubSection>
    <div slot="header">
        {timeFramePlaceholderText} Users {yearPlaceholderText}
    </div>
    <div slot="content">
        <div class="row">
            <div class="col-sm-12">
                <LineChart chartData={yearOnYearUsersChartData}
                           lineColor="#C01000"
                           bulletColor="#400600"/>
            </div>
        </div>
    </div>
</SubSection>
</div>

<div class="col-sm-6">
    <SubSection>
        <div slot="header">
            {timeFramePlaceholderText} Hits {yearPlaceholderText}
        </div>
        <div slot="content">
            <div class="row">
                <div class="col-sm-12">
                    <LineChart chartData={yearOnYearHitsChartData}
                    lineColor="#90A8AC"
                    bulletColor="#003740"/>
                </div>
            </div>
        </div>
    </SubSection>
</div>