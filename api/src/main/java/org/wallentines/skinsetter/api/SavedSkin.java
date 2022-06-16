package org.wallentines.skinsetter.api;

import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.Collection;

public interface SavedSkin {

    /**
     * Retrieves the skin's ID
     *
     * @return The skin's ID
     */
    String getId();

    /**
     * Retrieves the internal skin object
     *
     * @return The skin object
     */
    Skin getSkin();

    /**
     * Retrieves the display name for the skin
     *
     * @return The skin's display name
     */
    MComponent getName();

    /**
     * Retrieves whether the skin will be excluded in random selections
     *
     * @return Whether the skin is excluded in random selections
     */
    boolean excludedFromRandom();

    /**
     * Retrieves the display item for the skin that will appear in GUIs
     *
     * @return The display item
     */
    MItemStack getDisplayItem();

    /**
     * Retrieves a head item with the skin applied to it
     *
     * @return A head item
     */
    MItemStack getHeadItem();

    /**
     * Determines whether the head's item was auto-generated or customized in the config
     *
     * @return Whether the item is custom or not
     */
    boolean hasCustomItem();

    /**
     * Retrieves a collection of all the groups the skin is a part of
     *
     * @return The list of groups
     */
    Collection<String> getGroups();

    /**
     * Determines whether the skin has a particular group
     *
     * @param group The group to query
     * @return      Whether the skin has the group
     */
    boolean hasGroup(String group);

    /**
     * Determines whether a player has permission to use this skin
     *
     * @param player The player to query
     * @return       Whether the player can use the skin or not
     */
    boolean canUse(MPlayer player);

}
