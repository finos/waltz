<script>
import SubSection from "../../../common/svelte/SubSection.svelte";
import BarChart from "./BarChart.svelte";
import {accessLogStore} from "../../../svelte-stores/access-log-store";
import Toggle from "../../../common/svelte/Toggle.svelte";

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

$: yearOnYearUsersCall = accessLogStore.findYearOnYearUsers(currentMode);
$: yearOnYearUsers = $yearOnYearUsersCall.data;
$: yearOnYearUsersChartData = yearOnYearUsers
    ? yearOnYearUsers
        .map(d => (
            {name: d.year, value: d.counts}
        ))
        .sort((a, b) => a.name - b.name)
    : [];

</script>

<SubSection>
    <div slot="header">
        Year On Year Users
    </div>
    <div slot="content">
        <div class="row">
            <div class="col-sm-12">
                <BarChart chartData={yearOnYearUsersChartData} />
            </div>
        </div>
    </div>
    <div slot="controls">
        <div class="col-sm-12">
            <Toggle labelOn="Unique" labelOff="All" state={toggleState} onToggle={() => handleToggle()}/>
        </div>
    </div>
</SubSection>