<script>
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";

    export let person = null;
    export let title = "";

    export let scheme = "secondary";
</script>

<a href="person/id/{person.id}">

    <div class="navaid-leader"
         class:primary={scheme === 'primary'}
         class:secondary={scheme === 'secondary'}>
        <div class="banner">
            <div>
                {title}
            </div>
            <div>
                <EntityLabel ref={person}
                             showIcon={false}/>
            </div>
        </div>

    </div>
</a>
<style>
    .navaid-leader {
        text-align: center;
    }

    .navaid-leader .banner {
        padding: 0.5em;
    }

    .navaid-leader.secondary .banner {
        background-color: #5178e7;
        color: #eee;
    }

    .navaid-leader.primary .banner {
        background-color: #2956e0;
        color: #eee;
    }
</style>

