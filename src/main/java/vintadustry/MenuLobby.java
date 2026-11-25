/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.ApplicationListener
 *  arc.Core
 *  arc.Events
 *  arc.files.Fi
 *  arc.graphics.Color
 *  arc.graphics.g2d.Draw
 *  arc.graphics.g2d.Fill
 *  arc.graphics.g2d.TextureAtlas$AtlasRegion
 *  arc.graphics.g2d.TextureRegion
 *  arc.math.Mathf
 *  arc.scene.Element
 *  arc.scene.Group
 *  arc.scene.event.Touchable
 *  arc.scene.style.Drawable
 *  arc.scene.ui.Button
 *  arc.scene.ui.ScrollPane
 *  arc.scene.ui.layout.Cell
 *  arc.scene.ui.layout.Scl
 *  arc.scene.ui.layout.Table
 *  arc.struct.Seq
 *  arc.util.Log
 *  arc.util.Reflect
 *  mindustry.Vars
 *  mindustry.core.Version
 *  mindustry.game.EventType$ClientLoadEvent
 *  mindustry.game.EventType$ResizeEvent
 *  mindustry.gen.Icon
 *  mindustry.graphics.Pal
 *  mindustry.ui.Fonts
 *  mindustry.ui.Styles
 *  mindustry.ui.fragments.MenuFragment
 *  mindustry.ui.fragments.MenuFragment$MenuButton
 */
package vintadustry;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Reflect;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.fragments.MenuFragment;
import vintadustry.LobbyAmbient;
import vintadustry.LobbyFix;

public class MenuLobby {
    static MenuFragment mf;
    static Group group;
    static Table fill;
    static Element vanillaLogo;
    static Table root;
    static Table list;
    static ScrollPane sp;
    static LogoEl logoEl;
    static boolean applied;
    static int lastKey;
    static final Seq<MenuFragment.MenuButton> open;
    static boolean inited;

    public static void init() {
        if (Vars.headless || inited) {
            return;
        }
        inited = true;
        Events.on(EventType.ClientLoadEvent.class, clientLoadEvent -> MenuLobby.apply());
    }

