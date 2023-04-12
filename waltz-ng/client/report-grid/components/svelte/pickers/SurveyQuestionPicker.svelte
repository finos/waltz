<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {surveyTemplateStore} from "../../../../svelte-stores/survey-template-store";
    import {surveyQuestionStore} from "../../../../svelte-stores/survey-question-store";
    import _ from "lodash";
    import Toggle from "../../../../common/svelte/Toggle.svelte";
    import {mkReportGridFixedColumnRef} from "../report-grid-utils";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;
    export let subjectKindFilter = () => true;

    let selectedTemplate = null;
    let showActiveOnly = true;

    $: templatesCall = surveyTemplateStore.findAll();

    $: templates = _
        .chain($templatesCall?.data)
        .filter(d => subjectKindFilter(d.targetEntityKind))
        .filter(r => !showActiveOnly || r.status === 'ACTIVE')
        .orderBy(d => d.name)
        .value();

    $: questionsCall = selectedTemplate && surveyQuestionStore.findQuestionsForTemplate(selectedTemplate?.id)
    $: questions = $questionsCall?.data || [];

    $: showSectionName = _.some(questions, d => !_.isEmpty(d.sectionName))

    $: rowData = _
        .chain(questions)
        .filter(d => selectionFilter(mkReportGridFixedColumnRef(d, "questionText")))
        .value();

    const sectionCols = [{field: "sectionName", name: "Section", width: "40%"}];


    const defaultColumnDefs = [
        {field: 'questionText', name: 'Question', width: '40%'},
        {field: "label", name: "Label", width: "40%"},
        {field: "fieldType", name: "Type", width: "20%"}];

    $: columnDefs = showSectionName
        ? _.concat(sectionCols, defaultColumnDefs)
        : defaultColumnDefs

    const templateColumnDefs = [
        {field: "name", name: "Survey Name", width: "40%"},
        {field: "description", name: "Description", width: "60%", maxLength: 300},
    ];

    function selectTemplate(template) {
        selectedTemplate = template;
    }

    function clearSelectedTemplate() {
        selectedTemplate = null;
    }

</script>

{#if selectedTemplate}
    <div class="help-block small">
        <Icon name="info-circle"/>Select a question from the list below, you can filter the list using the search bar or
        <button on:click={clearSelectedTemplate}
                class="btn-skinny">
            choose a different template
        </button>.
    </div>
    <p>Questions for template: <strong>{selectedTemplate.name}</strong></p>
    <Grid {columnDefs}
          {rowData}
          onSelectRow={d => onSelect(mkReportGridFixedColumnRef(d, "questionText"))}/>
{:else}
    <div class="help-block small">
        <Icon name="info-circle"/>
        Select a template from the list below, you can filter the list using the search bar.
    </div>
    <br>
    <Toggle labelOn="Active Templates Only"
            labelOff="Active Templates Only"
            state={showActiveOnly}
            onToggle={() => showActiveOnly = !showActiveOnly}/>
    <Grid columnDefs={templateColumnDefs}
          rowData={templates}
          onSelectRow={selectTemplate}/>
{/if}
