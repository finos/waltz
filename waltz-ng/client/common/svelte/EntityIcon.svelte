<script>
    import _ from "lodash";
    import {entity} from "../services/enums/entity";

    import Icon from "./Icon.svelte";
    export let kind;

    $: name = _.get(entity, [kind, "icon"], "circle-o");


</script>

<style>
</style>

<Icon {name}/>
