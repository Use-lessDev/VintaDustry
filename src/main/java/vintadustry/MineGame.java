/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.Events
 *  arc.graphics.Color
 *  arc.graphics.g2d.Draw
 *  arc.graphics.g2d.Fill
 *  arc.graphics.g2d.Lines
 *  arc.graphics.g2d.TextureRegion
 *  arc.input.KeyCode
 *  arc.math.Interp
 *  arc.math.Mat
 *  arc.math.Mathf
 *  arc.math.geom.Bresenham2
 *  arc.math.geom.Vec2
 *  arc.scene.Element
 *  arc.scene.event.Touchable
 *  arc.struct.ObjectMap
 *  arc.struct.Seq
 *  arc.util.Time
 *  arc.util.Tmp
 *  mindustry.Vars
 *  mindustry.content.Blocks
 *  mindustry.content.Fx
 *  mindustry.core.World
 *  mindustry.entities.Effect
 *  mindustry.game.EventType$ClientLoadEvent
 *  mindustry.game.EventType$Trigger
 *  mindustry.game.EventType$WorldLoadEvent
 *  mindustry.gen.Building
 *  mindustry.gen.Call
 *  mindustry.gen.Itemsc
 *  mindustry.gen.Player
 *  mindustry.gen.Unit
 *  mindustry.graphics.Pal
 *  mindustry.type.Item
 *  mindustry.ui.Fonts
 *  mindustry.world.Tile
 */
package vintadustry;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Interp;
import arc.math.Mat;
import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Itemsc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Fonts;
import mindustry.world.Tile;
import vintadustry.Boulder;
import vintadustry.MinePacket;

public class MineGame {
    public static final int rounds = 6;
    public static final int maxMisses = 3;
    static final float reach = 26.0f;
    static final ObjectMap<Player, Session> sessions = new ObjectMap();
    static State state = State.idle;
    static Tile target;
    static float holdTime;
    static float needle;
    static float speed;
    static float zoneStart;
    static float zoneSize;
    static float resultTime;
    static float shake;
    static float flash;
    static int round;
    static int misses;
    static int streak;
    static int earned;
    static int lastGain;
    static boolean dir;
    static String banner;
    static Color bannerColor;
    static Element ui;
    static final Seq<float[]> sparks;
    static boolean locked;
    static float dragX;
    static float dragY;
    static float openT;
    static float pulse;
    static float bannerT;
    static int mode;
    static String hint;
    static boolean charging;
    static float charge;
    static float chargeSpeed;
    static float chargeLo;
    static float chargeSize;
    static float timer;
    static int taps;
    static int need;
    static int inputIdx;
    static int lit;
    static int nIdx;
    static int caught;
    static int nTotal;
    static float nWait;
    static float nLife;
    static float nMax;
    static float nx;
    static float ny;
    static float showTimer;
    static float inputTime;
    static float litTime;
    static final String[] modeNames;

    public static void init() {
        Events.run((Object)EventType.Trigger.update, MineGame::update);
        Events.on(EventType.ClientLoadEvent.class, clientLoadEvent -> Vars.ui.settings.game.sliderPref("vintadustry-minigame-size", 100, 50, 160, 10, n -> n + "%"));
        Events.on(EventType.WorldLoadEvent.class, worldLoadEvent -> {
            sessions.clear();
            MineGame.stop(false);
        });
    }

