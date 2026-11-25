/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.Events
 *  arc.func.Cons
 *  arc.graphics.Color
 *  arc.graphics.g2d.Draw
 *  arc.graphics.g2d.Fill
 *  arc.graphics.g2d.Lines
 *  arc.graphics.g2d.TextureAtlas$AtlasRegion
 *  arc.graphics.g2d.TextureRegion
 *  arc.math.Angles
 *  arc.math.Mathf
 *  arc.math.geom.Position
 *  arc.math.geom.Vec2
 *  arc.struct.IntMap
 *  arc.struct.IntMap$Entry
 *  arc.struct.IntSeq
 *  arc.util.Time
 *  arc.util.Tmp
 *  mindustry.Vars
 *  mindustry.game.EventType$Trigger
 *  mindustry.gen.Bullet
 *  mindustry.gen.Groups
 *  mindustry.gen.Player
 *  mindustry.gen.Unit
 *  mindustry.graphics.Pal
 *  mindustry.net.Net
 */
package vintadustry;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.net.Net;
import vintadustry.AimPacket;
import vintadustry.BoulderOres;
import vintadustry.BranchUnit;
import vintadustry.CrateUnit;
import vintadustry.DeathScreen;
import vintadustry.DonaTree;
import vintadustry.DragPacket;
import vintadustry.DualUnit;
import vintadustry.GumpyUnit;
import vintadustry.HandPacket;
import vintadustry.MenuLobby;
import vintadustry.PullPacket;
import vintadustry.SlotDropPacket;
import vintadustry.StashPacket;
import vintadustry.TestMod;
import vintadustry.TreeBreakPacket;
import vintadustry.TreeFx;
import vintadustry.TreeSyncPacket;
import vintadustry.TrunkPacket;
import vintadustry.UnitMenu;

public class Hand {
    public static final float startReach = 110.0f;
    public static final float ripDist = 190.0f;
    static DonaTree.DonaTreeBuild gTree;
    static DonaTree.DonaTreeBuild hoverTree;
    static int gSlot;
    static int hoverSlot;
    static boolean locked;
    static float gripDist;
    public static final float pullNeeded = 40.0f;
    public static final float dragReach = 200.0f;
    static Unit gLoose;
    static Unit hoverLoose;
    static DonaTree.DonaTreeBuild gTrunk;
    static DonaTree.DonaTreeBuild hoverTrunk;
    static float trunkProg;
    public static final float trunkNeed = 200.0f;
    static final Vec2 camLock;
    static boolean camLocked;
    static boolean looseRotate;
    static float grabAngle;
    static float grabRot;
    static float dragSend;
    static boolean sentAim;
    static float aimSend;
    static float branchProg;
    public static final float branchNeeded = 30.0f;
    static float scanX;
    static float scanY;
    static Unit scanFrom;
    static Unit scanHit;
    static final Cons<Unit> looseScan;
    static TextureRegion animIcon;
    static int iconIdx;
    static final String[] iconNames;
    static Unit camUnit;
    static float camT;
    static boolean down;
    static boolean uiBlocked;
    static float mx;
    static float my;
    static int ptr;
    static long inputFrame;
    static final HandView local;
    static final IntMap<HandView> remote;
    static final IntSeq stale;
    static int lastSentMode;
    static float netT;

    static CrateUnit crateNear(Unit unit) {
        for (Unit unit2 : Groups.unit) {
            if (!(unit2 instanceof CrateUnit) || unit2.dead || unit2.team != unit.team || !unit2.within((Position)unit, unit2.hitSize / 2.0f + 14.0f)) continue;
            return (CrateUnit)unit2;
        }
        return null;
    }

    static void tryStash(Unit unit) {
        if (!(gLoose instanceof BranchUnit) || !gLoose.isAdded()) {
            return;
        }
        CrateUnit crateUnit = Hand.crateNear(gLoose);
        if (crateUnit == null) {
            return;
        }
        if (Vars.net.client()) {
            StashPacket stashPacket = new StashPacket();
            stashPacket.branchId = Hand.gLoose.id;
            stashPacket.crateId = crateUnit.id;
            stashPacket.x = Hand.gLoose.x;
            stashPacket.y = Hand.gLoose.y;
            Vars.net.send((Object)stashPacket, true);
        } else {
            StashPacket.apply(unit, (BranchUnit)gLoose, crateUnit);
        }
    }

