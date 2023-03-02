<script>

import _ from "lodash";
import EntitySearchSelector from "../../../../../common/svelte/EntitySearchSelector.svelte";
import Toggle from "../../../../../common/svelte/Toggle.svelte";
import {model, RenderModes, renderModeStore} from "../builderStore";
import LeaderControl from "./LeaderControl.svelte";
import Icon from "../../../../../common/svelte/Icon.svelte";

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
    model.addGroup("Hello Group" + Math.random())
}

function onAddUnit(group) {
    model.addUnit(group.groupId, "Hello Unit " + Math.random())
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
    <LeaderControl on:update={(evt) => model.setLeader(evt.detail)}/>
    <div class="help-block">
        The leader is a person and title to include at the head of the diagram
    </div>

    <ul class="group-header header-list">
        <li>
            <Icon name="cubes"/>
            Groups
            <button class="btn-skinny" on:click={() => onAddGroup()}>
                <Icon name="plus"/>
                Add
            </button>

            <ul class="group-list data-list">
            {#each $model.groups as group}
                <li>
                    <button class="btn-skinny">{group.name}</button>
                    <button class="btn-skinny" on:click={() => onRemoveGroup(group)}>
                        <Icon name="trash"/>
                    </button>
                    <ul class="unit-header header-list">
                        <li>
                            <Icon name="cube"/>
                            Units
                            <button class="btn-skinny" on:click={() => onAddUnit(group)}>
                                <Icon name="plus"/>
                                Add
                            </button>
                            <ul class="unit-list data-list">
                            {#each _.filter($model.units, u => u.groupId === group.groupId) as unit}
                                <li>
                                    {unit.name}
                                    <button class="btn-skinny" on:click={() => onRemoveUnit(unit)}>
                                        <Icon name="trash"/>
                                    </button>
                                </li>
                                <ul class="people-header header-list">
                                    <li>
                                        <Icon name="users"/>
                                        People
                                        <Icon name="plus"/>
                                        Add
                                    </li>
                                    <ul class="people-list data-list">
                                    {#each _.filter($model.people, p => p.unitId === unit.unitId) as person}
                                        <li>
                                            {person.person.name}
                                            <button class="btn-skinny" on:click={() => onRemovePerson(person)}>
                                                <Icon name="trash"/>
                                            </button>
                                        </li>
                                    {/each}
                                    </ul>
                                </ul>
                            {/each}
                            </ul>
                        </li>
                    </ul>
                </li>
            {/each}
            </ul>
        </li>
    </ul>
</div>


<style>
    li {
        list-style-type: none;
    }

    .header-list {
        padding-left: 0.5em;
        border-left: 1px dotted #ddd;
    }

    .data-list {
        padding-left: 2em;
    }

    .data-list>li {
        list-style-type: circle;
    }

    .header-list>li {
        list-style-type: none;
    }
</style>