    static void serverEvent(Player player, byte by, int n) {
        Building building;
        if (player.unit() == null || player.unit().dead) {
            return;
        }
        Tile tile = Vars.world.tile(n);
        if (tile == null || !((building = tile.build) instanceof Boulder.BoulderBuild)) {
            sessions.remove((Object)player);
            return;
        }
        Boulder.BoulderBuild boulderBuild = (Boulder.BoulderBuild)building;
        if (boulderBuild.ore == null) {
            sessions.remove((Object)player);
            return;
        }
        building = player.unit();
        if (!building.within(tile.worldx(), tile.worldy(), 70.0f)) {
            sessions.remove((Object)player);
            return;
        }
        if (by == 0) {
            Session session = new Session();
            session.pos = n;
            sessions.put((Object)player, (Object)session);
            return;
        }
        Session session = (Session)sessions.get((Object)player);
        if (session == null || session.pos != n) {
            return;
        }
        if (by == 4) {
            MineGame.finish(player, session, tile, boulderBuild);
            return;
        }
        if (Time.millis() - session.last < 150L) {
            return;
        }
        session.last = Time.millis();
        ++session.round;
        if (by == 1) {
            ++session.misses;
            session.streak = 0;
            Call.effect((Effect)Fx.pulverizeRed, (float)tile.worldx(), (float)tile.worldy(), (float)0.0f, (Color)boulderBuild.ore.color);
        } else {
            ++session.hits;
            ++session.streak;
            boolean bl = by == 3;
            int n2 = (!boulderBuild.cracked ? 3 : 2) + Math.min(session.streak / 2, 4) + (bl ? 2 : 0);
            MineGame.giveOre((Unit)building, boulderBuild.ore, n2, tile);
            Call.effect((Effect)(bl ? Fx.pulverizeMedium : Fx.mineHuge), (float)tile.worldx(), (float)tile.worldy(), (float)0.0f, (Color)boulderBuild.ore.color);
            if (bl) {
                Call.effect((Effect)Fx.mineImpactWave, (float)tile.worldx(), (float)tile.worldy(), (float)0.0f, (Color)boulderBuild.ore.color);
            }
        }
        if (session.round >= 6 || session.misses >= 3) {
            MineGame.finish(player, session, tile, boulderBuild);
        }
    }

    static void giveOre(Unit unit, Item item, int n, Tile tile) {
        int n2 = 0;
        for (int i = 0; i < n; ++i) {
            if (unit.acceptsItem(item)) {
                unit.addItem(item, 1);
                ++n2;
            } else if (unit.team.core() != null) {
                unit.team.core().items.add(item, 1);
                ++n2;
            }
            if (n2 <= 0) continue;
            Call.transferItemEffect((Item)item, (float)(tile.worldx() + Mathf.range((float)6.0f)), (float)(tile.worldy() + Mathf.range((float)6.0f)), (Itemsc)unit);
        }
    }

    static void finish(Player player, Session session, Tile tile, Boulder.BoulderBuild boulderBuild) {
        sessions.remove((Object)player);
        if (session.hits <= 0) {
            return;
        }
        Call.effect((Effect)Fx.explosion, (float)tile.worldx(), (float)tile.worldy(), (float)0.0f, (Color)boulderBuild.ore.color);
        if (!boulderBuild.cracked) {
            boulderBuild.configure(true);
        } else {
            tile.setNet(Blocks.air);
        }
    }

    static float k() {
        return (float)Core.settings.getInt("vintadustry-minigame-size", 100) / 100.0f;
    }

    static void send(int n) {
        if (target == null) {
            return;
        }
        if (Vars.net.client()) {
            MinePacket minePacket = new MinePacket();
            minePacket.type = (byte)n;
            minePacket.pos = target.pos();
            Vars.net.send((Object)minePacket, true);
        } else if (Vars.player != null) {
            MineGame.serverEvent(Vars.player, (byte)n, target.pos());
        }
    }

    static void stop(boolean bl) {
        if (bl && target != null && state == State.play) {
            MineGame.send(4);
        }
        state = State.idle;
        target = null;
        holdTime = 0.0f;
        if (ui != null) {
            ui.remove();
            ui = null;
        }
    }

    static Tile findBoulder(float f, float f2) {
        Tile tile = null;
        float f3 = Vars.mobile ? 20.0f : 6.0f;
        int n = World.toTile((float)f);
        int n2 = World.toTile((float)f2);
        for (int i = n - 2; i <= n + 2; ++i) {
            for (int j = n2 - 2; j <= n2 + 2; ++j) {
                float f4;
                Tile tile2 = Vars.world.tile(i, j);
                if (tile2 == null || !(tile2.build instanceof Boulder.BoulderBuild) || !((f4 = Mathf.dst((float)f, (float)f2, (float)tile2.worldx(), (float)tile2.worldy()) - 4.0f) < f3)) continue;
                f3 = f4;
                tile = tile2;
            }
        }
        return tile;
    }

    static boolean canReach(Unit unit, Tile tile) {
        if (unit.type.flying || unit.type.allowLegStep || unit.type.hovering) {
            return true;
        }
        boolean[] blArray = new boolean[]{true};
        World world = Vars.world;
        Bresenham2.line((int)unit.tileX(), (int)unit.tileY(), (int)tile.x, (int)tile.y, (n, n2) -> {
            Tile tile2 = world.tile(n, n2);
            if (tile2 != null && tile2 != tile && tile2.solid() && !(tile2.block() instanceof Boulder)) {
                blArray[0] = false;
            }
        });
        return blArray[0];
    }

