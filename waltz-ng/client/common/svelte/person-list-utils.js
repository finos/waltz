/**
 * Determines whether a user (self) can remove another person from a list.
 * The options object controls the behaviour and looks like:
 * ```
 *   {
 *     canRemove: bool, // can the user remove anyone ?
 *     canRemoveSelf: bool, // can the user remove themselves ?
 *   }
 * ```
 * @param person - person being checked to see if they can be removed
 * @param self - who is the person doing the removal
 * @param options - what are the broad capabilities (`{canRemove, canRemoveSelf}`)
 * @returns boolean  - true iff the person may be removed
 */
export function mayRemove(person,
                          self,
                          options = { canRemove: false, canRemoveSelf: false}) {

    if (options.canRemove) {
        // user is broadly allowed to remove other people
        if (options.canRemoveSelf) {
            // they can even remove themselves, therefore they can definitely remove this person
            return true;
        } else {
            // however they can only remove other people, not themselves
            return self && self.id !== person.id;
        }
    } else {
        // user cannot remove anyone
        return false;
    }
}