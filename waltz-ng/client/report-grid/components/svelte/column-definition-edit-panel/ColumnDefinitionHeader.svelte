<script>

    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";
    import {entity} from "../../../../common/services/enums/entity";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";
    import {surveyTemplateStore} from "../../../../svelte-stores/survey-template-store";

    export let column;

    let measurableCall;
    let categoryCall;
    let surveyTemplateCall;

    $: {
        if (column?.columnEntityReference?.id) {
            if (column?.columnEntityReference?.kind === entity.MEASURABLE.key) {
                measurableCall = measurableStore.getById(column?.columnEntityReference?.id);
            } else if (column?.columnEntityReference?.kind === entity.SURVEY_QUESTION.key) {
                surveyTemplateCall = surveyTemplateStore.getByQuestionId(column?.columnEntityReference?.id);
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


<h4>{column?.columnEntityReference?.name}</h4>
<div class="help-block small">
    <DescriptionFade text={column?.columnEntityReference?.description || ''}/>
</div>

{#if column?.columnEntityReference?.kind === entity.MEASURABLE.key}
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
{:else if column?.columnEntityReference?.kind === entity.SURVEY_QUESTION.key}
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