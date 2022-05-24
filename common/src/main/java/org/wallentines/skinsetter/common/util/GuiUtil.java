package org.wallentines.skinsetter.common.util;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.Collection;
import java.util.function.Consumer;

public class GuiUtil {

    public static void openGUI(MPlayer player, LangProvider provider, Collection<SavedSkin> skins, Consumer<SavedSkin> out) {

        InventoryGUI gui = MidnightCoreAPI.getInstance().createGUI(provider.getMessage("gui.set.title", player));

        // Check if multiple pages will be necessary
        int pageSize = 54;
        if(skins.size() > 54) {
            pageSize = 45;
        }
        int pages = skins.size() / pageSize + 1;

        MItemStack nextItem = MItemStack.Builder.of(new Identifier("minecraft", "lime_stained_glass_pane")).withName(provider.getMessage("gui.next_page", player)).build();
        MItemStack prevItem = MItemStack.Builder.of(new Identifier("minecraft", "red_stained_glass_pane")).withName(provider.getMessage("gui.prev_page", player)).build();

        int index = 0;
        int currentPage = 0;
        for(SavedSkin sk : skins) {
            gui.setItem(index, sk.getDisplayItem(), (type, pl) -> out.accept(sk));
            index++;
            if(index > pageSize) {

                int offset = 54 * currentPage;
                if(currentPage != 0) {
                    final int prevPage = currentPage - 1;
                    gui.setItem(45 + offset, prevItem.copy(), (type, pl) -> gui.open(pl, prevPage));
                }
                index = (index % pageSize) + offset;
                currentPage++;

                if(currentPage != pages) {
                    final int nextPage = currentPage;
                    gui.setItem(53 + offset, nextItem.copy(), (type, pl) -> gui.open(pl, nextPage));
                }
            }
        }

        gui.open(player, 0);

    }

}
