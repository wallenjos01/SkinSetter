package org.wallentines.skinsetter.common.util;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.skinsetter.api.SavedSkin;

import java.util.Collection;
import java.util.function.Consumer;

public class GuiUtil {

    public static void openGUI(MPlayer player, LangProvider provider, Collection<SavedSkin> skins, Consumer<SavedSkin> out) {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) throw new IllegalStateException("Attempt to open GUI before MidnightCoreAPI is loaded!");

        InventoryGUI gui = api.createGUI(provider.getMessage("gui.set.title", player));

        // Check if multiple pages will be necessary
        int pageSize = 54;
        if(skins.size() > 54) {
            pageSize = 45;
        }
        int pages = (int) Math.ceil(skins.size() / (float) pageSize);

        MItemStack nextItem = MItemStack.Builder.of(new Identifier("minecraft", "lime_stained_glass_pane")).withName(provider.getMessage("gui.next_page", player)).build();
        MItemStack prevItem = MItemStack.Builder.of(new Identifier("minecraft", "red_stained_glass_pane")).withName(provider.getMessage("gui.prev_page", player)).build();

        int index = 0;
        for(SavedSkin sk : skins) {

            int slot = (index % pageSize) + (54 * (index / pageSize));
            gui.setItem(slot, sk.getDisplayItem(), (type, pl) -> {
                gui.close(pl);
                out.accept(sk);
            });

            index++;
        }

        for(int i = 0 ; i < pages ; i++) {

            if(i > 0) {

                int slot = (54 * i) + 45;
                int prevPage = i - 1;
                gui.setItem(slot, prevItem.copy(), (type, pl) -> gui.open(pl, prevPage));
            }
            if(i < pages - 1) {

                int slot = (54 * i) + 53;
                int nextPage = i + 1;
                gui.setItem(slot, nextItem.copy(), (type, pl) -> gui.open(pl, nextPage));
            }
        }

        gui.open(player, 0);

    }

}
