<script>
    import _ from "lodash";
    import StepHeader from "./StepHeader.svelte";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import EnumSelect from "./EnumSelect.svelte";

    export let primaryEntityRef;

    let done = false;
    let workingCopy = {};
    let transportKinds = [];

   let enumsCall = enumValueStore.load();

   function toOptions(enumsByType, kind) {
       return _
           .chain(enumsByType)
           .get([kind], [])
           .orderBy([d => d.position, d => d.name])
           .value();
   }

    $: enumsByType = _.groupBy($enumsCall.data, d => d.type);

    $: transportKinds = toOptions(enumsByType, "TransportKind");
    $: frequencyKinds = toOptions(enumsByType, "Frequency");
    $: physicalFlowCriticalityKinds = toOptions(enumsByType, "physicalFlowCriticality");

    $: console.log({d: $enumsCall.data, enumsByType, transportKinds})

    function save() {
        console.log("Save ?")
    }
</script>

<StepHeader label="Delivery Characteristics"
            icon="envelope-o"
            checked={done}/>

<div class="step-body">

    <pre>{JSON.stringify(workingCopy, 2, "")}</pre>
    <form autocomplete="off"
          on:submit|preventDefault={save}>

        <!-- Basis Offset, Criticality, -->

        <EnumSelect options={transportKinds}
                    bind:value={workingCopy.transport}
                    mandatory="true"
                    name="Transport">
            <div slot="help">
                Describes how that data is transferred between the source and target.
            </div>
        </EnumSelect>


        <EnumSelect options={frequencyKinds}
                    bind:value={workingCopy.frequencyKind}
                    mandatory="true"
                    name="Frequency">
            <div slot="help">
                Describes how often (on average) the data is transferred between the source and target.
            </div>
        </EnumSelect>

        <EnumSelect options={physicalFlowCriticalityKinds}
                    bind:value={workingCopy.physicalFlowCriticality}
                    mandatory="true"
                    name="Criticality">
            <div slot="help">
                How important ?!
            </div>
        </EnumSelect>

    </form>

</div>


<style>
    .step-body {
        padding-left: 1em;
    }
</style>