    static void animateIcon() {
        int n;
        if (Vars.headless || TestMod.galho == null || TestMod.galho.fullIcon == null) {
            return;
        }
        if (animIcon == null) {
            TestMod.galho.fullIcon = animIcon = new TextureRegion(TestMod.galho.fullIcon);
            TestMod.galho.uiIcon = animIcon;
        }
        if ((n = (int)(Time.globalTime / 50.0f) % iconNames.length) != iconIdx) {
            iconIdx = n;
            TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find(iconNames[n]);
            if (atlasRegion.found()) {
                animIcon.set((TextureRegion)atlasRegion);
            }
        }
    }

    public static void init() {
        Net.registerPacket(StashPacket::new);
        Net.registerPacket(TreeSyncPacket::new);
        Net.registerPacket(TreeBreakPacket::new);
        Net.registerPacket(HandPacket::new);
        Net.registerPacket(SlotDropPacket::new);
        BoulderOres.init();
        DeathScreen.init();
        UnitMenu.init();
        MenuLobby.init();
        Events.run((Object)EventType.Trigger.update, Hand::update);
        Events.run((Object)EventType.Trigger.update, Hand::updateAim);
        Events.run((Object)EventType.Trigger.draw, Hand::draw);
        Events.run((Object)EventType.Trigger.draw, Hand::animateIcon);
        Events.run((Object)EventType.Trigger.preDraw, Hand::waitingCamera);
    }

    static void waitingCamera() {
        if (Vars.headless || Vars.player == null || !Vars.state.isGame() || !Vars.player.dead()) {
            camUnit = null;
            return;
        }
        Bullet bullet2 = (Bullet)Groups.bullet.find(bullet -> (bullet.type == TestMod.gumpyShot || bullet.type == TestMod.gumpyShotVisual) && bullet.team == Vars.player.team());
        if (bullet2 != null) {
            Core.camera.position.set(bullet2.x, bullet2.y);
            return;
        }
        camT += Time.delta;
        if (camUnit == null || !camUnit.isAdded() || Hand.camUnit.dead || camT >= 15.0f) {
            camT = 0.0f;
            camUnit = (Unit)Groups.unit.find(unit -> {
                if (!(unit instanceof GumpyUnit)) {
                    return false;
                }
                GumpyUnit gumpyUnit = (GumpyUnit)((Object)unit);
                return gumpyUnit.flungTime > 0.0f && gumpyUnit.team == Vars.player.team();
            });
            if (camUnit == null) {
                camUnit = (Unit)Groups.unit.find(unit -> {
                    DualUnit dualUnit;
                    return unit instanceof DualUnit && (dualUnit = (DualUnit)((Object)unit)).isTasky() && dualUnit.team == Vars.player.team() && !dualUnit.dead;
                });
            }
        }
        if (camUnit != null) {
            Core.camera.position.set(Hand.camUnit.x, Hand.camUnit.y);
        }
    }

    static Unit tasky() {
        if (Vars.headless || Vars.player == null) {
            return null;
        }
        Unit unit = Vars.player.unit();
        return unit != null && !unit.dead && unit.isAdded() ? unit : null;
    }

    static void updateAim() {
        DualUnit dualUnit;
        Hand.readInput();
        if (Vars.headless || !Vars.state.isGame() || Vars.player == null) {
            return;
        }
        Unit unit = Vars.player.unit();
        if (!(unit instanceof DualUnit) || !(dualUnit = (DualUnit)unit).isTasky() || dualUnit.dead) {
            return;
        }
        boolean bl = down && !uiBlocked && gTree == null && gLoose == null && hoverTree == null && hoverLoose == null && gTrunk == null && hoverTrunk == null && !locked;
        float f = Angles.angle((float)dualUnit.x, (float)dualUnit.y, (float)mx, (float)my);
        dualUnit.aiming = bl;
        dualUnit.aimAng = f;
        if (Vars.net.client()) {
            aimSend += Time.delta;
            if (bl != sentAim || bl && aimSend >= 5.0f) {
                AimPacket aimPacket = new AimPacket();
                aimPacket.on = bl;
                aimPacket.angle = f;
                Vars.net.send((Object)aimPacket, bl != sentAim);
                sentAim = bl;
                aimSend = 0.0f;
            }
        }
    }

