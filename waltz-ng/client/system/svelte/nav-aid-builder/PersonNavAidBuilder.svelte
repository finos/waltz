<script>
    import {mkRef} from "../../../common/entity-utils";
    import GroupLeader from "./GroupLeader.svelte";
    import Group from "./Group.svelte";

    let boss = mkRef('PERSON', 23, "Big B. Boss");
    let gil = mkRef('PERSON', 24, "Gil Perez");
    let gordon = mkRef('PERSON', 25, "Gordon Mackechnie");
    let stefan = mkRef('PERSON', 26, "Stefan Peschke");
    let marcus = mkRef('PERSON', 27, "Marcus Heidmann");
    let marie = mkRef('PERSON', 28, "Marie Lauri");

    const struct = {
        leader: {
            person: boss,
            title: "Chief Technology, Data & Innovation Officer"
        },
        groups: [
            {
                name: "Functions",
                units: [
                    {
                        name: "Cloud and Innovation Network",
                        people: [
                            {
                                person: gil,
                                title: "Chief Information Officer"
                            }
                        ]
                    }, {
                        name: "Chief Technology Office",
                        people: [
                            {
                                person: gordon,
                                title: "Chief Technology Officer"
                            }
                        ]
                    }, {
                        name: "Chief Risk Office",
                        people: [
                            {
                                person: marie,
                                title: "Chief Risk Officer"
                            }
                        ]
                    }
                ]
            },
            {
                name: "Chief Information Officers",
                units: [
                    {
                        name: "Private Bank",
                        people: [
                            {
                                person: marcus,
                                title: "Co Chief Information Officer - Private Bank"
                            }, {
                                person: stefan,
                                title: "Co Chief Information Officer - Private Bank"
                            }
                        ]
                    }, {
                        name: "Corporate Bank",
                        people: [
                            {
                                person: marcus,
                                title: "Co Chief Information Officer - Private Bank"
                            }, {
                                person: stefan,
                                title: "Co Chief Information Officer - Private Bank"
                            }
                        ]
                    }
                ]
            }
        ]
    }

</script>

<div class="navaid-diagram">
    <GroupLeader {...struct.leader}
                 scheme="primary"/>
    <div class="navaid-groups">
        {#each struct.groups as group}
            <Group {group}/>
        {/each}
    </div>

</div>

<style>
    .navaid-groups {
        display: flex;
        flex-direction: column;
        justify-content: space-evenly;
    }
</style>