    static void update() {
        if (Vars.headless || !Vars.state.isGame() || Vars.player == null) {
            return;
        }
        Unit unit = Vars.player.unit();
        if (unit == null || unit.dead) {
            if (state != State.idle) {
                MineGame.stop(false);
            }
            return;
        }
        if (state == State.idle) {
            if (!Core.input.isTouched()) {
                locked = false;
                holdTime = 0.0f;
                target = null;
                return;
            }
            if (locked || !Vars.mobile && Core.scene.hasMouse()) {
                holdTime = 0.0f;
                target = null;
                return;
            }
            if (target == null) {
                dragX = Core.input.mouseX();
                dragY = Core.input.mouseY();
            } else {
                float f = Mathf.dst((float)Core.input.mouseX(), (float)Core.input.mouseY(), (float)dragX, (float)dragY);
                float f2 = Vars.mobile ? 14.0f : 8.0f;
                if (f > f2) {
                    locked = true;
                    holdTime = 0.0f;
                    target = null;
                    return;
                }
            }
            float f = Core.input.mouseWorldX();
            float f3 = Core.input.mouseWorldY();
            Tile tile = MineGame.findBoulder(f, f3);
            if (tile == null) {
                holdTime = 0.0f;
                target = null;
                return;
            }
            if (tile != target) {
                target = tile;
                holdTime = 0.0f;
                dragX = Core.input.mouseX();
                dragY = Core.input.mouseY();
            }
            holdTime += Time.delta;
            float f4 = Vars.mobile ? 18.0f : 25.0f;
            if (holdTime >= f4) {
                Building building = tile.build;
                if (building instanceof Boulder.BoulderBuild) {
                    Boulder.BoulderBuild boulderBuild = (Boulder.BoulderBuild)building;
                    if (boulderBuild.ore == null) {
                        Vars.ui.showInfoToast("Tap the boulder to choose an ore first.", 2.0f);
                        target = null;
                        holdTime = 0.0f;
                        locked = true;
                        return;
                    }
                }
                if (!MineGame.canReach(unit, tile)) {
                    Vars.ui.showInfoToast("Can't reach that boulder.", 2.0f);
                    target = null;
                    holdTime = 0.0f;
                    return;
                }
                state = State.approach;
            }
            return;
        }
        if (target == null || !(MineGame.target.build instanceof Boulder.BoulderBuild)) {
            MineGame.stop(false);
            return;
        }
        float f = target.worldx();
        float f5 = target.worldy();
        if (state == State.approach) {
            if (Core.input.keyTap(KeyCode.escape)) {
                MineGame.stop(false);
                return;
            }
            float f6 = unit.dst(f, f5);
            if (f6 > 600.0f) {
                MineGame.stop(false);
                return;
            }
            if (f6 <= 26.0f) {
                MineGame.startGame();
            } else {
                unit.movePref(Tmp.v1.set(f - unit.x, f5 - unit.y).setLength(unit.speed()));
                unit.lookAt(f, f5);
            }
            return;
        }
        if (state == State.play) {
            if (unit.dst(f, f5) > 80.0f) {
                MineGame.stop(true);
                return;
            }
            if (Core.input.keyTap(KeyCode.escape)) {
                MineGame.stop(true);
                return;
            }
            MineGame.updateMode();
            shake = Mathf.lerpDelta((float)shake, (float)0.0f, (float)0.15f);
            flash = Mathf.lerpDelta((float)flash, (float)0.0f, (float)0.1f);
            pulse = Mathf.lerpDelta((float)pulse, (float)0.0f, (float)0.12f);
            bannerT = Mathf.lerpDelta((float)bannerT, (float)0.0f, (float)0.08f);
            for (int i = MineGame.sparks.size - 1; i >= 0; --i) {
                float[] fArray = (float[])sparks.get(i);
                fArray[0] = fArray[0] + fArray[2] * Time.delta;
                fArray[1] = fArray[1] + fArray[3] * Time.delta;
                fArray[3] = fArray[3] - 0.12f * Time.delta;
                fArray[4] = fArray[4] - Time.delta / 40.0f;
                if (!(fArray[4] <= 0.0f)) continue;
                sparks.remove(i);
            }
        } else if (state == State.result && (resultTime -= Time.delta) <= 0.0f) {
            MineGame.stop(false);
        }
    }