    static void update() {
        float f;
        float f2;
        Object object;
        Hand.readInput();
        if (!Vars.state.isGame()) {
            return;
        }
        Unit unit = Hand.tasky();
        boolean bl = down;
        if (!bl) {
            locked = false;
        }
        if (unit == null) {
            gTree = null;
            hoverTree = null;
            return;
        }
        if (!bl) {
            gTree = null;
            gTrunk = null;
            trunkProg = 0.0f;
        }
        if (Vars.mobile && bl && (gTree != null || gLoose != null || hoverTree != null || hoverLoose != null || gTrunk != null || hoverTrunk != null)) {
            if (!camLocked) {
                camLock.set(Core.camera.position);
                camLocked = true;
            }
            Core.camera.position.set(camLock);
        } else {
            camLocked = false;
        }
        if (!bl && gLoose != null) {
            if (Vars.net.client() && gLoose.isAdded()) {
                object = new DragPacket();
                ((DragPacket)((Object)object)).id = Hand.gLoose.id;
                ((DragPacket)((Object)object)).x = Hand.gLoose.x;
                ((DragPacket)((Object)object)).y = Hand.gLoose.y;
                ((DragPacket)((Object)object)).rot = Hand.gLoose.rotation;
                Vars.net.send(object, true);
            }
            Hand.tryStash(unit);
        }
        if (!bl || gLoose != null && (!gLoose.isAdded() || Hand.gLoose.dead)) {
            gLoose = null;
        }
        if (gLoose != null) {
            float f3 = mx;
            float f4 = my;
            float f5 = Hand.gLoose.x;
            float f6 = Hand.gLoose.y;
            float f7 = Hand.gLoose.rotation;
            if (looseRotate) {
                Hand.gLoose.rotation = f7 = grabRot + (Angles.angle((float)Hand.gLoose.x, (float)Hand.gLoose.y, (float)f3, (float)f4) - grabAngle);
            } else {
                Tmp.v2.set(f3, f4).sub(unit.x, unit.y).limit(200.0f).add(unit.x, unit.y);
                f5 = Mathf.approach((float)Hand.gLoose.x, (float)Tmp.v2.x, (float)(4.0f * Time.delta));
                f6 = Mathf.approach((float)Hand.gLoose.y, (float)Tmp.v2.y, (float)(4.0f * Time.delta));
                gLoose.set(f5, f6);
            }
            dragSend += Time.delta;
            if (Vars.net.client() && dragSend >= 3.0f) {
                dragSend = 0.0f;
                DragPacket dragPacket = new DragPacket();
                dragPacket.id = Hand.gLoose.id;
                dragPacket.x = f5;
                dragPacket.y = f6;
                dragPacket.rot = f7;
                Vars.net.send((Object)dragPacket, false);
            }
            return;
        }
        if (Vars.mobile && !bl) {
            hoverLoose = null;
            hoverTree = null;
            hoverTrunk = null;
            return;
        }
        hoverLoose = null;
        if (gTree == null && !locked) {
            f2 = mx;
            f = my;
            scanX = f2;
            scanY = f;
            scanFrom = unit;
            scanHit = null;
            float f8 = Vars.mobile ? 30.0f : 20.0f;
            Groups.unit.intersect(f2 - f8, f - f8, f8 * 2.0f, f8 * 2.0f, looseScan);
            hoverLoose = scanHit;
            if (hoverLoose != null) {
                hoverTree = null;
                if (bl && !uiBlocked) {
                    float f9;
                    gLoose = hoverLoose;
                    if (gLoose instanceof BranchUnit) {
                        ((BranchUnit)gLoose).cancelFlight();
                    }
                    float f10 = mx;
                    float f11 = my;
                    looseRotate = Mathf.dst((float)f10, (float)f9, (float)Hand.gLoose.x, (float)Hand.gLoose.y) > 12.0f;
                    grabAngle = Angles.angle((float)Hand.gLoose.x, (float)Hand.gLoose.y, (float)f10, (float)f11);
                    grabRot = Hand.gLoose.rotation;
                }
                return;
            }
        }
        if (gTree != null) {
            if (!gTree.isValid() || Hand.gTree.growth[gSlot] < 1.0f) {
                gTree = null;
                return;
            }
            object = gTree.tip(gSlot, Tmp.v4);
            f = Mathf.dst((float)mx, (float)my, (float)((Vec2)object).x, (float)((Vec2)object).y);
            float f12 = mx - Hand.gTree.x;
            float f13 = my - Hand.gTree.y;
            float f14 = Math.max(1.0f, Mathf.len((float)f12, (float)f13));
            float f15 = Mathf.clamp((float)(f / 40.0f));
            branchProg = f >= 24.0f ? (branchProg += Time.delta * Mathf.clamp((float)((f - 14.0f) / 40.0f))) : Math.max(0.0f, branchProg - Time.delta * 2.0f);
            f15 = Math.max(f15 * 0.6f, Mathf.clamp((float)(branchProg / 30.0f)));
            Hand.gTree.shake = Math.max(Hand.gTree.shake, 0.7f * Mathf.clamp((float)(branchProg / 30.0f)));
            Hand.gTree.fx += f12 / f14 * f15 * 0.4f;
            Hand.gTree.fy += f13 / f14 * f15 * 0.4f;
            Hand.gTree.pullSlot = gSlot;
            Hand.gTree.pullAmt = f15;
            Hand.gTree.pullAng = Angles.angle((float)Hand.gTree.x, (float)Hand.gTree.y, (float)mx, (float)my);
            Hand.gTree.pullStamp = Time.time;
            if (!unit.within((Position)object, 230.0f)) {
                gTree = null;
                return;
            }
            if (branchProg >= 30.0f) {
                branchProg = 0.0f;
                if (Vars.net.client()) {
                    PullPacket pullPacket = new PullPacket();
                    pullPacket.pos = Hand.gTree.tile.pos();
                    pullPacket.slot = (byte)gSlot;
                    Vars.net.send((Object)pullPacket, true);
                    Hand.gTree.growth[Hand.gSlot] = 0.0f;
                    TreeFx.snap.at(((Vec2)object).x, ((Vec2)object).y, 90.0f + 45.0f * (float)gSlot, gTree.leafColor());
                } else {
                    gTree.rip(gSlot, unit);
                }
                gTree = null;
                locked = true;
            }
            return;
        }
        if (gTrunk != null) {
            if (!(gTrunk.isValid() && gTrunk.trunkFree() && unit.within((Position)gTrunk, 230.0f))) {
                gTrunk = null;
                trunkProg = 0.0f;
                return;
            }
            f2 = Mathf.dst((float)mx, (float)my, (float)Hand.gTrunk.x, (float)Hand.gTrunk.y);
            float f16 = mx - Hand.gTrunk.x;
            float f17 = my - Hand.gTrunk.y;
            float f18 = Math.max(1.0f, Mathf.len((float)f16, (float)f17));
            float f19 = Mathf.clamp((float)(f2 / 60.0f)) * (0.5f + 0.5f * Mathf.clamp((float)(trunkProg / 200.0f)));
            Hand.gTrunk.fx += f16 / f18 * f19 * 0.6f;
            Hand.gTrunk.fy += f17 / f18 * f19 * 0.6f;
            if (f2 >= 40.0f) {
                Hand.gTrunk.shake = Math.max(Hand.gTrunk.shake, 0.4f + 2.2f * (trunkProg += Time.delta) / 200.0f);
                if (trunkProg % 24.0f < Time.delta) {
                    TreeFx.snap.at(Hand.gTrunk.x, Hand.gTrunk.y - 4.0f, 90.0f, gTrunk.leafColor());
                }
            } else {
                trunkProg = Math.max(0.0f, trunkProg - Time.delta * 2.0f);
            }
            if (trunkProg >= 200.0f) {
                if (Vars.net.client()) {
                    TrunkPacket trunkPacket = new TrunkPacket();
                    trunkPacket.pos = Hand.gTrunk.tile.pos();
                    Vars.net.send((Object)trunkPacket, true);
                } else {
                    gTrunk.ripTrunk(unit);
                }
                gTrunk = null;
                trunkProg = 0.0f;
                locked = true;
            }
            return;
        }
        hoverTree = null;
        hoverTrunk = null;
        f2 = mx;
        f = my;
        for (DonaTree.DonaTreeBuild donaTreeBuild2 : DonaTree.trees) {
            if (!donaTreeBuild2.isValid()) continue;
            for (int i = 0; i < 8; ++i) {
                if (donaTreeBuild2.growth[i] < 1.0f) continue;
                Vec2 vec2 = donaTreeBuild2.tip(i, Tmp.v4);
                if (!Mathf.within((float)vec2.x, (float)vec2.y, (float)f2, (float)f, (float)(Vars.mobile ? 26.0f : 16.0f)) || !unit.within((Position)vec2, Vars.mobile ? 150.0f : 110.0f)) continue;
                hoverTree = donaTreeBuild2;
                hoverSlot = i;
            }
        }
        DonaTree.trees.removeAll(donaTreeBuild -> !donaTreeBuild.isValid());
        if (hoverTree == null) {
            for (DonaTree.DonaTreeBuild donaTreeBuild2 : DonaTree.trees) {
                if (!donaTreeBuild2.isValid() || !donaTreeBuild2.trunkFree() || !Mathf.within((float)donaTreeBuild2.x, (float)donaTreeBuild2.y, (float)f2, (float)f, (float)(Vars.mobile ? 26.0f : 14.0f)) || !unit.within((Position)donaTreeBuild2, Vars.mobile ? 150.0f : 110.0f)) continue;
                hoverTrunk = donaTreeBuild2;
            }
            if (hoverTrunk != null && bl && !locked && !uiBlocked) {
                gTrunk = hoverTrunk;
                trunkProg = 0.0f;
                return;
            }
        }
        if (hoverTree != null && bl && !locked && !uiBlocked) {
            gTree = hoverTree;
            branchProg = 0.0f;
            gSlot = hoverSlot;
            gripDist = unit.dst((Position)gTree.tip(gSlot, Tmp.v4));
        }
    }

