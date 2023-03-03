<script>

    import Toggle from "../../../../../common/svelte/Toggle.svelte";
    import {model, RenderModes, renderModeStore} from "../builderStore";
    import PersonControl from "./PersonControl.svelte";
    import NavTree from "./NavTree.svelte";

    function onRemoveGroup(group) {
        model.removeGroup(group.groupId);
    }

    function onRemoveUnit(unit) {
        model.removeUnit(unit.unitId);
    }

    function onRemovePerson(person) {
        model.removePerson(person.personId);
    }

    function onAddGroup() {
        model.addGroup("Hello Group" + Math.random());
    }

    function onAddUnit(group) {
        model.addUnit(group.groupId, "Hello Unit " + Math.random());
    }

    function onAddLeader() {
        model.addLeader();
    }

    function onRemoveLeader(person) {
        model.removeLeader(person.personId);
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



    <NavTree on:addGroup={() => onAddGroup()}
             on:removeGroup={(evt) => onRemoveGroup(evt.detail)}
             on:addUnit={(evt) => onAddUnit(evt.detail)}
             on:removeUnit={(evt) => onRemoveUnit(evt.detail)}
             on:addLeader={(evt) => onAddLeader(evt.detail)}
             on:removeLeader={(evt) => onRemoveLeader(evt.detail)}
             on:addPerson={(evt) => onAddPerson(evt.detail)}
             on:removePerson={(evt) => onRemovePerson(evt.detail)}/>
</div>


<style>

</style>