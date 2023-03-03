<script>
    import _ from "lodash";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import {model} from "../builderStore";
    import {createEventDispatcher} from "svelte";

    const dispatch = createEventDispatcher();

</script>

<ul class="header-list">
    <li>
        <Icon name="user"/>
        Leaders
        <button class="btn-skinny" on:click={() => dispatch("addLeader")}>
            <Icon name="plus"/>
            Add
        </button>
        <ul>
            {#each $model.leaders as leader}
                <li>
                    {leader.person.name} ({leader.personId})
                    <button class="btn-skinny" on:click={() => dispatch("removeLeader", leader)}>
                        <Icon name="trash"/>
                    </button>
                </li>
            {/each}
        </ul>
    </li>
    <li>
        <Icon name="cubes"/>
        Groups
        <button class="btn-skinny" on:click={() => dispatch("addGroup")}>
            <Icon name="plus"/>
            Add
        </button>

        <ul class="group-list data-list">
            {#each $model.groups as group}
                <li>
                    <button class="btn-skinny">{group.name}</button>
                    <button class="btn-skinny" on:click={() => dispatch("removeGroup", group)}>
                        <Icon name="trash"/>
                    </button>
                    <ul class="header-list">
                        <li>
                            <Icon name="cube"/>
                            Units
                            <button class="btn-skinny" on:click={() => dispatch("addUnit", group)}>
                                <Icon name="plus"/>
                                Add
                            </button>
                            <ul class="unit-list data-list">
                                {#each _.filter($model.units, u => u.groupId === group.groupId) as unit}
                                    <li>
                                        {unit.name}
                                        <button class="btn-skinny" on:click={() => dispatch("removeUnit", unit)}>
                                            <Icon name="trash"/>
                                        </button>
                                    </li>
                                    <ul class="header-list">
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
                                                    <button class="btn-skinny" on:click={() => dispatch("removePerson", person)}>
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