    static void readInput() {
        long l = Core.graphics.getFrameId();
        if (l == inputFrame) {
            return;
        }
        inputFrame = l;
        if (Vars.mobile) {
            if (ptr >= 0 && !Core.input.isTouched(ptr)) {
                ptr = -1;
            }
            if (ptr < 0) {
                for (int i = 0; i < 6; ++i) {
                    if (!Core.input.isTouched(i) || Core.scene.hasMouse((float)Core.input.mouseX(i), (float)Core.input.mouseY(i))) continue;
                    ptr = i;
                    break;
                }
            }
            boolean bl = down = ptr >= 0;
            if (down) {
                Vec2 vec2 = Core.input.mouseWorld((float)Core.input.mouseX(ptr), (float)Core.input.mouseY(ptr));
                mx = vec2.x;
                my = vec2.y;
            }
            uiBlocked = false;
        } else {
            down = Core.input.isTouched();
            mx = Core.input.mouseWorldX();
            my = Core.input.mouseWorldY();
            uiBlocked = Core.scene.hasMouse();
        }
    }

    public static void remoteUpdate(HandPacket handPacket) {
        if (Vars.headless || Vars.player == null || handPacket.pid == Vars.player.id()) {
            return;
        }
        HandView handView = (HandView)remote.get(handPacket.pid);
        if (handView == null) {
            handView = new HandView();
            remote.put(handPacket.pid, (Object)handView);
        }
        handView.set(handPacket.mode, handPacket.x, handPacket.y, handPacket.tension, handPacket.tension * 2.0f, Pal.accent);
        handView.lastNet = Time.time;
    }

