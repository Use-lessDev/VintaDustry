/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.Events
 *  arc.graphics.Color
 *  arc.graphics.g2d.Draw
 *  arc.graphics.g2d.Lines
 *  arc.graphics.g2d.TextureAtlas$AtlasRegion
 *  arc.graphics.g2d.TextureRegion
 *  arc.math.Angles
 *  arc.math.Mathf
 *  arc.math.geom.Position
 *  arc.scene.ui.layout.Scl
 *  arc.util.Time
 *  arc.util.Tmp
 *  mindustry.Vars
 *  mindustry.content.Items
 *  mindustry.content.UnitTypes
 *  mindustry.core.Logic
 *  mindustry.entities.units.AIController
 *  mindustry.game.EventType$Trigger
 *  mindustry.game.EventType$UnitDestroyEvent
 *  mindustry.game.EventType$WorldLoadEvent
 *  mindustry.game.Team
 *  mindustry.gen.EntityMapping
 *  mindustry.gen.Groups
 *  mindustry.gen.Unit
 *  mindustry.graphics.Drawf
 *  mindustry.graphics.MultiPacker
 *  mindustry.graphics.Pal
 *  mindustry.mod.Mod
 *  mindustry.net.Net
 *  mindustry.type.Category
 *  mindustry.type.Item
 *  mindustry.type.ItemStack
 *  mindustry.type.UnitType
 *  mindustry.ui.Fonts
 *  mindustry.world.Block
 *  mindustry.world.blocks.environment.Floor
 *  mindustry.world.meta.BuildVisibility
 */
package vintadustry;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.scene.ui.layout.Scl;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.core.Logic;
import mindustry.entities.units.AIController;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.EntityMapping;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.net.Net;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.BuildVisibility;
import vintadustry.AimPacket;
import vintadustry.Boulder;
import vintadustry.BranchUnit;
import vintadustry.CrateTransferPacket;
import vintadustry.CrateUI;
import vintadustry.CrateUnit;
import vintadustry.DonaTree;
import vintadustry.DragPacket;
import vintadustry.DualUnit;
import vintadustry.GrayHook;
import vintadustry.GumpyShot;
import vintadustry.GumpyUnit;
import vintadustry.Hand;
import vintadustry.HookPacket;
import vintadustry.MineGame;
import vintadustry.MinePacket;
import vintadustry.PullPacket;
import vintadustry.Rope;
import vintadustry.TaskyNest;
import vintadustry.TreeFx;
import vintadustry.TrunkPacket;