    static boolean holdsVersion(Object object, int n) {
        if (object == null || n > 2) {
            return false;
        }
        String string = Version.combined();
        for (Class<?> clazz = object.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                try {
                    field.setAccessible(true);
                    Object object2 = field.get(object);
                    if (object2 instanceof String) {
                        if (!((String)object2).contains(string)) continue;
                        return true;
                    }
                    if (object2 == null || n >= 2) continue;
                    String string2 = object2.getClass().getName();
                    if (!object2.getClass().isSynthetic() && !string2.contains("Lambda") || !MenuLobby.holdsVersion(object2, n + 1)) continue;
                    return true;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return false;
    }

    static void apply() {
        if (applied) {
            return;
        }
        try {
            mf = Vars.ui.menufrag;
            Table table = (Table)Reflect.get((Object)mf, (String)"container");
            if (table == null || !(table.parent instanceof ScrollPane) || !(table.parent.parent instanceof Table)) {
                Log.warn((String)"Lobby layout: the menu has another shape, the normal menu stays", (Object[])new Object[0]);
                return;
            }
            fill = (Table)table.parent.parent;
            group = MenuLobby.fill.parent;
            if (group == null) {
                return;
            }
            for (Element element : group.getChildren()) {
                if (element instanceof Group || !MenuLobby.holdsVersion(element, 0)) continue;
                vanillaLogo = element;
                break;
            }
            if (vanillaLogo == null) {
                Log.warn((String)"Lobby layout: the game's logo was not found, so only the column is not used: normal menu stays", (Object[])new Object[0]);
                return;
            }
            root = new Table();
            root.setFillParent(true);
            MenuLobby.root.touchable = Touchable.childrenOnly;
            logoEl = new LogoEl();
            group.addChild((Element)root);
            group.addChild((Element)logoEl);
            Vars.ui.settings.game.checkPref("vd-lobby", true, bl -> Core.app.post(MenuLobby::layout));
            applied = true;
            LobbyAmbient.start();
            Events.on(EventType.ResizeEvent.class, resizeEvent -> MenuLobby.layout());
            Core.app.addListener((ApplicationListener)new TickListener());
            MenuLobby.layout();
        }
        catch (Throwable throwable) {
            applied = false;
            Log.err((String)"Lobby layout could not start, the normal menu stays", (Throwable)throwable);
            MenuLobby.restore();
        }
    }

    static void restore() {
        try {
            LobbyAmbient.stop();
            if (fill != null) {
                MenuLobby.fill.visible = true;
            }
            if (vanillaLogo != null) {
                MenuLobby.vanillaLogo.visible = true;
            }
            if (root != null) {
                MenuLobby.root.visible = false;
            }
            if (logoEl != null) {
                MenuLobby.logoEl.visible = false;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static int key() {
        Seq seq = (Seq)Reflect.get((Object)mf, (String)"customButtons");
        return (seq == null ? 0 : seq.size) * 31 + (MenuLobby.mf.desktopButtons == null ? 0 : MenuLobby.mf.desktopButtons.size);
    }

    static void tick() {
        if (!applied) {
            return;
        }
        try {
            if (MenuLobby.key() != lastKey) {
                MenuLobby.layout();
            }
        }
        catch (Throwable throwable) {
            applied = false;
            Log.err((String)"Lobby layout stopped", (Throwable)throwable);
            MenuLobby.restore();
        }
    }

    static void check(Runnable runnable) {
        if (!Vars.mods.hasContentErrors()) {
            runnable.run();
        } else {
            Vars.ui.showInfo("@mod.noerrorplay");
        }
    }

    static Seq<MenuFragment.MenuButton> defaults() {
        Seq seq = new Seq();
        seq.add((Object)new MenuFragment.MenuButton("@play", (Drawable)Icon.play, () -> {}, new MenuFragment.MenuButton[]{new MenuFragment.MenuButton("@campaign", (Drawable)Icon.play, () -> MenuLobby.check(() -> Vars.ui.planet.show())), new MenuFragment.MenuButton("@joingame", (Drawable)Icon.add, () -> MenuLobby.check(() -> Vars.ui.join.show())), new MenuFragment.MenuButton("@customgame", (Drawable)Icon.terrain, () -> MenuLobby.check(() -> Vars.ui.custom.show())), new MenuFragment.MenuButton("@loadgame", (Drawable)Icon.download, () -> MenuLobby.check(() -> Vars.ui.load.show()))}));
        MenuFragment.MenuButton[] menuButtonArray = new MenuFragment.MenuButton[]{new MenuFragment.MenuButton("@schematics", (Drawable)Icon.paste, () -> Vars.ui.schematics.show()), new MenuFragment.MenuButton("@database", (Drawable)Icon.book, () -> Vars.ui.database.show()), new MenuFragment.MenuButton("@about.button", (Drawable)Icon.info, () -> Vars.ui.about.show())};
        seq.add((Object)new MenuFragment.MenuButton("@database.button", (Drawable)Icon.menu, () -> {}, menuButtonArray));
        seq.add((Object)new MenuFragment.MenuButton("@editor", (Drawable)Icon.terrain, () -> MenuLobby.check(() -> Vars.ui.maps.show())));
        if (Vars.steam) {
            seq.add((Object)new MenuFragment.MenuButton("@workshop", (Drawable)Icon.steam, () -> Vars.platform.openWorkshop()));
        }
        seq.add((Object)new MenuFragment.MenuButton("@mods", (Drawable)Icon.book, () -> Vars.ui.mods.show()));
        seq.add((Object)new MenuFragment.MenuButton("@settings", (Drawable)Icon.settings, () -> Vars.ui.settings.show()));
        return seq;
    }

    static float[] logoSize() {
        TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("logo");
        if (atlasRegion == null || !atlasRegion.found()) {
            return new float[]{300.0f, 60.0f};
        }
        float f = Scl.scl((float)1.0f) * atlasRegion.scale;
        float f2 = Core.graphics.isPortrait() ? (float)Core.graphics.getWidth() * 0.82f : (float)Core.graphics.getWidth() * 0.32f;
        float f3 = Math.min((float)atlasRegion.width * f, Math.min(f2, (float)Core.graphics.getWidth() - Scl.scl((float)20.0f)));
        return new float[]{f3, f3 * (float)atlasRegion.height / (float)atlasRegion.width};
    }

    static float logoSpace() {
        float[] fArray = MenuLobby.logoSize();
        return (fArray[1] + Scl.scl((float)38.0f) + Core.scene.marginTop) / Scl.scl((float)1.0f);
    }

    static float width() {
        float f = (float)Core.graphics.getWidth() / Scl.scl((float)1.0f);
        float f2 = Core.graphics.isPortrait() ? Math.min(f - 40.0f, 400.0f) : (Vars.mobile ? 280.0f : 210.0f);
        return Math.max(160.0f, Math.min(f2, f - 40.0f));
    }

    static void layout() {
        if (!applied) {
            return;
        }
        try {
            boolean bl = Core.settings.getBool("vd-lobby", true);
            MenuLobby.fill.visible = !bl;
            MenuLobby.vanillaLogo.visible = !bl;
            MenuLobby.root.visible = bl;
            MenuLobby.logoEl.visible = bl;
            LobbyAmbient.sync(bl);
            if (!bl) {
                lastKey = MenuLobby.key();
                return;
            }
            Seq seq = (Seq)Reflect.get((Object)mf, (String)"customButtons");
            Seq seq2 = MenuLobby.mf.desktopButtons != null ? MenuLobby.mf.desktopButtons : LobbyFix.defaults();
            float f = MenuLobby.width();
            float f2 = Vars.mobile ? 64.0f : 70.0f;
            list = new Table();
            list.top().left();
            list.defaults().width(f).height(f2);
            MenuLobby.add(list, (Seq<MenuFragment.MenuButton>)seq2, 0, f);
            if (seq != null) {
                MenuLobby.add(list, (Seq<MenuFragment.MenuButton>)seq, 0, f);
            }
            Seq seq3 = new Seq();
            seq3.add((Object)(Vars.ios ? new MenuFragment.MenuButton("@about.button", (Drawable)Icon.info, () -> Vars.ui.about.show()) : new MenuFragment.MenuButton("@quit", (Drawable)Icon.exit, () -> Core.app.exit())));
            MenuLobby.add(list, (Seq<MenuFragment.MenuButton>)seq3, 0, f);
            list.add().height(90.0f).row();
            sp = new ScrollPane((Element)list, Styles.noBarPane);
            sp.setScrollingDisabled(true, false);
            sp.setOverscroll(false, false);
            Table table = new Table(Styles.black6);
            table.add((Element)new BarEl()).width(10.0f).growY().padLeft(14.0f).padTop(6.0f).padBottom(6.0f);
            table.add((Element)sp).width(f + 6.0f).growY().padLeft(10.0f);
            root.clear();
            root.setFillParent(true);
            root.top().left();
            root.marginLeft(Core.scene.marginLeft / Scl.scl((float)1.0f) + 12.0f);
            root.add((Element)table).padTop(MenuLobby.logoSpace()).growY().left();
            root.invalidateHierarchy();
            sp.setScrollY(0.0f);
            sp.updateVisualScroll();
            lastKey = MenuLobby.key();
        }
        catch (Throwable throwable) {
            applied = false;
            Log.err((String)"Lobby layout failed, the normal menu is back", (Throwable)throwable);
            MenuLobby.restore();
        }
    }

    static void add(Table table, Seq<MenuFragment.MenuButton> seq, int n, float f) {
        for (MenuFragment.MenuButton menuButton : seq) {
            if (menuButton == null) continue;
            MenuFragment.MenuButton menuButton2 = menuButton;
            boolean bl = menuButton.submenu != null && menuButton.submenu.any();
            boolean bl2 = open.contains((Object)menuButton, true);
            Cell cell = LobbyFix.button(table, menuButton.text, menuButton.icon, Styles.flatToggleMenut, () -> {
                if (bl) {
                    if (bl2) {
                        open.remove((Object)menuButton2, true);
                    } else {
                        open.add((Object)menuButton2);
                    }
                    Core.app.post(MenuLobby::layout);
                } else if (menuButton2.runnable != null) {
                    menuButton2.runnable.run();
                }
            }).marginLeft(11.0f);
            if (n > 0) {
                cell.width(f - 28.0f * (float)n).padLeft(28.0f * (float)n).left();
            }
            Button button = (Button)cell.get();
            button.setChecked(bl && bl2);
            table.row();
            if (!bl || !bl2) continue;
            MenuLobby.add(table, (Seq<MenuFragment.MenuButton>)menuButton.submenu, n + 1, f);
        }
    }

    static {
        try {
            MenuLobby.init();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        lastKey = -1;
        open = new Seq();
    }

    static class LogoEl
    extends Element {
        LogoEl() {
            this.setFillParent(true);
            this.touchable = Touchable.disabled;
        }

        public void draw() {
            TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("logo");
            if (atlasRegion == null || !atlasRegion.found()) {
                return;
            }
            float[] fArray = MenuLobby.logoSize();
            float f = Core.scene.marginLeft + Scl.scl((float)14.0f);
            float f2 = (float)Core.graphics.getHeight() - Core.scene.marginTop - Scl.scl((float)6.0f);
            Draw.color();
            Draw.rect((TextureRegion)atlasRegion, (float)(f + fArray[0] / 2.0f), (float)(f2 - fArray[1] / 2.0f), (float)fArray[0], (float)fArray[1]);
            String string = (Version.build == -1 ? "[#fc8140aa]" : "[#ffffffba]") + Version.combined();
            Fonts.outline.setColor(Color.white);
            Fonts.outline.draw((CharSequence)string, f + Scl.scl((float)4.0f), f2 - fArray[1] - Scl.scl((float)2.0f), 8);
        }
    }

    static class TickListener
    implements ApplicationListener {
        TickListener() {
        }

        public void init() {
        }

        public void resize(int n, int n2) {
        }

        public void update() {
            MenuLobby.tick();
        }

        public void pause() {
        }

        public void resume() {
        }

        public void dispose() {
        }

        public void exit() {
        }

        public void fileDropped(Fi fi) {
        }
    }

    static class BarEl
    extends Element {
        BarEl() {
            this.touchable = Touchable.disabled;
        }

        public void draw() {
            if (sp == null || list == null) {
                return;
            }
            float f = sp.getHeight();
            float f2 = list.getPrefHeight();
            float f3 = Scl.scl((float)5.0f);
            float f4 = this.x + this.width / 2.0f;
            Draw.color((Color)Color.black, (float)0.35f);
            Fill.rect((float)f4, (float)(this.y + this.height / 2.0f), (float)f3, (float)this.height);
            if (f2 > f + 1.0f) {
                float f5 = Math.max(Scl.scl((float)48.0f), this.height * f / f2);
                float f6 = Mathf.clamp((float)sp.getScrollPercentY());
                Draw.color((Color)Pal.accent);
                Fill.rect((float)f4, (float)(this.y + this.height - f6 * (this.height - f5) - f5 / 2.0f), (float)f3, (float)f5);
            }
            Draw.reset();
        }
    }
}
