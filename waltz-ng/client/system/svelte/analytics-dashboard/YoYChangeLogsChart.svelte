<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import BarChart from "./BarChart.svelte";
    import LineChart from "./LineChart.svelte";
    import {changeLogSummariesStore} from "../../../svelte-stores/change-log-summaries-store";

    let parentKind = "";
    let childKind = ""

    $: yoyChangeLogCall = changeLogSummariesStore.findYearOnYearChanges(parentKind, childKind);
    $: yoyChangeLogData = $yoyChangeLogCall.data;
    $: yoyChangeLogChartData = yoyChangeLogData
        ? Object.entries(yoyChangeLogData)
            .map(([key, value]) => ({name: key, value: value}))
            .sort((a, b) => a.name - b.name)
        : [];

    $: console.log(yoyChangeLogChartData);
</script>

<div class="row">
<div class="col-sm-12">
<SubSection>
    <div slot="header">
        YoY Changes
    </div>
    <div slot="content">
        <LineChart chartData={yoyChangeLogChartData}></LineChart>
    </div>
</SubSection>
</div>
</div>
