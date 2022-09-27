<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {surveyTemplateStore} from "../../../../svelte-stores/survey-template-store";
    import _ from "lodash";
    import {entityFieldReferenceStore} from "../../../../svelte-stores/entity-field-reference-store";
    import {entity} from "../../../../common/services/enums/entity";
    import NoData from "../../../../common/svelte/NoData.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";

    export let onSelect = () => console.log("Selecting survey field kind");
    export let selectionFilter = () => true;
    export let subjectKindFilter = () => true;

    let selectedTemplate = null;
    let showActiveOnly = true;

    $: templatesCall = surveyTemplateStore.findAll();
    $: templates = _
        .chain($templatesCall.data)
        .filter(d => subjectKindFilter(d.targetEntityKind))
        .filter(r => !showActiveOnly || r.status === 'ACTIVE')
        .orderBy(d => d.name)
        .value();

    $: entityFieldReferenceCall = entityFieldReferenceStore.findAll();
    $: entityFieldReferences = $entityFieldReferenceCall.data;

    $: fieldReferences = _
        .chain(entityFieldReferences)
        .filter(d => d.entityKind === "SURVEY_INSTANCE")
        .map(d => Object.assign(
            {},
            d,
            {
                columnEntityId: selectedTemplate?.id,
                columnEntityKind: entity.SURVEY_TEMPLATE.key,
                entityFieldReference: d,
                columnName: selectedTemplate?.name,
                displayName: null
            }))
        .orderBy(d => d.displayName)
        .value();

    $: rowData = _.filter(fieldReferences, selectionFilter)

    const columnDefs = [
        {field: "entityFieldReference.displayName", name: "Field", width: "30%"},
        {field: "entityFieldReference.description", name: "Description", width: "70%"},
    ];

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
        <Icon name="info-circle"/>
        Select a survey field from the list below, you can filter the list using the search bar or
        <button on:click={clearSelectedTemplate}
                class="btn-skinny">
            choose a different template
        </button>
        .
    </div>
    <p>Survey fields for template: <strong>{selectedTemplate.name}</strong></p>
    {#if _.isEmpty(rowData)}
        <NoData type="info">There are no more fields to add</NoData>
    {:else }
        <Grid {columnDefs}
              {rowData}
              onSelectRow={onSelect}/>
    {/if}
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
