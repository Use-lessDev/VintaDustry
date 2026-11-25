/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.Events
 *  arc.math.Interp
 *  arc.math.Mathf
 *  arc.scene.Action
 *  arc.scene.Element
 *  arc.scene.actions.Actions
 *  arc.scene.event.Touchable
 *  arc.scene.style.TextureRegionDrawable
 *  arc.scene.ui.Label
 *  arc.scene.ui.layout.Table
 *  mindustry.Vars
 *  mindustry.game.EventType$GameOverEvent
 *  mindustry.game.EventType$WorldLoadEvent
 *  mindustry.game.Team
 *  mindustry.gen.Tex
 *  mindustry.graphics.Pal
 *  mindustry.ui.Styles
 */
package vintadustry;

import arc.Core;
import arc.Events;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.Action;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class DeathScreen {
    static boolean active;
    static Table root;
    static final float wait = 1.3f;
    static final float fade = 1.9f;
    static final float msgAt = 2.4f;
    static final float msgFade = 1.0f;
    static final float total = 6.0f;

    public static void init() {
        if (Vars.headless) {
            return;
        }
        Events.on(EventType.GameOverEvent.class, gameOverEvent -> {
            if (Vars.player == null || gameOverEvent.winner == Vars.player.team() || active) {
                return;
            }
            Team team = gameOverEvent.winner;
            Core.app.post(() -> {
                Vars.ui.restart.hide();
                DeathScreen.start(team);
            });
        });
        Events.on(EventType.WorldLoadEvent.class, worldLoadEvent -> DeathScreen.clear());
    }

    static void clear() {
        active = false;
        if (root != null) {
            root.remove();
            root = null;
        }
    }

    static String message() {
        String string = "Tasky was torn apart.";
        return Core.bundle.get("death.tasky." + (1 + Mathf.random((int)5)), string);
    }

    static void start(Team team) {
        Table table;
        if (active) {
            return;
        }
        active = true;
        root = table = new Table();
        table.setFillParent(true);
        table.touchable = Touchable.enabled;
        table.setBackground(((TextureRegionDrawable)Tex.whiteui).tint(0.0f, 0.0f, 0.0f, 1.0f));
        table.color.a = 0.0f;
        Label label = new Label((CharSequence)DeathScreen.message(), Styles.outlineLabel);
        label.setFontScale(1.9f);
        label.setWrap(true);
        label.setAlignment(1);
        label.setColor(Pal.remove);
        label.color.a = 0.0f;
        Label label2 = new Label((CharSequence)Core.bundle.get("death.continue", "Tap to continue"), Styles.outlineLabel);
        label2.setFontScale(0.85f);
        label2.setColor(0.75f, 0.75f, 0.78f, 0.0f);
        table.add((Element)label).width(Math.min((float)Core.graphics.getWidth() * 0.8f, 900.0f)).row();
        table.add((Element)label2).padTop(28.0f);
        float[] fArray = new float[]{0.0f};
        boolean[] blArray = new boolean[]{false};
        Runnable runnable = () -> {
            if (blArray[0]) {
                return;
            }
            blArray[0] = true;
            active = false;
            Vars.ui.restart.show(team);
            table.actions(new Action[]{Actions.sequence((Action)Actions.fadeOut((float)0.6f), (Action)Actions.remove())});
        };
        table.update(() -> {
            if (blArray[0]) {
                return;
            }
            fArray[0] = fArray[0] + Core.graphics.getDeltaTime();
            table.color.a = 0.95f * Interp.pow2Out.apply(Mathf.clamp((float)((fArray[0] - 1.3f) / 1.9f)));
            label.color.a = Interp.pow2.apply(Mathf.clamp((float)((fArray[0] - 2.4f) / 1.0f)));
            label2.color.a = 0.75f * Mathf.clamp((float)((fArray[0] - 4.0f) / 0.8f));
            if (fArray[0] >= 6.0f) {
                runnable.run();
            }
        });
        table.clicked(() -> {
            if (fArray[0] > 3.2f) {
                runnable.run();
            }
        });
        Core.scene.add((Element)table);
    }
}
