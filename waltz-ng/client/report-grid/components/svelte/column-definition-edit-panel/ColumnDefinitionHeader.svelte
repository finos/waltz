<script>

    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";
    import {surveyTemplateStore} from "../../../../svelte-stores/survey-template-store";
    import {getColumnName} from "../report-grid-utils";

    export let column;

    let measurableCall;
    let categoryCall;
    let surveyTemplateCall;

    $: {
        if (column?.columnEntityId) {
            if (column?.columnEntityKind === entity.MEASURABLE.key) {
                measurableCall = measurableStore.getById(column?.columnEntityId);
            } else if (column?.columnEntityKind === entity.SURVEY_QUESTION.key) {
                surveyTemplateCall = surveyTemplateStore.getByQuestionId(column?.columnEntityId);
            }
        }
    }

    $: {
        if (measurable?.categoryId) {
            categoryCall = measurableCategoryStore.getById(measurable?.categoryId);
        }
    }

    $: measurable = $measurableCall?.data;
    $: category = $categoryCall?.data;
    $: surveyTemplate = $surveyTemplateCall?.data;

    function determineColumnType(kind) {
        switch (kind) {
            case "REPORT_GRID_FIXED_COLUMN_DEFINITION":
                return "Fixed";
            case "REPORT_GRID_DERIVED_COLUMN_DEFINITION":
                return "Derived";
            default:
                return "Unknown"
        }
    }

    $: columnType = determineColumnType(column.kind);

</script>


<h4>{getColumnName(column)}</h4>
<div class="help-block small">
    <DescriptionFade text={column?.columnDescription || ''}/>
</div>

<div>
    <table class="table table-condensed small">
        <tbody>
        <tr>
            <td width="50%">
                Column Type
            </td>
            <td width="50%">{columnType}</td>
        </tr>
        {#if column?.columnEntityKind === entity.MEASURABLE.key}
            <tr>
                <td width="50%">Category</td>
                <td width="50%">{category?.name}</td>
            </tr>
        {:else if column?.columnEntityKind === entity.SURVEY_QUESTION.key}
            <tr>
                <td width="50%">Template</td>
                <td width="50%">{surveyTemplate?.name}</td>
            </tr>
        {/if}
        </tbody>
    </table>
</div>