    static void startGame() {
        state = State.play;
        openT = 0.0f;
        pulse = 0.0f;
        bannerT = 1.0f;
        round = 0;
        misses = 0;
        streak = 0;
        earned = 0;
        banner = "Get ready!";
        bannerColor = Color.white;
        MineGame.send(0);
        MineGame.newRound();
        ui = new Element(){
            {
                this.touchable = Touchable.enabled;
            }

            public void draw() {
                this.setBounds(0.0f, 0.0f, Core.scene.getWidth(), Core.scene.getHeight());
                MineGame.drawGame();
            }
        };
        Core.scene.add(ui);
    }

    static void newRound() {
        int n = mode;
        while ((mode = Mathf.random((int)0, (int)3)) == n) {
        }
        float f = round;
        charging = false;
        charge = 0.0f;
        taps = 0;
        inputIdx = 0;
        lit = -1;
        litTime = 0.0f;
        showTimer = 0.0f;
        inputTime = 0.0f;
        switch (mode) {
            case 0: {
                speed = 2.4f + f * 0.7f;
                zoneSize = Math.max(34.0f, 70.0f - f * 7.0f);
                zoneStart = Mathf.random((float)360.0f);
                needle = zoneStart + 180.0f + Mathf.range((float)40.0f);
                dir = Mathf.chance((double)0.5);
                hint = "STRIKE: tap when the pickaxe is over the bright zone";
                break;
            }
            case 1: {
                chargeSpeed = 0.012f + f * 0.002f;
                chargeSize = Math.max(0.12f, 0.24f - f * 0.025f);
                chargeLo = Mathf.random((float)0.55f, (float)(0.95f - chargeSize));
                hint = "HEAVY SWING: HOLD to wind up, release in the bright band";
                break;
            }
            case 2: {
                need = 9 + (int)f * 2;
                timer = 150.0f;
                hint = "CHIP AWAY: tap " + need + " times to break the rock before time runs out!";
                break;
            }
            default: {
                nIdx = 0;
                caught = 0;
                nWait = 20.0f;
                nLife = 0.0f;
                nTotal = 4;
                nMax = Math.max(32.0f, 55.0f - f * 4.0f);
                hint = "FIND THE ORE: tap each glowing nugget before it vanishes!";
            }
        }
    }

    static boolean tapped() {
        return Core.input.justTouched() || Core.input.keyTap(KeyCode.space);
    }

    static boolean held() {
        return Core.input.isTouched() || Core.input.keyDown(KeyCode.space);
    }

    static int quadrant() {
        float f = Core.scene.getWidth() / 2.0f;
        float f2 = Core.scene.getHeight() / 2.0f;
        Vec2 vec2 = Core.scene.screenToStageCoordinates(Tmp.v1.set((float)Core.input.mouseX(), (float)Core.input.mouseY()));
        float f3 = Mathf.mod((float)(Mathf.atan2((float)(vec2.x - f), (float)(vec2.y - f2)) * 57.295776f - 90.0f), (float)360.0f);
        return (int)((f3 + 45.0f) % 360.0f) / 90;
    }

