<script>
import SubSection from "../../../common/svelte/SubSection.svelte";
import LineChart from "./LineChart.svelte";
import {accessLogStore} from "../../../svelte-stores/access-log-store";

const CHART_MODES = {
    distinct: "distinct",
    all: "all"
}

let currentMode = CHART_MODES.distinct;
let toggleState = true;

const handleToggle = () => {
    const currentToggleState = toggleState;
    if(currentToggleState) {
        currentMode = CHART_MODES.all;
    } else {
        currentMode = CHART_MODES.distinct;
    }
    toggleState = !currentToggleState;
}

$: yearOnYearUsersCall = accessLogStore.findYearOnYearUsers(CHART_MODES.distinct);
$: yearOnYearUsers = $yearOnYearUsersCall.data;
$: yearOnYearUsersChartData = yearOnYearUsers
    ? yearOnYearUsers
        .map(d => (
            {name: d.year, value: d.counts}
        ))
        .sort((a, b) => a.name - b.name)
    : [];

$: yearOnYearHitsCall = accessLogStore.findYearOnYearUsers(CHART_MODES.all);
$: yearOnYearHits = $yearOnYearHitsCall.data;
$: yearOnYearHitsChartData = yearOnYearHits
    ? yearOnYearHits
        .map(d => (
            {name: d.year, value: d.counts}
        ))
        .sort((a, b) => a.name - b.name)
    : [];

</script>

<div class="col-sm-6">
<SubSection>
    <div slot="header">
        Year On Year Users
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
<!--    <div slot="controls">-->
<!--        <div class="col-sm-12">-->
<!--            <Toggle labelOn="Unique" labelOff="All" state={toggleState} onToggle={() => handleToggle()}/>-->
<!--        </div>-->
<!--    </div>-->
</SubSection>
</div>
<div class="col-sm-6">
    <SubSection>
        <div slot="header">
            Year On Year Hits
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
        <!--    <div slot="controls">-->
        <!--        <div class="col-sm-12">-->
        <!--            <Toggle labelOn="Unique" labelOff="All" state={toggleState} onToggle={() => handleToggle()}/>-->
        <!--        </div>-->
        <!--    </div>-->
    </SubSection>
</div>