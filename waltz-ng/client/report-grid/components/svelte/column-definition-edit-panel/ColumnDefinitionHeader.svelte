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

</script>


<h4>{getColumnName(column)}</h4>
<div class="help-block small">
    <DescriptionFade text={column?.columnDescription || ''}/>
</div>

{#if column?.columnEntityKind === entity.MEASURABLE.key}
    <div>
        <table class="table table-condensed small">
            <tbody>
            <tr>
                <td width="50%">Category</td>
                <td width="50%">{category?.name}</td>
            </tr>
            </tbody>
        </table>
    </div>
{:else if column?.columnEntityKind === entity.SURVEY_QUESTION.key}
    <div>
        <table class="table table-condensed small">
            <tbody>
            <tr>
                <td width="50%">Template</td>
                <td width="50%">{surveyTemplate?.name}</td>
            </tr>
            </tbody>
        </table>
    </div>
{/if}