<script>
    import {mkSelectionOptions} from "../../../../common/selector-utils";
    import {mkRef} from "../../../../common/entity-utils";
    import {flowDiagramEntityStore} from "../../../../svelte-stores/flow-diagram-entity-store";
    import {measurableStore} from "../../../../svelte-stores/measurables";
    import {measurableCategoryStore} from "../../../../svelte-stores/measurable-category-store";
    import _ from "lodash";
    import model from "../store/model";
    import {toGraphId} from "../../../flow-diagram-utils";
    import RelatedEntitiesViewTable from "./RelatedEntitiesViewTable.svelte";
    import AddRelatedMeasurableSubPanel from "./AddRelatedMeasurableSubPanel.svelte";
    import AddRelatedChangeInitiativeSubPanel from "./AddRelatedChangeInitiativeSubPanel.svelte";
    import AddRelatedDataTypeSubPanel from "./AddRelatedDataTypeSubPanel.svelte";

    export let diagramId;
    export let canEdit;

    const Modes = {
        VIEW: "VIEW",
        ADD_MEASURABLE: "ADD_MEASURABLE",
        ADD_CHANGE_INITIATIVE: "ADD_CHANGE_INITIATIVE",
        ADD_DATA_TYPE: "ADD_DATA_TYPE",
    };

    let activeMode = Modes.VIEW;

    $: measurablesCall = measurableStore.findMeasurablesBySelector(mkSelectionOptions(mkRef('FLOW_DIAGRAM', diagramId)), true);
    $: measurables = $measurablesCall.data;
    $: measurablesById = _.keyBy(measurables, d => d.id);

    $: measurableCategoryCall = measurableCategoryStore.findAll();
    $: categoriesById = _.keyBy($measurableCategoryCall.data, d => d.id);

    $: associatedCis = _
        .chain($model.relationships)
        .filter(d => d.data.kind === 'CHANGE_INITIATIVE')
        .sortBy("data.name")
        .value();

    $: associatedMeasurables = _
        .chain($model.relationships)
        .filter(d => d.data.kind === 'MEASURABLE')
        .map(d => {
            const measurable = measurablesById[d.data.id];
            const category = categoriesById[measurable?.categoryId];

            return Object.assign({}, d, {category: category})
        })
        .sortBy("data.name")
        .value();

    $: associatedMeasurableIds = _.map(associatedMeasurables, d => d.data.id)

    $: suggestedMeasurables = _
        .chain(measurables)
        .reject(m => _.includes(associatedMeasurableIds, m.id))
        .map(m => Object.assign({}, m, {category: categoriesById[m.categoryId]}))
        .sortBy(d => d?.name)
        .value();

    $: associatedDatatypes = _
        .chain($model.relationships)
        .filter(d => d.data.kind === 'DATA_TYPE')
        .sortBy("data.name")
        .value();

    function selectEntity(e) {
        flowDiagramEntityStore.addRelationship(diagramId, mkRef(e.kind, e.id, e.name));
        model.addRelationship({id: toGraphId(e), data: e});
        activeMode = Modes.VIEW;
    }

    function addEntityMode(e) {
        if (e.detail === 'MEASURABLE') {
            activeMode = Modes.ADD_MEASURABLE;
        } else if (e.detail === "CHANGE_INITIATIVE") {
            activeMode = Modes.ADD_CHANGE_INITIATIVE;
        } else if (e.detail === "DATA_TYPE") {
            activeMode = Modes.ADD_DATA_TYPE;
        } else {
            console.log("Cannot add entity kind: " + e.detail)
        }
    }

    function removeEntity(evt) {
        const entity = evt.detail
        flowDiagramEntityStore.removeRelationship(diagramId, entity.data);
        model.removeRelationship(entity);
    }
</script>

{#if activeMode === Modes.VIEW}
    <RelatedEntitiesViewTable {diagramId}
                              {canEdit}
                              measurables={associatedMeasurables}
                              changeInitiatives={associatedCis}
                              datatypes={associatedDatatypes}
                              on:removeEntity={removeEntity}
                              on:select={addEntityMode}/>
{:else if activeMode === Modes.ADD_MEASURABLE }
    <AddRelatedMeasurableSubPanel measurables={suggestedMeasurables}
                                  on:select={e => selectEntity(e.detail)}
                                  on:cancel={() => activeMode = Modes.VIEW}/>
{:else if activeMode === Modes.ADD_CHANGE_INITIATIVE }
    <AddRelatedChangeInitiativeSubPanel on:select={e => selectEntity(e.detail)}
                                        on:cancel={() => activeMode = Modes.VIEW}/>
{:else if activeMode === Modes.ADD_DATA_TYPE }
    <AddRelatedDataTypeSubPanel on:select={e => selectEntity(e.detail)}
                                on:cancel={() => activeMode = Modes.VIEW}/>
{/if}
