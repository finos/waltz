<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import EntityIcon from "../../../common/svelte/EntityIcon.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import AssessmentDefinitionEditor from "./AssessmentDefinitionEditor.svelte";

    import {termSearch} from "../../../common";
    import {assessmentDefinitionStore} from "../../../svelte-stores/assessment-definition.js";
    import {ratingSchemeStore} from "../../../svelte-stores/rating-schemes";
    import AssessmentDefinitionRemover from "./AssessmentDefinitionRemovalConfirmation.svelte";
    import AssessmentDefinitionRemovalConfirmation from "./AssessmentDefinitionRemovalConfirmation.svelte";

    const definitions = assessmentDefinitionStore.loadAll();
    const ratingSchemes = ratingSchemeStore.loadAll();

    const Modes = {
        LIST: "list",
        EDIT: "edit",
        DELETE: "delete"
    };

    let qry = "";
    let selectedDefinition = null;
    let activeMode = "list"; // edit | delete

    $: definitionList = _
        .chain(termSearch($definitions.data, qry, ["name", "entityKind"]))
        .orderBy("name")
        .value();

    $: ratingSchemesById = _.keyBy($ratingSchemes.data, "id");


    function onEdit(def) {
        selectedDefinition = def;
        activeMode = Modes.EDIT;
    }


    function onDelete(def) {
        selectedDefinition = def;
        activeMode = Modes.DELETE;
    }


    function doSave(d) {
        return assessmentDefinitionStore
            .save(d)
            .then(() => {
                selectedDefinition = null;
                activeMode = Modes.LIST;
                assessmentDefinitionStore.loadAll(true);
            });
    }

    function doRemove(id) {
        return assessmentDefinitionStore
            .remove(id)
            .then(() => {
                selectedDefinition = null;
                activeMode = Modes.LIST;
                assessmentDefinitionStore.loadAll(true);
            });
    }


    function doCancel() {
        activeMode = Modes.LIST;
        selectedDefinition = null;
    }


    function mkNew() {
        selectedDefinition = {
            isReadOnly: false,
            lastUpdatedBy: "temp-will-be-overwritten-by-server",
            visibility: "SECONDARY"
        };
    }

    $: console.log({d: $definitions, r: ratingSchemesById});


</script>

<PageHeader icon="puzzle-piece"
            name="Assessment Definitions">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Assessment Definitions</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        {#if selectedDefinition}
        <div class="col-md-12">
            {#if activeMode === Modes.EDIT}
                <AssessmentDefinitionEditor definition={selectedDefinition}
                                            {doCancel}
                                            {doSave}/>
            {:else if activeMode === Modes.DELETE}
                <AssessmentDefinitionRemovalConfirmation definition={selectedDefinition}
                                                         {doCancel}
                                                         {doRemove}/>
            {/if}
        </div>
        {:else }
        <div class="col-md-12">
            <SearchInput bind:value={qry}/>
            <table class="table table-condensed table-striped"
                   style="table-layout: fixed">
                <thead>
                    <tr>
                        <th style="width:40%">Name</th>
                        <th style="width:25%">Applicable Kind</th>
                        <th style="width:35%">Operations</th>
                    </tr>
                </thead>
                <tbody>
                {#each definitionList as def}
                    <tr>
                        <td>
                            <span title={def.description}>
                                {def.name}
                            </span>
                            {#if def.isReadOnly}
                                <span class="text-muted">
                                    <Icon name="lock"/>
                                </span>
                            {/if}
                            <ul class="list-inline">
                                {#each ratingSchemesById[def.ratingSchemeId]?.ratings || [] as rating}
                                    <li>
                                        <div class="rating-square"
                                             title={ rating.name }
                                             style="background-color: {rating.color}">
                                        </div>
                                    </li>
                                {/each}
                            </ul>
                        </td>
                        <td>
                            <EntityIcon kind={def.entityKind}/>
                            {def.entityKind}
                        </td>
                        <td>
                            <button class="btn-link"
                                    on:click={() => onEdit(def)}>
                                <Icon name="edit"/>
                                Edit
                            </button>
                            |
                            <button class="btn-link"
                                    on:click={() => onDelete(def)}>
                                <Icon name="trash"/>
                                Delete
                            </button>
                            |
                            <a href="../../assessment-definition/{def.id}">
                                <Icon name="table"/>
                                View Data
                            </a>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
            <button class="btn-link"
                    on:click={mkNew}>
                <Icon name="plus"/>
                Add new assessment definition
            </button>
        </div>
        {/if}
    </div>
</div>

<style>
    .rating-square {
        width: 1em;
        height: 1em;
        display: inline-block;
    }
</style>