    static void updateMode() {
        switch (mode) {
            case 0: {
                needle = Mathf.mod((float)(needle + (float)(dir ? 1 : -1) * speed * Time.delta), (float)360.0f);
                if (!MineGame.tapped()) break;
                float f = Mathf.mod((float)(needle - zoneStart), (float)360.0f);
                boolean bl = f <= zoneSize;
                MineGame.resolve(bl, bl && Math.abs(f - zoneSize / 2.0f) <= zoneSize * 0.14f);
                break;
            }
            case 1: {
                if (!charging) {
                    if (!MineGame.tapped()) break;
                    charging = true;
                    charge = 0.0f;
                    break;
                }
                charge += chargeSpeed * Time.delta;
                if (MineGame.held() && !(charge >= 1.1f)) break;
                boolean bl = charge >= chargeLo && charge <= chargeLo + chargeSize;
                MineGame.resolve(bl, bl && Math.abs(charge - (chargeLo + chargeSize / 2.0f)) <= chargeSize * 0.16f);
                break;
            }
            case 2: {
                timer -= Time.delta;
                if (MineGame.tapped()) {
                    ++taps;
                    flash = 0.6f;
                    shake = 2.0f;
                }
                if (!(timer <= 0.0f)) break;
                MineGame.resolve(taps >= need, (float)taps >= (float)need * 1.5f);
                break;
            }
            default: {
                if (lit == 1) {
                    nLife -= Time.delta;
                    boolean bl = false;
                    if (MineGame.tapped()) {
                        Vec2 vec2 = Core.scene.screenToStageCoordinates(Tmp.v1.set((float)Core.input.mouseX(), (float)Core.input.mouseY()));
                        boolean bl2 = bl = Mathf.dst((float)((vec2.x - Core.scene.getWidth() / 2.0f) / MineGame.k()), (float)((vec2.y - Core.scene.getHeight() / 2.0f) / MineGame.k()), (float)nx, (float)ny) < 48.0f;
                    }
                    if (bl) {
                        ++caught;
                        flash = 0.7f;
                        shake = 3.0f;
                        for (int i = 0; i < 8; ++i) {
                            float f = Mathf.random((float)360.0f);
                            float f2 = Mathf.random((float)1.5f, (float)4.0f);
                            sparks.add((Object)new float[]{nx / 12.0f, ny / 12.0f, Mathf.cosDeg((float)f) * f2, Mathf.sinDeg((float)f) * f2, 1.0f});
                        }
                    }
                    if (!bl && !(nLife <= 0.0f)) break;
                    if (!bl) {
                        shake = 5.0f;
                    }
                    lit = 0;
                    ++nIdx;
                    nWait = 14.0f;
                    break;
                }
                if (nIdx < nTotal) {
                    if (!((nWait -= Time.delta) <= 0.0f)) break;
                    float f = Mathf.random((float)360.0f);
                    float f3 = Mathf.random((float)25.0f, (float)75.0f);
                    nx = Mathf.cosDeg((float)f) * f3;
                    ny = Mathf.sinDeg((float)f) * f3;
                    nLife = nMax;
                    lit = 1;
                    break;
                }
                MineGame.resolve(caught >= nTotal - 1, caught == nTotal);
            }
        }
    }

    static void resolve(boolean bl, boolean bl2) {
        Building building;
        if (state != State.play || target == null || !((building = MineGame.target.build) instanceof Boulder.BoulderBuild)) {
            return;
        }
        Boulder.BoulderBuild boulderBuild = (Boulder.BoulderBuild)building;
        if (bl) {
            lastGain = (!boulderBuild.cracked ? 3 : 2) + Math.min(++streak / 2, 4) + (bl2 ? 2 : 0);
            earned += lastGain;
            banner = (bl2 ? "PERFECT! +" : "Hit! +") + lastGain + (String)(streak >= 2 ? "  x" + streak + " streak" : "");
            bannerColor = bl2 ? Pal.accent : boulderBuild.ore.color;
            flash = 1.0f;
            shake = bl2 ? 8.0f : 4.0f;
            for (int i = 0; i < (bl2 ? 26 : 14); ++i) {
                float f = Mathf.random((float)360.0f);
                float f2 = Mathf.random((float)1.5f, (float)5.0f);
                sparks.add((Object)new float[]{0.0f, 0.0f, Mathf.cosDeg((float)f) * f2, Mathf.sinDeg((float)f) * f2, 1.0f});
            }
        } else {
            streak = 0;
            shake = 10.0f;
            banner = "Miss!  (" + ++misses + "/3)";
            bannerColor = Pal.remove;
        }
        pulse = bl ? (bl2 ? 0.3f : 0.18f) : -0.12f;
        bannerT = 1.0f;
        MineGame.send(bl2 ? 3 : (bl ? 2 : 1));
        if (++round >= 6 || misses >= 3) {
            state = State.result;
            resultTime = 70.0f;
            banner = earned > 0 ? "Done! Got " + earned + " " + boulderBuild.ore.localizedName : "Nothing this time...";
            bannerColor = earned > 0 ? Pal.accent : Pal.remove;
        } else {
            MineGame.newRound();
        }
    }

