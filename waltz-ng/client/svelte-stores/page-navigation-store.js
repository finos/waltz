import {writable} from "svelte/store";

/**
 * Expects object looking like:
 *  {
 *      state: ''
 *      params: {}
 *      options: ''
 *  }
 * @type {Writable<null>}
 */
const pageInfo = writable(null);


export default pageInfo;