    static void sendHand(int n, float f, float f2, float f3) {
        boolean bl;
        if (!Vars.net.active() || Vars.player == null) {
            return;
        }
        netT += Time.delta;
        boolean bl2 = bl = n != lastSentMode;
        if (!bl && (n == 0 || netT < 6.0f)) {
            return;
        }
        netT = 0.0f;
        lastSentMode = n;
        HandPacket handPacket = new HandPacket();
        handPacket.pid = Vars.player.id();
        handPacket.mode = (byte)n;
        handPacket.x = f;
        handPacket.y = f2;
        handPacket.tension = f3;
        handPacket.rel = bl;
        Vars.net.send((Object)handPacket, bl);
    }

    static void draw() {
        if (Vars.headless || !Vars.state.isGame()) {
            return;
        }
        Draw.z((float)110.0f);
        Unit unit = Hand.tasky();
        if (unit != null) {
            Object object;
            int n = 0;
            float f = unit.x;
            float f2 = unit.y;
            float f3 = 0.0f;
            float f4 = 0.0f;
            float f5 = 0.0f;
            float f6 = Mathf.absin((float)Time.time, (float)5.0f, (float)1.5f);
            if (gLoose != null) {
                n = 2;
                f = Hand.gLoose.x;
                f2 = Hand.gLoose.y;
                f3 = 0.7f;
            } else if (hoverLoose != null) {
                n = 1;
                f = Hand.hoverLoose.x;
                f2 = Hand.hoverLoose.y + f6;
            } else if (gTree != null) {
                object = gTree.tip(gSlot, Tmp.v4);
                f5 = Mathf.clamp((float)(Mathf.dst((float)mx, (float)my, (float)object.x, (float)object.y) / 40.0f));
                n = 2;
                f = object.x + (mx - object.x) * 0.5f;
                f2 = object.y + (my - object.y) * 0.5f;
                f3 = f5;
                f4 = f5 * 2.0f;
            } else if (gTrunk != null) {
                f5 = Mathf.clamp((float)(trunkProg / 200.0f));
                n = 2;
                f = Hand.gTrunk.x;
                f2 = Hand.gTrunk.y;
                f3 = f5;
                f4 = 0.5f + f5 * 2.5f;
            } else if (hoverTrunk != null) {
                n = 1;
                f = Hand.hoverTrunk.x;
                f2 = Hand.hoverTrunk.y + f6;
            } else if (hoverTree != null) {
                object = hoverTree.tip(hoverSlot, Tmp.v4);
                n = 1;
                f = object.x;
                f2 = object.y + f6;
            }
            local.set(n, f, f2, f3, f4, Tmp.c1.set(Pal.accent).lerp(Color.scarlet, f5));
            local.update(unit);
            local.draw(unit);
            Hand.sendHand(n, f, f2, f3);
            if (gTrunk != null) {
                Draw.color((Color)Pal.accent, (Color)Color.scarlet, (float)f5);
                Lines.stroke((float)3.0f);
                Lines.arc((float)Hand.gTrunk.x, (float)Hand.gTrunk.y, (float)22.0f, (float)f5, (float)90.0f);
            }
            if (gLoose instanceof BranchUnit && gLoose.isAdded() && (object = Hand.crateNear(gLoose)) != null) {
                Draw.color((Color)Pal.accent);
                Lines.stroke((float)2.0f);
                Lines.circle((float)object.x, (float)object.y, (float)(object.hitSize / 2.0f + 8.0f + Mathf.absin((float)Time.time, (float)6.0f, (float)2.0f)));
            }
        } else {
            Hand.local.mode = 0;
            Hand.sendHand(0, 0.0f, 0.0f, 0.0f);
        }
        stale.clear();
        for (IntMap.Entry entry : remote.entries()) {
            Unit unit2;
            HandView handView = (HandView)entry.value;
            Player player = (Player)Groups.player.getByID(entry.key);
            Unit unit3 = unit2 = player == null ? null : player.unit();
            if (Time.time - handView.lastNet > 45.0f) {
                handView.mode = 0;
            }
            if (unit2 == null || unit2.dead || !unit2.isAdded()) {
                stale.add(entry.key);
                continue;
            }
            handView.update(unit2);
            if (handView.mode == 0 && handView.alpha < 0.03f) {
                stale.add(entry.key);
                continue;
            }
            handView.draw(unit2);
        }
        for (int i = 0; i < Hand.stale.size; ++i) {
            remote.remove(stale.get(i));
        }
        Draw.reset();
    }

