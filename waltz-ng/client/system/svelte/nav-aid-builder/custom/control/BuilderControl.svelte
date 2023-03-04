<script>

    import Toggle from "../../../../../common/svelte/Toggle.svelte";
    import {model, RenderModes, renderModeStore} from "../builderStore";
    import PersonControl from "./PersonControl.svelte";
    import NavTree from "./NavTree.svelte";
    import RemovalConfirmation from "./RemovalConfirmation.svelte";

    const Modes = {
        VIEW: "VIEW",
        EDIT_PERSON: "EDIT_PERSON",
        EDIT_UNIT: "EDIT_UNIT",
        EDIT_GROUP: "EDIT_GROUP",
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

    function onCancel() {
        showNavView();
    }

    function onConfirm(evt) {
        const givenParams = evt.detail;
        givenParams.action();
        showNavView();
    }

    // ----

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

    function onAddGroup() {
        model.addGroup("Hello Group" + Math.random());
    }

    function onAddUnit(group) {
        model.addUnit(group.groupId, "Hello Unit " + Math.random());
    }

    function onAddLeader() {
        model.addLeader({title: "hello", person: {id: 1323, name: "Bob"}});
    }

    function onAddPerson(unit) {

    }

</script>


<label for="foo">
    Render Mode
</label>
<Toggle id="foo"
        state={$renderModeStore === RenderModes.LIVE}
        labelOn="Live Mode"
        labelOff="Dev Mode"
        onToggle={() => renderModeStore.toggle()}/>
<div class="help-block">
    Render mode determines whether to have clickable
    regions being actual links, or used to focus
</div>

<div>

    <h4>Leader</h4>
    <PersonControl on:update={(evt) => model.setLeader(evt.detail)}/>
    <div class="help-block">
        The leader is a person and title to include at the head of the diagram
    </div>


    {#if mode === Modes.VIEW}
        <NavTree on:addGroup={() => onAddGroup()}
                 on:removeGroup={(evt) => onRemoveGroup(evt.detail)}
                 on:addUnit={(evt) => onAddUnit(evt.detail)}
                 on:removeUnit={(evt) => onRemoveUnit(evt.detail)}
                 on:addLeader={(evt) => onAddLeader(evt.detail)}
                 on:removeLeader={(evt) => onRemoveLeader(evt.detail)}
                 on:addPerson={(evt) => onAddPerson(evt.detail)}
                 on:removePerson={(evt) => onRemovePerson(evt.detail)}/>

    {:else if mode === Modes.REMOVAL_CONFIRMATION}
        <RemovalConfirmation {params}
                             on:cancel={onCancel}
                             on:confirm={onConfirm}/>

    {/if}
</div>


<style>

</style>