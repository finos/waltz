<script>

    let RandomStack = [
        {
            "name": "Random",
            "items": ["React", "Angular", "Solid", "Rails", "Express", "Flask", "VPS"]
        },
        {
            "name": "Frontend Stack",
            "items": ["Svelte", "SvelteKit"]
        },
        {
            "name": "Backend Stack",
            "items": ["Django"]
        },
        {
            "name": "Server Stack",
            "items": ["Vercel"]
        }
    ];

    function dragStart(evt, idx, itemIdx) {
        console.log("dragStart", {evt, idx, itemIdx});
    }

    function drop(evt, idx) {
        evt.preventDefault();
        console.log("drop", {evt, idx});
    }

</script>


<h4>Drag and Drop</h4>
{#each RandomStack as stack, idx (stack)}
    <ul class="list-inline">
        {#each stack.items as item, itemIndex (item)}
            <li draggable={true}
                on:drop={(event) => drop(event, idx, itemIndex)}
                on:dragenter={() => console.log("enter", {stack, idx, itemIndex})}
                on:dragleave={() => console.log("leave", {stack, idx, itemIndex})}
                on:dragstart={event => dragStart(event, idx, itemIndex)}
                ondragover="return false">
                {item}
            </li>
        {/each}
    </ul>
{/each}