public class TestMod
extends Mod {
    public static Item galho;
    public static UnitType looseBranch;
    public static GumpyShot gumpyShot;
    public static GumpyShot gumpyShotVisual;
    public static Block donaTree;
    public static UnitType tasky;
    public static UnitType crate;
    public static UnitType gumpy;
    public static TaskyNest nest;
    static Unit holdCrate;
    static float holdTime;
    static boolean holdDone;

    public TestMod() {
        Net.registerPacket(HookPacket::new);
        Net.registerPacket(PullPacket::new);
        Net.registerPacket(DragPacket::new);
        Net.registerPacket(AimPacket::new);
        Net.registerPacket(CrateTransferPacket::new);
        Net.registerPacket(MinePacket::new);
        Net.registerPacket(TrunkPacket::new);
        MineGame.init();
        Hand.init();
        Events.run((Object)EventType.Trigger.update, TestMod::updateHold);
        Events.on(EventType.WorldLoadEvent.class, worldLoadEvent -> {
            if (!Vars.net.client()) {
                TaskyNest.onWorldLoad();
            }
        });
        Events.on(EventType.UnitDestroyEvent.class, unitDestroyEvent -> {
            DualUnit dualUnit;
            Unit unit = unitDestroyEvent.unit;
            if (unit instanceof DualUnit && (dualUnit = (DualUnit)unit).isTasky() && !Vars.net.client() && Vars.state.isGame()) {
                Logic.gameOver((Team)Vars.state.rules.waveTeam);
            }
        });
    }

    public static boolean canHaul(Unit unit) {
        return unit instanceof DualUnit && !unit.dead;
    }

    public static void applyTransfer(DualUnit dualUnit, Unit unit, int n, int n2, boolean bl) {
        DualUnit dualUnit2;
        if (unit.dead || !dualUnit.within((Position)unit, 200.0f) || unit == dualUnit) {
            return;
        }
        if (unit instanceof CrateUnit) {
            CrateUnit crateUnit = (CrateUnit)unit;
            if (n < 0 || n >= crateUnit.stacks.length) {
                return;
            }
            ItemStack itemStack = crateUnit.stacks[n];
            if (bl) {
                if (itemStack.amount <= 0 || itemStack.item == null || !dualUnit.acceptsItem(itemStack.item)) {
                    return;
                }
                int n3 = Math.min(itemStack.amount, dualUnit.maxAccepted(itemStack.item));
                if (n3 <= 0) {
                    return;
                }
                dualUnit.addItem(itemStack.item, n3);
                itemStack.amount -= n3;
                if (itemStack.amount <= 0) {
                    itemStack.item = null;
                }
            } else {
                if (n2 < 0 || n2 >= dualUnit.ownSlots()) {
                    return;
                }
                ItemStack itemStack2 = dualUnit.slot(n2);
                if (itemStack2.amount <= 0 || itemStack2.item == null) {
                    return;
                }
                if (itemStack.amount > 0 && itemStack.item != itemStack2.item) {
                    return;
                }
                int n4 = Math.min(itemStack2.amount, crateUnit.type.itemCapacity - itemStack.amount);
                if (n4 <= 0) {
                    return;
                }
                itemStack.item = itemStack2.item;
                itemStack.amount += n4;
                itemStack2.amount -= n4;
                if (itemStack2.amount <= 0) {
                    itemStack2.item = null;
                }
            }
        } else if (unit instanceof DualUnit && (dualUnit2 = (DualUnit)unit).isTasky()) {
            if (bl) {
                if (n < 0 || n >= dualUnit2.ownSlots()) {
                    return;
                }
                ItemStack itemStack = dualUnit2.slot(n);
                if (itemStack.amount <= 0 || itemStack.item == null || !dualUnit.acceptsItem(itemStack.item)) {
                    return;
                }
                int n5 = Math.min(itemStack.amount, dualUnit.maxAccepted(itemStack.item));
                if (n5 <= 0) {
                    return;
                }
                dualUnit.addItem(itemStack.item, n5);
                itemStack.amount -= n5;
                if (itemStack.amount <= 0) {
                    itemStack.item = null;
                }
            } else {
                if (n2 < 0 || n2 >= dualUnit.ownSlots()) {
                    return;
                }
                ItemStack itemStack = dualUnit.slot(n2);
                if (itemStack.amount <= 0 || itemStack.item == null || !dualUnit2.acceptsItem(itemStack.item)) {
                    return;
                }
                int n6 = Math.min(itemStack.amount, dualUnit2.maxAccepted(itemStack.item));
                if (n6 <= 0) {
                    return;
                }
                dualUnit2.addItem(itemStack.item, n6);
                itemStack.amount -= n6;
                if (itemStack.amount <= 0) {
                    itemStack.item = null;
                }
            }
        }
    }

    static void updateHold() {
        if (Vars.headless || !Vars.state.isGame() || Vars.player == null) {
            return;
        }
        if (!Core.input.isTouched()) {
            if (holdCrate != null && (holdCrate instanceof CrateUnit || holdCrate instanceof DualUnit) && !holdDone && holdTime > 0.0f && holdTime < 20.0f && TestMod.canHaul(Vars.player.unit()) && Vars.player.unit().within((Position)holdCrate, 180.0f)) {
                CrateUI.open(holdCrate);
            }
            holdCrate = null;
            holdTime = 0.0f;
            holdDone = false;
            return;
        }
        if (holdDone || Core.scene.hasMouse() || !TestMod.canHaul(Vars.player.unit())) {
            return;
        }
        DualUnit dualUnit = (DualUnit)Vars.player.unit();
        float f = Core.input.mouseWorldX();
        float f2 = Core.input.mouseWorldY();
        Unit unit2 = (Unit)Groups.unit.find(unit -> {
            DualUnit dualUnit2;
            return (unit instanceof CrateUnit || dualUnit.isTasky() && unit instanceof GumpyUnit || dualUnit instanceof GumpyUnit && unit instanceof DualUnit && (dualUnit2 = (DualUnit)((Object)unit)).isTasky()) && unit != dualUnit && unit.team == dualUnit.team && !unit.dead && unit.within(f, f2, unit.hitSize / 2.0f + 3.0f) && (!(unit instanceof DualUnit) || unit.within((Position)dualUnit, unit.hitSize + 60.0f));
        });
        if (holdCrate == null) {
            holdCrate = unit2;
            holdTime = 0.0f;
            return;
        }
        if (unit2 != holdCrate) {
            holdCrate = null;
            holdTime = 0.0f;
            return;
        }
        if ((holdTime += Time.delta) >= 30.0f) {
            holdDone = true;
            if (Vars.net.client()) {
                HookPacket hookPacket = new HookPacket();
                hookPacket.targetId = TestMod.holdCrate.id;
                Vars.net.send((Object)hookPacket, true);
            } else {
                TestMod.applyHook(dualUnit, holdCrate);
            }
            holdCrate = null;
        }
    }

    public static void applyHook(DualUnit dualUnit, Unit unit) {
        Object object;
        if (unit instanceof DualUnit && ((DualUnit)((Object)(object = (DualUnit)unit))).isTasky() && dualUnit instanceof GumpyUnit) {
            GumpyUnit gumpyUnit = (GumpyUnit)dualUnit;
            if (gumpyUnit.within((Position)object, ((DualUnit)((Object)object)).hitSize + 60.0f)) {
                ((DualUnit)((Object)object)).swallow(gumpyUnit);
            }
            return;
        }
        if (unit instanceof CrateUnit) {
            object = (CrateUnit)unit;
            if (((CrateUnit)((Object)object)).owner == dualUnit) {
                dualUnit.crates.remove(object);
                ((CrateUnit)((Object)object)).owner = null;
                ((CrateUnit)((Object)object)).leader = null;
            } else if (dualUnit.crates.size < dualUnit.maxCrates()) {
                if (((CrateUnit)((Object)object)).owner != null) {
                    ((CrateUnit)((Object)object)).owner.crates.remove(object);
                }
                ((CrateUnit)((Object)object)).owner = dualUnit;
                dualUnit.crates.add(object);
            }
        } else if (unit instanceof GumpyUnit) {
            GumpyUnit gumpyUnit = (GumpyUnit)unit;
            if (dualUnit.isTasky()) {
                if (gumpyUnit.tower == dualUnit) {
                    dualUnit.towed.remove((Object)gumpyUnit);
                    gumpyUnit.tower = null;
                    gumpyUnit.leader = null;
                } else if (dualUnit.towed.size < 4) {
                    if (gumpyUnit.tower != null) {
                        gumpyUnit.tower.towed.remove((Object)gumpyUnit);
                    }
                    gumpyUnit.tower = dualUnit;
                    dualUnit.towed.add((Object)gumpyUnit);
                }
            }
        }
    }

    public void loadContent() {
        DualUnit.classId = EntityMapping.register((String)"tasky", DualUnit::new);
        GumpyUnit.gumpyClassId = EntityMapping.register((String)"gumpy", GumpyUnit::new);
        UnitType unitType = UnitTypes.gamma;
        UnitType unitType2 = UnitTypes.fortress;
        tasky = new UnitType("tasky"){

            public void load() {
                TextureRegion textureRegion;
                super.load();
                this.previewRegion = this.region;
                this.fullIcon = textureRegion = Core.atlas.find(this.name + "-icon", this.region);
                this.uiIcon = textureRegion;
                UnitType unitType = UnitTypes.fortress;
                TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("clear");
                this.outlineRegion = atlasRegion;
                this.shadowRegion = atlasRegion;
                this.softShadowRegion = atlasRegion;
                this.baseRegion = this.pick(unitType.baseRegion, this.baseRegion);
                this.legRegion = this.pick(unitType.legRegion, this.legRegion);
            }

            TextureRegion pick(TextureRegion textureRegion, TextureRegion textureRegion2) {
                return textureRegion != null ? textureRegion : textureRegion2;
            }

            public void createIcons(MultiPacker multiPacker) {
            }

            public void draw(Unit unit) {
                float f;
                float f2;
                float f3;
                float f4;
                DualUnit dualUnit;
                if (unit instanceof DualUnit) {
                    dualUnit = (DualUnit)unit;
                    if (dualUnit.gumpyProgress > 0.001f && gumpy != null) {
                        Tmp.v2.set(0.0f, 12.0f).rotate(unit.rotation - 90.0f).add(unit.x, unit.y);
                        f4 = Tmp.v2.x;
                        f3 = Tmp.v2.y;
                        f2 = unit.rotation - 90.0f;
                        f = dualUnit.gumpyProgress;
                        Color color = unit.team.color;
                        Draw.draw((float)59.98f, () -> Drawf.construct((float)f4, (float)f3, (TextureRegion)TestMod.gumpy.region, (Color)color, (float)f2, (float)f, (float)1.0f, (float)Time.time));
                    }
                }
                if (unit instanceof DualUnit) {
                    dualUnit = (DualUnit)unit;
                    if (dualUnit.gumpyProgress > 0.5f && (f4 = dualUnit.aimAngle()) >= 0.0f) {
                        Tmp.v2.set(0.0f, 12.0f).rotate(unit.rotation - 90.0f).add(unit.x, unit.y);
                        f3 = 150.0f;
                        f2 = 0.35f + 0.65f * dualUnit.gumpyProgress;
                        Draw.z((float)100.0f);
                        Draw.color((Color)unit.team.color, (float)f2);
                        Lines.stroke((float)1.5f);
                        Lines.dashLine((float)Tmp.v2.x, (float)Tmp.v2.y, (float)(Tmp.v2.x + Angles.trnsx((float)f4, (float)f3)), (float)(Tmp.v2.y + Angles.trnsy((float)f4, (float)f3)), (int)14);
                        f = 4.0f + Mathf.absin((float)Time.time, (float)4.0f, (float)2.0f);
                        Lines.circle((float)(Tmp.v2.x + Angles.trnsx((float)f4, (float)f3)), (float)(Tmp.v2.y + Angles.trnsy((float)f4, (float)f3)), (float)f);
                        Draw.reset();
                    }
                }
                super.draw(unit);
            }

            public void drawItems(Unit unit) {
                if (!(unit instanceof DualUnit)) {
                    return;
                }
                DualUnit dualUnit = (DualUnit)unit;
                this.applyColor(unit);
                float[][] fArrayArray = new float[][]{{0.0f, -6.5f}, {-7.0f, -6.5f}, {7.0f, -6.5f}};
                for (int i = 0; i < 3; ++i) {
                    ItemStack itemStack = dualUnit.slot(i);
                    if (itemStack.amount <= 0 || itemStack.item == null || unit.itemTime <= 0.01f) continue;
                    Tmp.v1.set(fArrayArray[i][0], fArrayArray[i][1]).rotate(unit.rotation - 90.0f).add(unit.x, unit.y);
                    float f = Tmp.v1.x;
                    float f2 = Tmp.v1.y;
                    float f3 = (3.0f + Mathf.absin((float)Time.time, (float)5.0f, (float)1.0f)) * unit.itemTime;
                    Draw.mixcol((Color)Pal.accent, (float)Mathf.absin((float)Time.time, (float)5.0f, (float)0.5f));
                    Draw.rect((TextureRegion)itemStack.item.fullIcon, (float)f, (float)f2, (float)f3, (float)f3, (float)unit.rotation);
                    Draw.mixcol();
                    Lines.stroke((float)1.0f, (Color)Pal.accent);
                    Lines.circle((float)f, (float)f2, (float)((3.0f + Mathf.absin((float)Time.time, (float)5.0f, (float)1.0f)) * unit.itemTime));
                    if (!unit.isLocal() || Vars.renderer.pixelator.enabled()) continue;
                    Fonts.outline.draw((CharSequence)("" + itemStack.amount), f, f2 - 3.0f, Pal.accent, 0.25f / Scl.scl((float)1.0f), false, 1);
                }
                Draw.reset();
            }
        };
        TestMod.tasky.constructor = DualUnit::new;
        TestMod.tasky.aiController = unitType.aiController;
        TestMod.tasky.controller = unitType.controller;
        TestMod.tasky.flying = false;
        TestMod.tasky.lowAltitude = false;
        TestMod.tasky.canBoost = false;
        TestMod.tasky.speed = unitType2.speed * 1.5f;
        TestMod.tasky.accel = unitType2.accel;
        TestMod.tasky.drag = unitType2.drag;
        TestMod.tasky.rotateSpeed = unitType2.rotateSpeed;
        TestMod.tasky.hitSize = unitType2.hitSize * 2.0f;
        TestMod.tasky.faceTarget = unitType2.faceTarget;
        TestMod.tasky.mechStride = unitType2.mechStride;
        TestMod.tasky.mechFrontSway = unitType2.mechFrontSway;
        TestMod.tasky.mechSideSway = unitType2.mechSideSway;
        TestMod.tasky.mechStepParticles = unitType2.mechStepParticles;
        TestMod.tasky.mechLandShake = unitType2.mechLandShake;
        TestMod.tasky.mechLegColor = unitType2.mechLegColor;
        TestMod.tasky.canDrown = true;
        TestMod.tasky.isEnemy = unitType.isEnemy;
        TestMod.tasky.coreUnitDock = false;
        TestMod.tasky.health = 2000.0f;
        TestMod.tasky.armor = unitType.armor;
        TestMod.tasky.buildSpeed = unitType.buildSpeed > 0.0f ? unitType.buildSpeed : 1.5f;
        TestMod.tasky.buildBeamOffset = unitType.buildBeamOffset;
        TestMod.tasky.mineSpeed = unitType.mineSpeed;
        TestMod.tasky.mineTier = unitType.mineTier;
        TestMod.tasky.mineWalls = unitType.mineWalls;
        TestMod.tasky.mineFloor = unitType.mineFloor;
        TestMod.tasky.fogRadius = unitType.fogRadius;
        TestMod.tasky.targetFlags = unitType.targetFlags;
        TestMod.tasky.itemCapacity = 200;
        TestMod.tasky.weapons.clear();
        final UnitType unitType3 = UnitTypes.dagger;
        gumpy = new UnitType("gumpy"){

            public void load() {
                super.load();
                this.previewRegion = this.region;
                this.fullIcon = this.region;
                this.uiIcon = this.region;
                TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("clear");
                this.outlineRegion = atlasRegion;
                this.shadowRegion = atlasRegion;
                this.softShadowRegion = atlasRegion;
                this.baseRegion = unitType3.baseRegion != null ? unitType3.baseRegion : this.baseRegion;
                this.legRegion = unitType3.legRegion != null ? unitType3.legRegion : this.legRegion;
            }

            public void createIcons(MultiPacker multiPacker) {
            }

            public void draw(Unit unit) {
                if (unit instanceof GumpyUnit) {
                    GumpyUnit gumpyUnit = (GumpyUnit)unit;
                    if (gumpyUnit.tower != null && gumpyUnit.leader != null) {
                        float f = Mathf.clamp((float)(gumpyUnit.stretch() / 24.0f));
                        Draw.z((float)59.97f);
                        Lines.stroke((float)1.6f, (Color)Pal.accent.cpy().lerp(Color.scarlet, f));
                        Lines.line((float)gumpyUnit.leader.x, (float)gumpyUnit.leader.y, (float)gumpyUnit.x, (float)gumpyUnit.y);
                        Draw.reset();
                    }
                }
                super.draw(unit);
            }
        };
        TestMod.gumpy.constructor = GumpyUnit::new;
        TestMod.gumpy.aiController = () -> new AIController(){

            public void updateMovement() {
            }

            public void updateTargeting() {
            }

            public void updateWeapons() {
            }
        };
        TestMod.gumpy.flying = false;
        TestMod.gumpy.canBoost = false;
        TestMod.gumpy.isEnemy = false;
        TestMod.gumpy.health = 130.0f;
        TestMod.gumpy.hitSize = 10.0f;
        TestMod.gumpy.speed = unitType3.speed;
        TestMod.gumpy.accel = unitType3.accel;
        TestMod.gumpy.drag = unitType3.drag;
        TestMod.gumpy.rotateSpeed = unitType3.rotateSpeed;
        TestMod.gumpy.faceTarget = unitType3.faceTarget;
        TestMod.gumpy.mechStride = unitType3.mechStride;
        TestMod.gumpy.mechFrontSway = unitType3.mechFrontSway;
        TestMod.gumpy.mechSideSway = unitType3.mechSideSway;
        TestMod.gumpy.mechStepParticles = unitType3.mechStepParticles;
        TestMod.gumpy.mechLandShake = unitType3.mechLandShake;
        TestMod.gumpy.mechLegColor = unitType3.mechLegColor;
        TestMod.gumpy.coreUnitDock = false;
        TestMod.gumpy.buildSpeed = 0.5f;
        TestMod.gumpy.mineTier = 1;
        TestMod.gumpy.mineSpeed = 0.5f;
        TestMod.gumpy.mineWalls = false;
        TestMod.gumpy.mineFloor = true;
        TestMod.gumpy.itemCapacity = 30;
        TestMod.gumpy.useUnitCap = false;
        TestMod.gumpy.weapons.clear();
        galho = new Item("galho", Color.valueOf((String)"7a4a2a"));
        donaTree = new DonaTree("dona-tree");
        for (String string : new Item[]{"maple", "cherry", "leafy", "palm", "dead"}) {
            new DonaTree(string + "-tree", true);
        }
        nest = new TaskyNest("tasky-nest"){
            {
                this.requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.with((Object[])new Object[]{Items.copper, 1}));
                this.alwaysUnlocked = true;
                this.size = 3;
                this.health = 1000000;
                this.itemCapacity = 1000;
                this.unitCapModifier = 24;
                this.unitType = tasky;
                this.isFirstTier = true;
            }
        };
        TreeFx.load();
        Item item = new Item("gold", Color.valueOf((String)"f2c94c"));
        Item item2 = new Item("ruby", Color.valueOf((String)"c2304a"));
        Boulder.oreItems = new Item[]{GrayHook.gray, Items.titanium, Items.copper, item, item2};
        for (final String string : new String[]{"sand", "dark", "moss", "snow"}) {
            new Boulder(string + "-boulder", string);
        }
        for (final String string : new String[]{"cherry", "dead", "mud"}) {
            new Floor("grass-" + string, 4){
                {
                    super(string3, n);
                    this.mapColor = Color.valueOf((String)(string.equals("cherry") ? "539f44" : (string.equals("dead") ? "8d8848" : "5e4932")));
                    if (string.equals("mud")) {
                        this.speedMultiplier = 0.8f;
                    }
                }
            };
        }
        CrateUnit.classId = EntityMapping.register((String)"crate", CrateUnit::new);
        crate = new UnitType("crate"){

            public void load() {
                super.load();
                this.previewRegion = this.region;
                this.fullIcon = this.region;
                this.uiIcon = this.region;
                TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("clear");
                this.outlineRegion = atlasRegion;
                this.shadowRegion = atlasRegion;
                this.softShadowRegion = atlasRegion;
            }

            public void createIcons(MultiPacker multiPacker) {
            }

            public void draw(Unit unit) {
                if (unit instanceof CrateUnit) {
                    CrateUnit crateUnit = (CrateUnit)unit;
                    if (crateUnit.owner != null && crateUnit.leader != null) {
                        float f = Mathf.clamp((float)(Rope.stretch((Unit)crateUnit, crateUnit.leader, crateUnit.ropeLength()) / 12.0f));
                        Draw.z((float)59.97f);
                        Lines.stroke((float)1.4f, (Color)Pal.accent.cpy().lerp(Color.scarlet, f));
                        Lines.line((float)crateUnit.leader.x, (float)crateUnit.leader.y, (float)crateUnit.x, (float)crateUnit.y);
                        Draw.reset();
                    }
                }
                super.draw(unit);
            }

            public void drawItems(Unit unit) {
                if (!(unit instanceof CrateUnit)) {
                    return;
                }
                CrateUnit crateUnit = (CrateUnit)unit;
                float[] fArray = new float[]{-3.0f, 3.0f};
                for (int i = 0; i < 2; ++i) {
                    ItemStack itemStack = crateUnit.stacks[i];
                    if (itemStack.amount <= 0 || itemStack.item == null) continue;
                    Tmp.v1.set(fArray[i], 0.0f).rotate(unit.rotation).add(unit.x, unit.y);
                    Draw.z((float)60.1f);
                    Draw.rect((TextureRegion)itemStack.item.fullIcon, (float)Tmp.v1.x, (float)Tmp.v1.y, (float)4.0f, (float)4.0f, (float)unit.rotation);
                    DualUnit dualUnit = crateUnit.group();
                    if (dualUnit == null || !dualUnit.isLocal() || Vars.renderer.pixelator.enabled()) continue;
                    Fonts.outline.draw((CharSequence)("" + itemStack.amount), Tmp.v1.x, Tmp.v1.y - 3.0f, Pal.accent, 0.2f / Scl.scl((float)1.0f), false, 1);
                }
                Draw.reset();
            }
        };
        TestMod.crate.constructor = CrateUnit::new;
        TestMod.crate.aiController = () -> new AIController(){

            public void updateMovement() {
            }

            public void updateTargeting() {
            }

            public void updateWeapons() {
            }
        };
        TestMod.crate.flying = false;
        TestMod.crate.lowAltitude = false;
        TestMod.crate.isEnemy = false;
        TestMod.crate.health = 200.0f;
        TestMod.crate.hitSize = 18.0f;
        TestMod.crate.speed = 1.0f;
        TestMod.crate.accel = 0.15f;
        TestMod.crate.drag = 0.08f;
        TestMod.crate.rotateSpeed = 10.0f;
        TestMod.crate.omniMovement = true;
        TestMod.crate.physics = false;
        TestMod.crate.itemCapacity = 50;
        TestMod.crate.playerControllable = false;
        TestMod.crate.logicControllable = false;
        TestMod.crate.allowedInPayloads = false;
        TestMod.crate.useUnitCap = false;
        TestMod.crate.drawCell = false;
        TestMod.crate.createWreck = false;
        TestMod.crate.canAttack = false;
        TestMod.crate.autoFindTarget = false;
        gumpyShot = new GumpyShot(false);
        gumpyShotVisual = new GumpyShot(true);
        BranchUnit.classId = EntityMapping.register((String)"galho-solto", BranchUnit::new);
        looseBranch = new UnitType("galho-solto"){

            public void load() {
                super.load();
                TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("clear");
                this.outlineRegion = atlasRegion;
                this.shadowRegion = atlasRegion;
                this.softShadowRegion = atlasRegion;
                this.previewRegion = DonaTree.branch != null ? DonaTree.branch : this.region;
                this.uiIcon = this.previewRegion;
                this.fullIcon = this.previewRegion;
            }

            public void createIcons(MultiPacker multiPacker) {
            }

            public void draw(Unit unit) {
                float[] fArray;
                BranchUnit branchUnit;
                float f = 38.4f;
                if (unit instanceof BranchUnit) {
                    branchUnit = (BranchUnit)unit;
                    v0 = branchUnit.pose();
                } else {
                    v0 = fArray = null;
                }
                if (fArray == null) {
                    Draw.z((float)59.5f);
                    Draw.rect((TextureRegion)DonaTree.branch, (float)unit.x, (float)unit.y, (float)f, (float)f, (float)unit.rotation);
                } else {
                    Draw.z((float)59.4f);
                    float f2 = 9.0f * (1.0f - Mathf.clamp((float)(fArray[2] / 40.0f)) * 0.5f);
                    Draw.color((float)0.0f, (float)0.0f, (float)0.0f, (float)0.28f);
                    Draw.rect((TextureRegion)Core.atlas.find("circle-shadow"), (float)(unit.x + fArray[0]), (float)(unit.y + fArray[1]), (float)(f2 * 2.2f), (float)(f2 * 1.2f));
                    Draw.color();
                    Draw.z((float)(fArray[2] > 1.5f ? 114.0f : 59.5f));
                    Draw.rect((TextureRegion)DonaTree.branch, (float)(unit.x + fArray[0]), (float)(unit.y + fArray[1] + fArray[2]), (float)f, (float)f, (float)(unit.rotation + fArray[3]));
                }
                Draw.reset();
                if (unit instanceof BranchUnit) {
                    branchUnit = (BranchUnit)unit;
                    if (branchUnit.count > 1 && !Vars.renderer.pixelator.enabled()) {
                        Draw.z((float)60.1f);
                        Fonts.outline.draw((CharSequence)("" + branchUnit.count), unit.x, unit.y - 8.0f, Pal.accent, 0.22f / Scl.scl((float)1.0f), false, 1);
                        Draw.reset();
                    }
                }
            }

            public void drawItems(Unit unit) {
            }
        };
        TestMod.looseBranch.constructor = BranchUnit::new;
        TestMod.looseBranch.aiController = () -> new AIController(){

            public void updateMovement() {
            }

            public void updateTargeting() {
            }

            public void updateWeapons() {
            }
        };
        TestMod.looseBranch.flying = false;
        TestMod.looseBranch.isEnemy = false;
        TestMod.looseBranch.health = 1000000.0f;
        TestMod.looseBranch.hitSize = 12.0f;
        TestMod.looseBranch.speed = 0.0f;
        TestMod.looseBranch.physics = false;
        TestMod.looseBranch.hittable = false;
        TestMod.looseBranch.targetable = false;
        TestMod.looseBranch.playerControllable = false;
        TestMod.looseBranch.logicControllable = false;
        TestMod.looseBranch.allowedInPayloads = false;
        TestMod.looseBranch.useUnitCap = false;
        TestMod.looseBranch.drawCell = false;
        TestMod.looseBranch.createWreck = false;
        TestMod.looseBranch.canAttack = false;
        TestMod.looseBranch.autoFindTarget = false;
    }
}
