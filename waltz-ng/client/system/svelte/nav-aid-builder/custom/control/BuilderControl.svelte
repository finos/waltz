<script>
    import {model} from "../builderStore";
    import NavTree from "./NavTree.svelte";
    import RemovalConfirmation from "./RemovalConfirmation.svelte";
    import BlockEditor from "./BlockEditor.svelte";
    import PersonEditor from "./PersonEditor.svelte";

    const Modes = {
        VIEW: "VIEW",
        EDIT_BLOCK: "EDIT_BLOCK",  // unit or group
        EDIT_PERSON: "EDIT_PERSON",
        REMOVAL_CONFIRMATION: "REMOVAL_CONFIRMATION"
    };

    let mode = Modes.VIEW;
    let params = null;

    function showNavView() {
        mode = Modes.VIEW;
        params = null;
    }

    function showRemovalConfirmation(d) {
        params = d;
        mode = Modes.REMOVAL_CONFIRMATION;
    }

    /** Used for both units and groups **/
    function showBlockEditor(d) {
        params = d;
        mode = Modes.EDIT_BLOCK;
    }

    function showPersonEditor(d) {
        params = d;
        mode = Modes.EDIT_PERSON;
    }

    function onCancel() {
        showNavView();
    }

    function onConfirm(evt) {
        const givenParams = evt.detail;
        givenParams.action(givenParams.data);
        showNavView();
    }


    // -- REMOVALS ------

    function onRemoveLeader(person) {
        showRemovalConfirmation({
            message: `Remove Leader: ${person.name}`,
            data: person,
            action: () => model.removeLeader(person.personId)
        });
    }

    function onRemoveGroup(group) {
        showRemovalConfirmation({
            message: `Remove Group: ${group.name}?`,
            data: group,
            action: () => model.removeGroup(group.groupId)
        });
    }

    function onRemoveUnit(unit) {
        showRemovalConfirmation({
            message: `Remove Unit: ${unit.name}?`,
            data: unit,
            action: () => model.removeUnit(unit.unitId)
        });
    }

    function onRemovePerson(person) {
        showRemovalConfirmation({
            message: `Remove Person: ${person.name}?`,
            data: person,
            action: () => model.removePerson(person.personId)
        });
    }


    // --- EDITS -----

    function onEditGroup(group) {
        showBlockEditor({
            message: `Edit group: ${group.name}`,
            data: Object.assign({}, group),
            action: (group) => model.updateGroup(group)
        });
    }

    function onEditUnit(unit) {
        showBlockEditor({
            message: `Edit unit: ${unit.name}`,
            data: Object.assign({}, unit),
            action: (d) => model.updateUnit(d)
        });
    }

    function onEditPerson(person) {
        showPersonEditor({
            message: `Edit Person: ${person.title}`,
            data: Object.assign({}, person),
            action: (d) => model.updatePerson(d)
        });
    }

    function onEditLeader(person) {
        showPersonEditor({
            message: `Edit Leader: ${person.title}`,
            data: Object.assign({}, person),
            action: (d) => model.updateLeader(d)
        });
    }

    // --- ADDITIONS -----

    function onAddGroup() {
        showBlockEditor({
            message: "Add Group",
            data: {
                name: "TBC"
            },
            action: (d) => model.addGroup(d.name)
        });
    }

    function onAddUnit(group) {
        model.addUnit(group.groupId, "Hello Unit " + Math.random());
    }

    function onAddLeader() {
        showPersonEditor({
            message: `Add Leader`,
            data: {title: "TBC", person: null},
            action: (d) => model.addLeader(d)
        });
    }

    function onAddPerson(unit) {
        showPersonEditor({
            message: `Add person to ${unit.name}`,
            data: {title: "TBC", person: null, unitId: unit.unitId},
            action: (d) => model.addPerson(d)
        });
    }

</script>



<div>
    {#if mode === Modes.VIEW}
        <NavTree on:addGroup={() => onAddGroup()}
                 on:addUnit={(evt) => onAddUnit(evt.detail)}
                 on:addLeader={(evt) => onAddLeader(evt.detail)}
                 on:addPerson={(evt) => onAddPerson(evt.detail)}
                 on:removeGroup={(evt) => onRemoveGroup(evt.detail)}
                 on:removeUnit={(evt) => onRemoveUnit(evt.detail)}
                 on:removeLeader={(evt) => onRemoveLeader(evt.detail)}
                 on:removePerson={(evt) => onRemovePerson(evt.detail)}
                 on:editGroup={(evt) => onEditGroup(evt.detail)}
                 on:editPerson={(evt) => onEditPerson(evt.detail)}
                 on:editLeader={(evt) => onEditLeader(evt.detail)}
                 on:editUnit={(evt) => onEditUnit(evt.detail)}/>

    {:else if mode === Modes.REMOVAL_CONFIRMATION}
        <RemovalConfirmation {params}
                             on:cancel={onCancel}
                             on:confirm={onConfirm}/>

    {:else if mode === Modes.EDIT_BLOCK}
        <BlockEditor {params}
                     on:cancel={onCancel}
                     on:confirm={onConfirm}/>

    {:else if mode === Modes.EDIT_PERSON}
        <PersonEditor {params}
                      on:cancel={onCancel}
                      on:confirm={onConfirm}/>

    {/if}
</div>


<style>

</style>