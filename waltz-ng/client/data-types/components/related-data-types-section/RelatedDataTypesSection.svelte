<script>
    import _ from "lodash";
    import {dataTypeStore} from "../../../svelte-stores/data-type-store";
    import {buildHierarchies, reduceToSelectedNodesOnly} from "../../../common/hierarchy-utils";
    import DataTypeTreeNode from "../../../common/svelte/DataTypeTreeNode.svelte";
    import DataTypeTreeSelector from "../../../common/svelte/DataTypeTreeSelector.svelte";
    import {entityRelationshipStore} from "../../../svelte-stores/entity-relationship-store";
    import {mkRel} from "../../../common/relationship-utils";
    import {toEntityRef} from "../../../common/entity-utils";
    import {onMount} from "svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Markdown from "../../../common/svelte/Markdown.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import {userStore} from "../../../svelte-stores/user-store";
    import {getEditRoleForEntityKind} from "../../../common/role-utils";
    import {entity} from "../../../common/services/enums/entity";


    const Modes = {
        EDIT: "EDIT",
        VIEW: "VIEW"
    };

    const root = {};

    export let primaryEntityReference;

    let dataTypesCall = dataTypeStore.findAll();
    let selected = [];
    let mode = Modes.VIEW;
    let viewHierarchy = [];
    let focusedType = null;
    let canEdit = false;

    let currentDataTypesCall = null;
    let userCall = null;

    let restrictedEntities = [entity.MEASURABLE.key]; // Determines which parent pages the permissions check applies to

    function reload() {
        currentDataTypesCall = entityRelationshipStore.findForEntity(
            primaryEntityReference,
            "SOURCE",
            ["RELATES_TO"],
            true);
    }

    onMount(() => {
        reload();
        userCall = userStore.load();
    });

    function onFocus(evt) {
        focusedType = evt.detail;
    }

    function onSelect(evt) {
        const dt = evt.detail;
        const alreadySelected = _.includes(selected, dt.id);

        const remoteCall = alreadySelected
            ? entityRelationshipStore.remove
            : entityRelationshipStore.create;

        const rel = mkRel(
            primaryEntityReference,
            "RELATES_TO",
            toEntityRef(evt.detail));

        const msgAction = alreadySelected
            ? "removed"
            : "added";

        remoteCall(rel)
            .then(() => {
                toasts.success(`Successfully ${msgAction} data type: ${dt.name}`);
            })
            .catch(err => {
                displayError(`Failed ${msgAction} data type: ${dt.name}`, err);
            })
            .finally(reload);
    }

    $: selected = _
        .chain($currentDataTypesCall?.data)
        .filter(d => d.b.kind === "DATA_TYPE")
        .map(d => d.b.id)
        .value();

    $: viewHierarchy = buildHierarchies(reduceToSelectedNodesOnly(dataTypes, selected));

    $: selectionFilter = (d) => ! _.includes(selected, d.id);
    $: dataTypes = $dataTypesCall.data;

    $: user = $userCall?.data;

    $: requiredRole = getEditRoleForEntityKind(primaryEntityReference.kind, entity.DATA_TYPE.key);
    $: hasEditRoleForKind = _.includes(user?.roles, requiredRole);

    $: canEdit = !_.includes(restrictedEntities, primaryEntityReference.kind) || hasEditRoleForKind;

</script>

{#if mode === Modes.VIEW}

    <h4>Associated Data Types <small>View</small></h4>
    <div class="help-block">
        The tree below shows the set of data types which have been related to this entity.
    </div>

    {#if viewHierarchy.length === 0}
        <NoData>
            There are <strong>no data types</strong> associated with this entity
        </NoData>
    {:else}
        <div class="row">
            <div class="col-md-7">
                <DataTypeTreeSelector multiSelect={true}
                                      nonConcreteSelectable={false}
                                      selectionFilter={selectionFilter}
                                      dataTypeIds={selected}
                                      on:select={onSelect}/>
            </div>
            <div class="col-md-5">
                {#if focusedType}
                    <SubSection>
                        <div slot="header">
                            <span>{focusedType.name}</span>
                        </div>
                        <div slot="content">
                            <div class="focused-type-content">
                                <Markdown text={focusedType.description}/>
                            </div>
                        </div>
                        <div slot="controls">
                            <div class="focused-type-controls">
                                More about: <EntityLink ref={focusedType}/>
                            </div>
                        </div>
                    </SubSection>
                {/if}
            </div>
        </div>

    {/if}

    {#if canEdit}
        <div class="actions">
            <button class="btn btn-primary btn-sm"
                    on:click={() => mode = Modes.EDIT}>
                Edit
            </button>
        </div>
    {/if}

{:else if mode === Modes.EDIT}

    <h4>Associated Data Types <small>Edit</small></h4>

    <div class="help-block">
        Use the tree below to edit the set of data types which have been related to this entity.
        As you toggle, the data will be automatically saved.
    </div>

    <DataTypeTreeSelector multiSelect={true}
                          nonConcreteSelectable={false}
                          selectionFilter={selectionFilter}
                          on:select={onSelect}/>

    <div class="actions">
        <button class="btn-skinny"
                on:click={() => mode = Modes.VIEW}>
            &laquo; Back to view
        </button>
    </div>
{/if}


<style>
    .actions {
        padding-top: 1em;
    }

    .focused-type-content {
        padding-left: 1em;
    }

    .focused-type-controls {
        float: right;
        padding-right: 1em;
    }
</style>