    static void drawGame() {
        float f;
        float f2;
        int n;
        Building building;
        if (state == State.idle || target == null || !((building = MineGame.target.build) instanceof Boulder.BoulderBuild)) {
            return;
        }
        Boulder.BoulderBuild boulderBuild = (Boulder.BoulderBuild)building;
        float f3 = Core.scene.getWidth() / 2.0f + Mathf.range((float)shake);
        float f4 = Core.scene.getHeight() / 2.0f + Mathf.range((float)shake);
        float f5 = Core.scene.getWidth();
        float f6 = Core.scene.getHeight();
        Draw.color((float)0.0f, (float)0.0f, (float)0.0f, (float)0.5f);
        Fill.rect((float)(f5 / 2.0f), (float)(f6 / 2.0f), (float)f5, (float)f6);
        openT = Math.min(1.0f, openT + Time.delta / 22.0f);
        float f7 = MineGame.k() * Interp.swingOut.apply(openT);
        Draw.flush();
        Draw.trans((Mat)Tmp.m1.setToTranslation(f5 / 2.0f, f6 / 2.0f).scale(f7, f7).translate(-f5 / 2.0f, -f6 / 2.0f));
        float f8 = Math.min(170.0f, (f6 /= MineGame.k()) * 0.36f);
        int n2 = boulderBuild.variant();
        Draw.color((float)0.04f, (float)0.035f, (float)0.05f, (float)0.82f);
        Fill.rect((float)((f5 /= MineGame.k()) / 2.0f), (float)(f6 / 2.0f), (float)f5, (float)f6);
        float f9 = Math.min(f5 - 20.0f, f8 * 2.0f + 330.0f);
        float f10 = Math.min(f6 - 20.0f, f8 * 2.0f + 190.0f);
        Draw.color((float)0.13f, (float)0.115f, (float)0.14f);
        Fill.rect((float)f3, (float)f4, (float)f9, (float)f10);
        Draw.color((float)0.19f, (float)0.17f, (float)0.2f);
        for (n = 0; n < 14; ++n) {
            f2 = f3 - f9 / 2.0f + Mathf.randomSeed((long)((long)n * 7L), (float)0.0f, (float)1.0f) * f9;
            f = f4 - f10 / 2.0f + Mathf.randomSeed((long)((long)n * 13L + 1L), (float)0.0f, (float)1.0f) * f10;
            Fill.rect((float)f2, (float)f, (float)(30.0f + Mathf.randomSeed((long)((long)n * 3L), (float)0.0f, (float)40.0f)), (float)(14.0f + Mathf.randomSeed((long)((long)n * 5L), (float)0.0f, (float)20.0f)));
        }
        Draw.color((Color)boulderBuild.ore.color, (float)(0.1f + flash * 0.25f));
        Fill.circle((float)f3, (float)f4, (float)(f8 * 1.25f));
        for (n = 0; n < 8; ++n) {
            Draw.color((Color)boulderBuild.ore.color, (float)(0.08f + flash * 0.12f));
            Fill.tri((float)f3, (float)f4, (float)(f3 + Mathf.cosDeg((float)(Time.time * 0.6f + (float)n * 45.0f)) * f8 * 1.6f), (float)(f4 + Mathf.sinDeg((float)(Time.time * 0.6f + (float)n * 45.0f)) * f8 * 1.6f), (float)(f3 + Mathf.cosDeg((float)(Time.time * 0.6f + (float)n * 45.0f + 12.0f)) * f8 * 1.6f), (float)(f4 + Mathf.sinDeg((float)(Time.time * 0.6f + (float)n * 45.0f + 12.0f)) * f8 * 1.6f));
        }
        Draw.color((Color)Pal.darkestGray);
        Lines.stroke((float)5.0f);
        Lines.rect((float)(f3 - f9 / 2.0f), (float)(f4 - f10 / 2.0f), (float)f9, (float)f10);
        Draw.color((Color)boulderBuild.ore.color);
        Lines.stroke((float)2.0f);
        Lines.rect((float)(f3 - f9 / 2.0f + 6.0f), (float)(f4 - f10 / 2.0f + 6.0f), (float)(f9 - 12.0f), (float)(f10 - 12.0f));
        for (n = 0; n < 10; ++n) {
            f2 = f3 - f9 / 2.0f + Mathf.randomSeed((long)((long)n * 31L), (float)0.0f, (float)1.0f) * f9;
            f = (Time.time / 240.0f + Mathf.randomSeed((long)((long)n * 17L), (float)0.0f, (float)1.0f)) % 1.0f;
            Draw.color((float)0.7f, (float)0.65f, (float)0.6f, (float)(0.35f * (1.0f - f)));
            Fill.rect((float)f2, (float)(f4 + f10 / 2.0f - f * f10), (float)3.0f, (float)3.0f);
        }
        Draw.color((Color)Color.white);
        float f11 = f8 * (1.0f + pulse + (mode == 1 && charging ? Mathf.sin((float)Time.time, (float)3.0f, (float)0.02f) : 0.0f));
        Draw.rect((TextureRegion)boulderBuild.baseR(n2), (float)f3, (float)(f4 += Mathf.sin((float)Time.time, (float)30.0f, (float)3.0f) * (1.0f - flash)), (float)f11, (float)f11);
        if (!boulderBuild.cracked) {
            Draw.rect((TextureRegion)boulderBuild.sideR(n2), (float)f3, (float)f4, (float)f11, (float)f11);
            Draw.rect((TextureRegion)boulderBuild.topR(n2), (float)f3, (float)f4, (float)f11, (float)f11);
        } else {
            Draw.rect((TextureRegion)boulderBuild.crackR(n2), (float)f3, (float)f4, (float)f11, (float)f11);
        }
        if (state == State.play) {
            switch (mode) {
                case 0: {
                    f2 = f8 * 0.78f;
                    Lines.stroke((float)14.0f);
                    Draw.color((Color)Color.gray, (float)0.6f);
                    Lines.circle((float)f3, (float)f4, (float)f2);
                    Draw.color((Color)boulderBuild.ore.color);
                    Lines.arc((float)f3, (float)f4, (float)f2, (float)(zoneSize / 360.0f), (float)zoneStart);
                    Draw.color((Color)Color.white);
                    Lines.arc((float)f3, (float)f4, (float)f2, (float)(zoneSize * 0.28f / 360.0f), (float)(zoneStart + zoneSize * 0.36f));
                    f = f3 + Mathf.cosDeg((float)needle) * (f2 + 6.0f);
                    float f12 = f4 + Mathf.sinDeg((float)needle) * (f2 + 6.0f);
                    Draw.color((float)0.45f, (float)0.3f, (float)0.15f);
                    Lines.stroke((float)5.0f);
                    Lines.line((float)(f3 + Mathf.cosDeg((float)needle) * (f2 - 30.0f)), (float)(f4 + Mathf.sinDeg((float)needle) * (f2 - 30.0f)), (float)f, (float)f12);
                    Draw.color((Color)Color.lightGray);
                    Lines.stroke((float)7.0f);
                    float f13 = Mathf.cosDeg((float)(needle + 90.0f)) * 14.0f;
                    float f14 = Mathf.sinDeg((float)(needle + 90.0f)) * 14.0f;
                    Lines.line((float)(f - f13), (float)(f12 - f14), (float)(f + f13), (float)(f12 + f14));
                    break;
                }
                case 1: {
                    f2 = f3 + f8 * 0.5f + 70.0f;
                    f = f8 * 1.4f;
                    float f12 = f4 - f / 2.0f;
                    Draw.color((float)0.07f, (float)0.06f, (float)0.08f);
                    Fill.rect((float)f2, (float)f4, (float)30.0f, (float)(f + 8.0f));
                    Draw.color((Color)boulderBuild.ore.color, (float)0.8f);
                    Fill.rect((float)f2, (float)(f12 + (chargeLo + chargeSize / 2.0f) * f / 1.1f), (float)30.0f, (float)(chargeSize * f / 1.1f));
                    Draw.color((Color)Color.white);
                    Fill.rect((float)f2, (float)(f12 + (chargeLo + chargeSize / 2.0f) * f / 1.1f), (float)30.0f, (float)(chargeSize * 0.3f * f / 1.1f));
                    Draw.color((Color)Pal.accent);
                    Fill.rect((float)f2, (float)(f12 + Math.min(charge, 1.1f) * f / 1.1f), (float)44.0f, (float)6.0f);
                    Draw.color((Color)Pal.accent, (float)0.35f);
                    Fill.rect((float)f2, (float)(f12 + Math.min(charge, 1.1f) * f / 2.2f), (float)18.0f, (float)(Math.min(charge, 1.1f) * f / 1.1f));
                    Draw.color((Color)Color.white);
                    Lines.stroke((float)2.0f);
                    Lines.rect((float)(f2 - 15.0f), (float)(f12 - 4.0f), (float)30.0f, (float)(f + 8.0f));
                    if (charging) break;
                    Fonts.outline.setColor(Pal.accent);
                    Fonts.outline.draw((CharSequence)"HOLD", f2, f12 - 16.0f, 1);
                    break;
                }
                case 2: {
                    float f12;
                    f2 = Mathf.clamp((float)((float)taps / (float)need));
                    Draw.color((Color)boulderBuild.ore.color, (float)(f2 * 0.9f));
                    Lines.stroke((float)(2.0f + f2 * 4.0f));
                    for (int i = 0; i < 6; ++i) {
                        f12 = (float)i * 60.0f + 15.0f;
                        Lines.line((float)f3, (float)f4, (float)(f3 + Mathf.cosDeg((float)f12) * f8 * 0.55f * f2), (float)(f4 + Mathf.sinDeg((float)f12) * f8 * 0.55f * f2));
                    }
                    f = f8 * 1.6f;
                    f12 = f4 - f8 * 0.62f;
                    Draw.color((float)0.07f, (float)0.06f, (float)0.08f);
                    Fill.rect((float)f3, (float)f12, (float)f, (float)14.0f);
                    Draw.color((Color)boulderBuild.ore.color);
                    Fill.rect((float)(f3 - f / 2.0f + f * f2 / 2.0f), (float)f12, (float)(f * f2), (float)10.0f);
                    Draw.color((Color)Pal.accent);
                    Fill.rect((float)(f3 - f / 2.0f + f * Mathf.clamp((float)(timer / 150.0f)) / 2.0f), (float)(f12 - 14.0f), (float)(f * Mathf.clamp((float)(timer / 150.0f))), (float)5.0f);
                    Fonts.outline.setColor(Color.white);
                    Fonts.outline.draw((CharSequence)(taps + " / " + need), f3, f12 - 24.0f, 1);
                    break;
                }
                default: {
                    if (lit == 1) {
                        f2 = Mathf.clamp((float)(nLife / nMax));
                        f = 1.0f + Mathf.sin((float)Time.time, (float)5.0f, (float)0.15f);
                        Draw.color((Color)boulderBuild.ore.color, (float)0.35f);
                        Fill.circle((float)(f3 + nx), (float)(f4 + ny), (float)(40.0f * f));
                        Draw.color((Color)Color.white);
                        Fill.circle((float)(f3 + nx), (float)(f4 + ny), (float)16.0f);
                        Draw.color((Color)boulderBuild.ore.color);
                        Fill.circle((float)(f3 + nx), (float)(f4 + ny), (float)11.0f);
                        Lines.stroke((float)4.0f);
                        Draw.color((Color)Pal.accent);
                        Lines.arc((float)(f3 + nx), (float)(f4 + ny), (float)30.0f, (float)f2, (float)(90.0f - f2 * 360.0f));
                    }
                    Fonts.outline.setColor(Color.white);
                    Fonts.outline.draw((CharSequence)("Nuggets " + caught + "/" + nTotal), f3, f4 - f8 * 0.62f, 1);
                }
            }
        }
        for (float[] fArray : sparks) {
            Draw.color((Color)boulderBuild.ore.color, (Color)Color.white, (float)0.4f);
            Draw.alpha((float)fArray[4]);
            Fill.circle((float)(f3 + fArray[0] * 12.0f), (float)(f4 + fArray[1] * 12.0f), (float)(3.0f * fArray[4] + 1.0f));
        }
        Draw.reset();
        Fonts.outline.setColor(bannerColor);
        Fonts.outline.getData().setScale(1.0f + bannerT * 0.6f);
        Fonts.outline.draw((CharSequence)banner, f3, f4 + f10 / 2.0f - 14.0f, 1);
        Fonts.outline.getData().setScale(1.0f);
        Fonts.outline.setColor(Color.white);
        Fonts.outline.draw((CharSequence)("Round " + Math.min(round + 1, 6) + "/6    Streak " + streak + "    Ore +" + earned), f3, f4 - f10 / 2.0f + 52.0f, 1);
        Fonts.outline.draw((CharSequence)(state == State.play ? hint : ""), f3, f4 - f10 / 2.0f + 26.0f, 1);
        Draw.reset();
        Draw.flush();
        Draw.trans((Mat)Tmp.m1.idt());
    }

    static {
        banner = "";
        bannerColor = Color.white;
        sparks = new Seq();
        mode = -1;
        hint = "";
        modeNames = new String[]{"Needle", "Charge", "Rapid Tap", "Memory"};
    }

    static class Session {
        int pos;
        int round;
        int misses;
        int streak;
        int hits;
        long last;

        Session() {
        }
    }

    static enum State {
        idle,
        approach,
        play,
        result;

    }
}
