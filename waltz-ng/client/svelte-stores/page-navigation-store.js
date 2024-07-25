import {writable} from "svelte/store";

/**
 * If you push an item into this store the browser will navigate to the
 * corresponding page.
 *
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