    static {
        looseScan = unit -> {
            if (unit instanceof BranchUnit && unit.team == Hand.scanFrom.team && unit.within(scanX, scanY, Vars.mobile ? 30.0f : 20.0f) && scanFrom.within((Position)unit, 200.0f)) {
                scanHit = unit;
            }
        };
        iconIdx = -1;
        iconNames = new String[]{"vintadustry-maple-galho", "vintadustry-cherry-galho", "vintadustry-leafy-galho", "vintadustry-palm-galho", "vintadustry-dead-galho"};
        ptr = -1;
        inputFrame = -1L;
        local = new HandView();
        remote = new IntMap();
        stale = new IntSeq();
        lastSentMode = -1;
        camLock = new Vec2();
    }

    static class HandView {
        float x;
        float y;
        float alpha;
        float solid = 0.45f;
        float closed;
        float tension;
        float jitter;
        float tx;
        float ty;
        int mode;
        boolean inited;
        float lastNet;
        final Color color = new Color(Pal.accent);

        HandView() {
        }

        void set(int n, float f, float f2, float f3, float f4, Color color) {
            this.mode = n;
            this.tx = f;
            this.ty = f2;
            this.jitter = f4;
            this.color.set(color);
            this.tension = Mathf.lerpDelta((float)this.tension, (float)f3, (float)0.2f);
        }

