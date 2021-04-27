<script>

    import UsageEdit from "./UsageEdit.svelte";
    import EnvironmentRemovalConfirmation from "./EnvironmentRemovalConfirmation.svelte";
    import UsageTree from "./UsageTree.svelte";
    import UsageTable from "./UsageTable.svelte";
    import {customEnvironmentStore} from "../../../svelte-stores/custom-environment-store";
    import MiniActions from "../../../common/svelte/MiniActions.svelte";

    export let doCancel;
    export let application
    export let usages;
    export let environment;

    const showTableAction = {name: "Show table", icon: "table", description: "Click to switch to table view", handleAction: showTable}
    const showTreeAction = {name: "Show tree", icon: "sitemap", description: "Click to switch to tree view", handleAction: showTree}
    const editAction = {name: "Edit", icon: "pencil", description: "Click to edit environment", handleAction: showEdit}
    const removeAction = {name: "Remove",
        icon: "trash",
        description: "Click to delete this environment and its associations to servers and databases",
        handleAction: showRemove}
    const cancelEditAction = {name: "Cancel", icon: "times", description: "Cancel editing and return to tree view", handleAction: showTree}
    const cancelRemoveAction = {name: "Cancel", icon: "times", description: "Cancel this removal and return to tree view", handleAction: showTree}

    const modeActions = {
        TREE: [showTableAction, editAction, removeAction],
        TABLE: [showTreeAction, editAction, removeAction],
        EDIT: [],
        REMOVE: []
    }

    const Modes = {
        TREE: "TREE",
        TABLE: "TABLE",
        EDIT: "EDIT",
        REMOVE: "REMOVE"
    }


    let activeMode = Modes.TREE;
    let selectedAsset

    function cancel() {
        doCancel();
    }

    function showTable() {
        return activeMode = Modes.TABLE
    }

    function showTree() {
        return activeMode = Modes.TREE
    }

    function showRemove() {
        return activeMode = Modes.REMOVE
    }

    function showEdit() {
        return activeMode = Modes.EDIT;
    }

    function doRemove(environment) {
        return customEnvironmentStore.remove(environment);
    }

</script>

<!--TREEMODE-->
{#if usages.length === 0 && activeMode !== Modes.EDIT && activeMode !== Modes.REMOVE}
    <div>No databases or servers have been associated to this environment.</div>
{:else if activeMode === Modes.TREE}
    <UsageTree usages={usages}/>
{:else if activeMode === Modes.TABLE}
    <UsageTable {usages}/>
{:else if activeMode === Modes.EDIT}
    <UsageEdit {environment}
               usages={usages || []}
               {application}
               doCancel={showTree}/>
{:else if activeMode === Modes.REMOVE}
    <EnvironmentRemovalConfirmation {environment}
                                    {doRemove}
                                    doCancel={showTree}/>
{/if}
<br>
<MiniActions actions={modeActions[activeMode]}/>

