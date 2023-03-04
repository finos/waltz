<script>
    import _ from "lodash";
    import Icon from "../../../../../common/svelte/Icon.svelte";
    import {model} from "../builderStore";
    import {createEventDispatcher} from "svelte";

    /**
     * Events  (leader, group, unit, person)
     * -------
     *
     * addLeader
     * addGroup
     * addUnit
     * addPerson
     *
     * editLeader
     * editGroup
     * editUnit
     * editPerson
     *
     * removeLeader
     * removeGroup
     * removeUnit
     * removePerson
     */

    const dispatch = createEventDispatcher();

</script>

<ul class="header-list">
    <li>
        <span class="section-label">Leaders</span>
        <button class="btn-skinny"
                on:click={() => dispatch("addLeader")}>
            <Icon name="plus"/>
            Add
        </button>

        <ul>
            {#each $model.leaders as leader}
                <li>
                    <Icon name="user"/>
                    <button class="btn-skinny"
                            on:click={() => dispatch("editLeader", leader)}>
                        {leader.person.name}
                    </button>
                    <button class="btn-skinny"
                            on:click={() => dispatch("removeLeader", leader)}>
                        <Icon name="trash"/>
                    </button>
                </li>
            {/each}
        </ul>
    </li>
    <li>
        <span class="section-label">Groups</span>
        <button class="btn-skinny"
                on:click={() => dispatch("addGroup")}>
            <Icon name="plus"/>
            Add
        </button>

        <ul class="group-list data-list">
            {#each $model.groups as group}
                <li>
                    <Icon name="cubes"/>
                    <button class="btn-skinny"
                            on:click={() => dispatch("editGroup", group)}>
                        {group.name}
                    </button>
                    <button class="btn-skinny"
                            on:click={() => dispatch("removeGroup", group)}>
                        <Icon name="trash"/>
                    </button>
                    <ul class="header-list">
                        <li>
                            <span class="section-label">Units</span>
                            <button class="btn-skinny"
                                    on:click={() => dispatch("addUnit", group)}>
                                <Icon name="plus"/>
                                Add
                            </button>
                            <ul class="unit-list data-list">
                                {#each _.filter($model.units, u => u.groupId === group.groupId) as unit}
                                    <li>
                                        <Icon name="cube"/>
                                        <button class="btn-skinny"
                                                on:click={() => dispatch("editUnit", unit)}>
                                            {unit.name}
                                        </button>
                                        <button class="btn-skinny"
                                                on:click={() => dispatch("removeUnit", unit)}>
                                            <Icon name="trash"/>
                                        </button>
                                    </li>
                                    <ul class="header-list">
                                        <li>
                                            <span class="section-label">People</span>
                                            <Icon name="plus"/>
                                            Add
                                        </li>
                                        <ul class="people-list data-list">
                                            {#each _.filter($model.people, p => p.unitId === unit.unitId) as person}
                                                <li>
                                                    <Icon name="user"/>
                                                    <button class="btn-skinny"
                                                            on:click={() => dispatch("editPerson", person)}>
                                                        {person.person.name}
                                                    </button>
                                                    <button class="btn-skinny"
                                                            on:click={() => dispatch("removePerson", person)}>
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

    .section-label {
        color: #888;
        font-weight: bolder;
        font-style: italic;
    }
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