        void update(Unit unit) {
            this.alpha = Mathf.lerpDelta((float)this.alpha, (float)(this.mode == 0 ? 0.0f : 1.0f), (float)(this.mode == 0 ? 0.14f : 0.22f));
            if (this.alpha < 0.03f && this.mode == 0) {
                this.inited = false;
                return;
            }
            if (!this.inited) {
                this.x = unit.x;
                this.y = unit.y;
                this.inited = true;
            }
            if (this.mode == 0) {
                this.x = Mathf.lerpDelta((float)this.x, (float)unit.x, (float)0.16f);
                this.y = Mathf.lerpDelta((float)this.y, (float)unit.y, (float)0.16f);
            } else {
                float f = this.mode == 2 ? 0.45f : 0.26f;
                this.x = Mathf.lerpDelta((float)this.x, (float)this.tx, (float)f);
                this.y = Mathf.lerpDelta((float)this.y, (float)this.ty, (float)f);
            }
            this.closed = Mathf.lerpDelta((float)this.closed, (float)(this.mode == 2 ? 1.0f : 0.0f), (float)0.3f);
            this.solid = Mathf.lerpDelta((float)this.solid, (float)(this.mode == 2 ? 1.0f : 0.45f), (float)0.2f);
        }

        static void arm(float f, float f2, float f3, float f4, float f5, float f6, int n) {
            Lines.beginLine();
            for (int i = 0; i <= n; ++i) {
                float f7 = (float)i / (float)n;
                float f8 = f6 * Mathf.sin((float)(f7 * (float)Math.PI)) * (1.0f + 0.25f * Mathf.sin((float)(Time.time * 0.12f + f7 * 5.0f)));
                Lines.linePoint((float)(Mathf.lerp((float)f, (float)f3, (float)f7) + Angles.trnsx((float)f5, (float)f8)), (float)(Mathf.lerp((float)f2, (float)f4, (float)f7) + Angles.trnsy((float)f5, (float)f8)));
            }
            Lines.endLine();
        }

        void draw(Unit unit) {
            if (this.alpha < 0.03f) {
                return;
            }
            float f = Angles.angle((float)unit.x, (float)unit.y, (float)this.x, (float)this.y);
            float f2 = unit.x + Angles.trnsx((float)f, (float)(unit.hitSize / 2.0f));
            float f3 = unit.y + Angles.trnsy((float)f, (float)(unit.hitSize / 2.0f));
            float f4 = this.x - Angles.trnsx((float)f, (float)2.0f) + Mathf.range((float)(this.jitter * 0.5f));
            float f5 = this.y - Angles.trnsy((float)f, (float)2.0f) + Mathf.range((float)(this.jitter * 0.5f));
            float f6 = Mathf.dst((float)f2, (float)f3, (float)f4, (float)f5);
            float f7 = (1.0f - Mathf.clamp((float)this.tension)) * Math.min(9.0f, f6 * 0.14f);
            int n = Math.max(4, Math.min(14, (int)(f6 / 8.0f)));
            float f8 = this.alpha * this.solid;
            Draw.color((Color)this.color, (float)(0.5f * f8));
            Lines.stroke((float)4.0f);
            HandView.arm(f2, f3, f4, f5, f + 90.0f, f7, n);
            Draw.color((Color)Color.white, (float)f8);
            Lines.stroke((float)1.5f);
            HandView.arm(f2, f3, f4, f5, f + 90.0f, f7, n);
            Draw.color((Color)this.color, (float)this.alpha);
            Fill.circle((float)f2, (float)f3, (float)((4.0f + Mathf.absin((float)Time.time, (float)6.0f, (float)0.8f)) * this.alpha));
            Draw.color((Color)Color.white, (float)this.alpha);
            Fill.circle((float)f2, (float)f3, (float)(2.0f * this.alpha));
            Draw.color((float)1.0f, (float)1.0f, (float)1.0f, (float)Mathf.clamp((float)(this.alpha * (0.6f + 0.4f * this.solid))));
            float f9 = 22.0f * (1.0f - 0.14f * this.closed) * (0.7f + 0.3f * this.alpha);
            Draw.rect((TextureRegion)(this.closed > 0.5f ? DonaTree.handClosed : DonaTree.handOpen), (float)f4, (float)f5, (float)f9, (float)f9, (float)(f - 90.0f));
            Draw.color();
        }
    }
}
