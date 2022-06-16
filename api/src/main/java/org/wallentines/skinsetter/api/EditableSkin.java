package org.wallentines.skinsetter.api;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;

public interface EditableSkin extends SavedSkin {

    /**
     * Changes the display name of the skin
     *
     * @param name The new display name
     */
    void setName(MComponent name);


    /**
     * Changes whether the skin will be excluded in random selections
     *
     * @param exclude Whether the skin should be excluded in random selections
     */
    void excludeFromRandom(boolean exclude);

    /**
     * Changes the display item for the skin that will appear in GUIs
     *
     * @param item The new item
     */
    void setDisplayItem(MItemStack item);

    /**
     * Adds a group to the skin
     *
     * @param group The group to add
     */
    void addGroup(String group);

    /**
     * Removes a group from the skin
     *
     * @param group The skin to remove
     */
    void removeGroup(String group);

    /**
     * Removes all groups from the skin
     */
    void clearGroups();

    /**
     * Saves the edits made to this skin to the registry
     */
    void save();

}
