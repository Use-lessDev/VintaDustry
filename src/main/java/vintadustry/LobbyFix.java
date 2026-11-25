/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.scene.style.Drawable
 *  arc.scene.ui.TextButton
 *  arc.scene.ui.TextButton$TextButtonStyle
 *  arc.scene.ui.layout.Cell
 *  arc.scene.ui.layout.Table
 *  arc.struct.Seq
 *  arc.util.Log
 *  mindustry.Vars
 *  mindustry.gen.Icon
 *  mindustry.ui.fragments.MenuFragment$MenuButton
 */
package vintadustry;

import arc.Core;
import arc.scene.style.Drawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.fragments.MenuFragment;
import vintadustry.LobbyLogo;
import vintadustry.LobbyWarning;
import vintadustry.MenuLobby;

public class LobbyFix {
    static MenuFragment.MenuButton b(String string, Drawable drawable, Runnable runnable) {
        return new MenuFragment.MenuButton(string, drawable, runnable);
    }

    public static Seq<MenuFragment.MenuButton> defaults() {
        try {
            LobbyLogo.swap();
        }
        catch (Throwable throwable) {
            Log.err((String)"Lobby logo swap failed", (Throwable)throwable);
        }
        try {
            LobbyWarning.setup();
        }
        catch (Throwable throwable) {
            Log.err((String)"Lobby warning panel failed", (Throwable)throwable);
        }
        Seq seq = new Seq();
        seq.add((Object)LobbyFix.b("@campaign", (Drawable)Icon.play, () -> MenuLobby.check(() -> Vars.ui.planet.show())));
        seq.add((Object)LobbyFix.b("@customgame", (Drawable)Icon.terrain, () -> MenuLobby.check(() -> Vars.ui.custom.show())));
        seq.add((Object)LobbyFix.b("@joingame", (Drawable)Icon.add, () -> MenuLobby.check(() -> Vars.ui.join.show())));
        seq.add((Object)LobbyFix.b("@loadgame", (Drawable)Icon.download, () -> MenuLobby.check(() -> Vars.ui.load.show())));
        seq.add((Object)LobbyFix.b("@editor", (Drawable)Icon.terrain, () -> MenuLobby.check(() -> Vars.ui.maps.show())));
        seq.add((Object)LobbyFix.b("@schematics", (Drawable)Icon.paste, () -> Vars.ui.schematics.show()));
        seq.add((Object)LobbyFix.b("@database", (Drawable)Icon.book, () -> Vars.ui.database.show()));
        if (Vars.steam) {
            seq.add((Object)LobbyFix.b("@workshop", (Drawable)Icon.steam, () -> Vars.platform.openWorkshop()));
        }
        seq.add((Object)LobbyFix.b("@mods", (Drawable)Icon.book, () -> Vars.ui.mods.show()));
        seq.add((Object)LobbyFix.b("@about.button", (Drawable)Icon.info, () -> Vars.ui.about.show()));
        seq.add((Object)LobbyFix.b("@settings", (Drawable)Icon.settings, () -> Vars.ui.settings.show()));
        return seq;
    }

    public static Cell<TextButton> button(Table table, String string, Drawable drawable, TextButton.TextButtonStyle textButtonStyle, Runnable runnable) {
        TextButton[] textButtonArray = new TextButton[1];
        Cell cell = table.button(string, drawable, textButtonStyle, () -> {
            runnable.run();
            Core.app.post(() -> {
                if (textButtonArray[0] != null) {
                    textButtonArray[0].setChecked(false);
                }
            });
        });
        textButtonArray[0] = (TextButton)cell.get();
        textButtonArray[0].setProgrammaticChangeEvents(false);
        return cell;
    }
}
