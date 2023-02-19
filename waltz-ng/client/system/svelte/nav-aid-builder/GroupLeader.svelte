<script>
    import EntityLabel from "../../../common/svelte/EntityLabel.svelte";

    export let person = null;
    export let title = "";

    export let scheme = "secondary";
</script>

<a href="person/id/{person.id}">

<div class="person-card"
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
    .person-card {
        text-align: center;
    }

    .banner {
        padding: 0.5em;
    }

    .secondary .banner {
        background-color: #5178e7;
        color: #eee;
    }

    .primary .banner {
        background-color: #2956e0;
        color: #eee;
    }
</style>

