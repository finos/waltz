<script>
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import _ from 'lodash';
    import LineChart from "./LineChart.svelte";
    import {changeLogSummariesStore} from "../../../svelte-stores/change-log-summaries-store";
    import DropdownPicker from "../../../common/svelte/DropdownPicker.svelte";
    import {entity} from "../../../common/services/enums/entity";

    let parentKind;
    let childKind;

    const entityList = [
        {name: "All", icon: "info", description: "Placeholder for all entities", key: "", position: 0},
        entity.ACTOR,
        entity.APP_GROUP,
        entity.APPLICATION,
        entity.CHANGE_INITIATIVE,
        entity.DATA_TYPE,
        entity.END_USER_APPLICATION,
        entity.LOGICAL_DATA_FLOW,
        entity.ORG_UNIT,
        entity.PHYSICAL_FLOW,
        entity.MEASURABLE,
        entity.MEASURABLE_CATEGORY,
        entity.FLOW_CLASSIFICATION_RULE,
        entity.SURVEY_INSTANCE,
        entity.ASSESSMENT_DEFINITION
    ];

    const TIME_FRAMES = {
        YEARLY: "Yearly",
        MONTHLY: "Monthly"
    }

    export let chartTimeFrame = TIME_FRAMES.YEARLY;
    export let year;
    export let numToMonthMap;

    $: changeLogCall = chartTimeFrame === TIME_FRAMES.MONTHLY ? changeLogSummariesStore.findMonthOnMonthChanges(parentKind, childKind, year)
        : changeLogSummariesStore.findYearOnYearChanges(parentKind, childKind);
    $: changeLogData = $changeLogCall.data;
    $: changeLogChartData = changeLogData
        ? Object.entries(changeLogData)
            .map(([key, value]) => ({name: key, value: value}))
            .sort((a, b) => a.name - b.name)
            .map(d => chartTimeFrame === TIME_FRAMES.MONTHLY ? {name: numToMonthMap[d.name], value: d.value} : d)
        : [];

    $: console.log(changeLogChartData);
    $: timeFramePlaceholderText = chartTimeFrame ?? TIME_FRAMES.YEARLY;
    $: yearPlaceholderText = chartTimeFrame === TIME_FRAMES.MONTHLY && year ? `(${year})` : "";
</script>

<div class="row">
    <div class="col-sm-12">
        <div class="row">
            <div class="col-sm-12">
                <SubSection>
                    <div slot="header">
                        {timeFramePlaceholderText} Changes {yearPlaceholderText}
                    </div>
                    <div slot="content">
                        <LineChart chartData={changeLogChartData}></LineChart>
                    </div>
                </SubSection>
            </div>
        </div>
        <div class="row">
            <div class="col-sm-6">
                <DropdownPicker items={entityList}
                                onSelect={d => parentKind = d.key}
                                selectedItem={_.find(entityList, d => d.key === parentKind)}
                                defaultMessage="Select Parent Entity Kind"/>
            </div>
        </div>
    </div>
</div>
