package org.wallentines.skinsetter;

import org.wallentines.mcore.*;
import org.wallentines.mcore.lang.CustomPlaceholder;
import org.wallentines.mcore.lang.LangContent;
import org.wallentines.mcore.lang.LangManager;
import org.wallentines.mcore.skin.Skinnable;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ComponentResolver;
import org.wallentines.mcore.text.ConfigSerializer;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.mcore.util.MojangUtil;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SkinCommand {

    private static final Random RANDOM = new Random();

    public static int set(Collection<Skinnable> targets, String skinId, boolean forceOnline, BiFunction<String, Integer, Boolean> permissionSupplier, Consumer<Component> feedback) {

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        if(targets.isEmpty()) {
            feedback.accept(LangContent.component(lang, "error.no_entities"));
            return 0;
        }

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        SavedSkin skin = reg.getSkin(skinId);

        if(skin != null && !forceOnline && skin.getPermission() != null && !permissionSupplier.apply(skin.getPermission(), 2)) {
            feedback.accept(LangContent.component(lang, "error.skin_not_found", CustomPlaceholder.inline("skin_id", skinId)));
            return 0;
        }

        if(skin == null || forceOnline) {
            if(permissionSupplier.apply("skinsetter.command.set.online", 2)) {

                // Online
                MojangUtil.getSkinByNameAsync(skinId).thenAccept(downloaded -> {

                    if (downloaded == null) {
                        feedback.accept(LangContent.component(lang, "error.skin_not_found", CustomPlaceholder.inline("skin_id", skinId)));
                        return;
                    }

                    for(Skinnable sk : targets) {
                        sk.setSkin(downloaded);
                    }
                    sendMultiFeedback(targets, "command.set", feedback, CustomPlaceholder.inline("skin_id", skinId));
                });

                feedback.accept(LangContent.component(lang, "command.set.online", targets, CustomPlaceholder.inline("skin_id", skinId), CustomPlaceholder.inline("name", skinId)));
                return 2;
            }

            feedback.accept(LangContent.component(lang, "error.skin_not_found", CustomPlaceholder.inline("skin_id", skinId)));
            return 0;

        } else {

            for(Skinnable sk : targets) {
                sk.setSkin(skin.getSkin());
            }
            sendMultiFeedback(targets, "command.set", feedback, CustomPlaceholder.inline("skin_id", skinId));
            return targets.size();
        }

    }

    public static int setGUI(Player sender, Collection<Skinnable> targets, Consumer<Component> feedback) {


        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        if (targets.isEmpty()) {
            feedback.accept(LangContent.component(lang, "error.no_entities"));
            return 0;
        }

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        List<SavedSkin> sks = reg.getAllSkins(sender, null, SkinRegistry.ExcludeFlag.IN_GUI);

        if (reg.getSize() == 0) {
            feedback.accept(LangContent.component(lang, "error.no_skins_found"));
            return 0;
        }

        if (reg.getSize() <= 54) {
            // One-page GUI
            if (sks.isEmpty()) {
                feedback.accept(LangContent.component(lang, "error.no_skins_found"));
            }

            InventoryGUI gui = InventoryGUI.FACTORY.get().build(LangContent.component(lang, "gui.title",
                    CustomPlaceholder.inline("skins", sks.size())), 6);
            int index = 0;
            for (SavedSkin sk : sks) {
                gui.setItem(index++, sk.getDisplayItem(), (pl, ct) -> {
                    if (ct == InventoryGUI.ClickType.LEFT) {
                        for (Skinnable skb : targets) {
                            skb.setSkin(sk.getSkin());
                        }
                    }
                });
            }
            gui.open(sender);
            return 1;

        } else {
            // Multi-page GUI
            openPagedGUI(sks, 0, lang, sender, targets);
            return 2;
        }
    }

    private static void openPagedGUI(List<SavedSkin> sks, int page, LangManager lang, Player sender, Collection<Skinnable> target) {

        int skins = sks.size();
        int pages = (skins / 45) + (skins % 45 == 0 ? 0 : 1);
        if(page > pages || page < 0) {
            InventoryGUI.closeMenu(sender);
            return;
        }

        InventoryGUI gui = InventoryGUI.FACTORY.get().build(
                LangContent.component(lang, "gui.title.paged",
                        CustomPlaceholder.inline("page", page + 1),
                        CustomPlaceholder.inline("pages", pages + 1),
                        CustomPlaceholder.inline("start_skin", (page * 45) + 1),
                        CustomPlaceholder.inline("end_skin", (page * 45) + 45),
                        CustomPlaceholder.inline("skins", skins)),
                6);

        if(page < pages) {
            ItemStack nextItem = ItemStack.Builder.glassPaneWithColor(TextColor.GREEN)
                    .withName(ComponentResolver.resolveComponent(LangContent.component(lang, "gui.next_page"), sender))
                    .build();
            gui.setItem(53, nextItem, (pl, ck) -> {
                if(ck == InventoryGUI.ClickType.LEFT) {
                    openPagedGUI(sks, page + 1, lang, sender, target);
                }
            });
        }
        if(page > 0) {
            ItemStack prevItem = ItemStack.Builder.glassPaneWithColor(TextColor.RED)
                    .withName(ComponentResolver.resolveComponent(LangContent.component(lang, "gui.previous_page"), sender))
                    .build();
            gui.setItem(45, prevItem, (pl, ck) -> {
                if(ck == InventoryGUI.ClickType.LEFT) {
                    openPagedGUI(sks, page - 1, lang, sender, target);
                }
            });
        }

        for(int i = 0 ; i < 45 ; i++) {
            SavedSkin sk = sks.get(i + (page * 45));
            gui.setItem(i, sk.getDisplayItem(), (pl, ct) -> {
                if(ct == InventoryGUI.ClickType.LEFT) {
                    for(Skinnable skb : target) {
                        skb.setSkin(sk.getSkin());
                    }
                }
            });
        }

        gui.open(sender);
    }

    public static int reset(Collection<Skinnable> targets, Consumer<Component> feedback) {

        try {
            if (targets.isEmpty()) {
                feedback.accept(LangContent.component(SkinSetterServer.INSTANCE.get().getLangManager(), "error.no_entities"));
                return 0;
            }

            for (Skinnable skb : targets) {
                skb.resetSkin();
            }

            sendMultiFeedback(targets, "command.reset", feedback, null);
        } catch (Throwable th) {
            SkinSetterAPI.LOGGER.error("An error occurred while resetting a skin!", th);
        }
        return targets.size();
    }

    public static int save(Skinnable target, String id, String file, BiFunction<String, Integer, Boolean> permissionSupplier, Consumer<Component> feedback) {

        try {
            SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
            LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();

            if (reg.getSkin(id) != null && !permissionSupplier.apply("skinsetter.command.save.overwrite", 4)) {
                feedback.accept(LangContent.component(lang, "error.skin_exists", target, CustomPlaceholder.inline("skin_id", id)));
                return 0;
            }

            Skin skin = target.getSkin();

            SkinConfiguration config = new SkinConfiguration.Builder().build();
            reg.registerSkin(id, config.createSkin(skin), file);

            feedback.accept(LangContent.component(lang, "command.save", target, CustomPlaceholder.inline("skin_id", id)));
        } catch (Throwable th) {
            SkinSetterAPI.LOGGER.error("An error occurred while saving a skin!", th);
        }
        return 1;
    }

    public static int setRandom(Collection<Skinnable> targets, PermissionHolder permissionHolder, String group, Consumer<Component> feedback) {

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();

        List<SavedSkin> skins = reg.getAllSkins(permissionHolder, group, SkinRegistry.ExcludeFlag.IN_RANDOM);
        if(skins.isEmpty()) {
            feedback.accept(LangContent.component(lang, "error.no_skins_found"));
            return 0;
        }

        SavedSkin sk = skins.get(RANDOM.nextInt(skins.size()));
        for(Skinnable target : targets) {
            target.setSkin(sk.getSkin());
        }

        sendMultiFeedback(targets, "command.setrandom", feedback, null);
        return targets.size();
    }

    public static int item(Collection<Player> targets, String skinName, PermissionHolder permissionHolder, Consumer<Component> feedback) {

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();

        SavedSkin skin = reg.getSkin(skinName);

        if(skin == null || skin.getPermission() != null && !permissionHolder.hasPermission(skin.getPermission(), 2)) {
            feedback.accept(LangContent.component(lang, "error.skin_not_found", CustomPlaceholder.inline("skin_id", skinName)));
            return 0;
        }

        for(Player pl : targets) {
            pl.giveItem(skin.getDisplayItem());
        }

        sendMultiFeedback(targets, "command.item", feedback, CustomPlaceholder.inline("skin_id", skinName));
        return targets.size();
    }

    public static int persistence(boolean enabled, Consumer<Component> feedback) {

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        SkinSetterServer.INSTANCE.get().setPersistenceEnabled(enabled);

        feedback.accept(LangContent.component(lang, "command.persistence." + (enabled ? "enabled" : "disabled")));
        return 1;
    }

    public static int setDefault(String skinId, Consumer<Component> feedback) {

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();

        SavedSkin sk = getSkinOrFeedback(skinId, feedback);
        if(sk == null) return 0;

        SkinSetterServer.INSTANCE.get().setDefaultSkin(skinId);
        feedback.accept(LangContent.component(lang, "command.set_default", CustomPlaceholder.inline("skin_id", skinId)));
        return 1;
    }

    public static int clearDefault(Consumer<Component> feedback) {

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();

        SkinSetterServer.INSTANCE.get().setDefaultSkin(null);
        feedback.accept(LangContent.component(lang, "command.clear_default"));
        return 1;
    }

    public static int editName(String skinId, String name, Consumer<Component> feedback) {

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();

        SavedSkin sk = getSkinOrFeedback(skinId, feedback);
        if(sk == null) return 0;

        SerializeResult<Component> displayName = ConfigSerializer.INSTANCE.deserialize(ConfigContext.INSTANCE, new ConfigPrimitive(name));
        if(!displayName.isComplete()) {
            feedback.accept(LangContent.component(lang, "error.parse_component", CustomPlaceholder.inline("error", displayName.getError())));
            return 0;
        }

        Component finalName = displayName.getOrThrow();

        SkinConfiguration configuration = new SkinConfiguration.Builder(reg.getSavedConfiguration(name))
                .displayName(finalName)
                .build();

        reg.registerSkin(skinId, configuration.createSkin(sk.getSkin()));

        feedback.accept(LangContent.component(lang, "command.edit.name", CustomPlaceholder.inline("skin_id", skinId), CustomPlaceholder.of("name", finalName)));
        return 1;
    }

    public static int editPermission(String skinId, String permission, Consumer<Component> feedback) {

        SavedSkin sk = getSkinOrFeedback(skinId, feedback);
        if(sk == null) return 0;

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        SkinConfiguration configuration = new SkinConfiguration.Builder(reg.getSavedConfiguration(skinId))
                .permission(permission)
                .build();

        SkinSetterAPI.REGISTRY.get().registerSkin(skinId, configuration.createSkin(sk.getSkin()));

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        feedback.accept(LangContent.component(lang, "command.edit.permission", CustomPlaceholder.inline("skin_id", skinId), CustomPlaceholder.inline("permission", permission)));

        return 1;
    }

    public static int editExcludedInRandom(String skinId, boolean excludedInRandom, Consumer<Component> feedback) {

        SavedSkin sk = getSkinOrFeedback(skinId, feedback);
        if(sk == null) return 0;

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        SkinConfiguration configuration = new SkinConfiguration.Builder(reg.getSavedConfiguration(skinId))
                .excludeInRandom(excludedInRandom)
                .build();

        SkinSetterAPI.REGISTRY.get().registerSkin(skinId, configuration.createSkin(sk.getSkin()));

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        feedback.accept(LangContent.component(lang, "command.edit.excludedInRandom", CustomPlaceholder.inline("skin_id", skinId), CustomPlaceholder.inline("value", excludedInRandom)));

        return 1;
    }

    public static int editExcludedInGUI(String skinId, boolean excludedInGUI, Consumer<Component> feedback) {

        SavedSkin sk = getSkinOrFeedback(skinId, feedback);
        if(sk == null) return 0;

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        SkinConfiguration configuration = new SkinConfiguration.Builder(reg.getSavedConfiguration(skinId))
                .excludeInGUI(excludedInGUI)
                .build();

        SkinSetterAPI.REGISTRY.get().registerSkin(skinId, configuration.createSkin(sk.getSkin()));

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        feedback.accept(LangContent.component(lang, "command.edit.excludedInGUI", CustomPlaceholder.inline("skin_id", skinId), CustomPlaceholder.inline("value", excludedInGUI)));

        return 1;
    }

    public static int editItem(String skinId, Player player, Consumer<Component> feedback) {

        SavedSkin sk = getSkinOrFeedback(skinId, feedback);
        if(sk == null) return 0;

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        SkinConfiguration configuration = new SkinConfiguration.Builder(reg.getSavedConfiguration(skinId))
                .displayItem(player.getItem(Entity.EquipmentSlot.MAINHAND))
                .build();

        SkinSetterAPI.REGISTRY.get().registerSkin(skinId, configuration.createSkin(sk.getSkin()));

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        feedback.accept(LangContent.component(lang, "command.edit.item", CustomPlaceholder.inline("skin_id", skinId)));

        return 1;
    }

    public static int reload(Consumer<Component> feedback) {

        long time = System.currentTimeMillis();

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();
        SkinSetterServer.INSTANCE.get().reload();

        time = System.currentTimeMillis() - time;

        feedback.accept(LangContent.component(lang, "command.reload", CustomPlaceholder.inline("elapsed", time)));

        return 1;
    }

    private static SavedSkin getSkinOrFeedback(String skinId, Consumer<Component> feedback) {

        LangManager lang = SkinSetterServer.INSTANCE.get().getLangManager();

        SkinRegistry reg = SkinSetterAPI.REGISTRY.get();
        SavedSkin sk = reg.getSkin(skinId);
        if(sk == null) {
            feedback.accept(LangContent.component(lang, "error.skin_not_found", CustomPlaceholder.inline("skin_id", skinId)));
            return null;
        }

        return sk;
    }

    private static void sendMultiFeedback(Collection<?> targets, String key, Consumer<Component> feedback, Object arg) {

        List<Object> args = new ArrayList<>();
        if(arg != null) args.add(arg);

        if(targets.size() == 1) {
            args.add(targets.iterator().next());
            feedback.accept(LangContent.component(SkinSetterServer.INSTANCE.get().getLangManager(), key, args));
        } else {
            args.add(CustomPlaceholder.inline("count", targets.size()));
            feedback.accept(LangContent.component(SkinSetterServer.INSTANCE.get().getLangManager(), key + ".multiple", args));
        }

    }

}
