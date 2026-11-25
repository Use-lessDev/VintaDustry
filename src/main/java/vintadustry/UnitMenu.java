package vintadustry;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import vintadustry.CrateTransferPacket;
import vintadustry.CrateUI;
import vintadustry.CrateUnit;
import vintadustry.DualUnit;
import vintadustry.GumpyUnit;
import vintadustry.Hand;
import vintadustry.HookPacket;
import vintadustry.PartDraw;
import vintadustry.PartHealth;
import vintadustry.SlotDropPacket;
import vintadustry.TestMod;

public class UnitMenu {
    public static Unit sel;
    static Table menu;
    static int lastSig;
    static final Color yellow;
    static boolean wasDown;
    static boolean pressing;
    static boolean moved;
    static float pressT;
    static float sx0;
    static float sy0;
    static float wx0;
    static float wy0;
    static float menuX;
    static float menuY;
    static boolean placed;

    public static void init() {
        if (Vars.headless) {
            return;
        }
        Events.run((Object)EventType.Trigger.update, UnitMenu::update);
        Events.run((Object)EventType.Trigger.draw, UnitMenu::draw);
        Events.on(EventType.WorldLoadEvent.class, worldLoadEvent -> UnitMenu.clear());
    }

    static void clear() {
        sel = null;
        placed = false;
        pressing = false;
        lastSig = Integer.MIN_VALUE;
        if (menu != null) {
            UnitMenu.menu.visible = false;
        }
    }

    static boolean eligible(DualUnit dualUnit, Unit unit) {
        if (unit == null || unit.dead || !unit.isAdded() || unit.team != dualUnit.team) {
            return false;
        }
        if (unit == dualUnit) {
            return true;
        }
        if (unit instanceof CrateUnit) {
            return true;
        }
        if (dualUnit.isTasky() && unit instanceof GumpyUnit) {
            return true;
        }
        return unit instanceof DualUnit && ((DualUnit)unit).isTasky();
    }

    static float screenX() {
        return Vars.mobile && Hand.ptr >= 0 ? (float)Core.input.mouseX(Hand.ptr) : (float)Core.input.mouseX();
    }

    static float screenY() {
        return Vars.mobile && Hand.ptr >= 0 ? (float)Core.input.mouseY(Hand.ptr) : (float)Core.input.mouseY();
    }

    static void update() {
        if (Vars.state == null || !Vars.state.isGame() || Vars.player == null) {
            UnitMenu.clear();
            wasDown = false;
            return;
        }
        TestMod.holdDone = true;
        TestMod.holdCrate = null;
        TestMod.holdTime = 0.0f;
        Unit unit = Vars.player.unit();
        if (!(unit instanceof DualUnit) || unit.dead) {
            UnitMenu.clear();
            wasDown = false;
            return;
        }
        DualUnit dualUnit = (DualUnit)unit;
        Hand.readInput();
        boolean bl = Hand.down;
        if (bl && !wasDown && !Hand.uiBlocked && !Core.scene.hasMouse()) {
            pressing = true;
            moved = false;
            pressT = 0.0f;
            sx0 = UnitMenu.screenX();
            sy0 = UnitMenu.screenY();
            wx0 = Hand.mx;
            wy0 = Hand.my;
        }
        if (bl && pressing) {
            pressT += Core.graphics.getDeltaTime();
            if (Mathf.dst((float)UnitMenu.screenX(), (float)UnitMenu.screenY(), (float)sx0, (float)sy0) > Scl.scl((float)14.0f)) {
                moved = true;
            }
        }
        if (!bl && wasDown && pressing) {
            pressing = false;
            if (!moved && pressT < 0.4f) {
                UnitMenu.tap(dualUnit, wx0, wy0);
            }
        }
        wasDown = bl;
        if (!(sel == null || UnitMenu.eligible(dualUnit, sel) && sel.within((Position)dualUnit, 260.0f))) {
            UnitMenu.clear();
        }
        if (sel == null) {
            if (menu != null) {
                UnitMenu.menu.visible = false;
            }
            return;
        }
        UnitMenu.refresh(dualUnit);
    }

    static void tap(DualUnit dualUnit, float f, float f2) {
        Unit unit = null;
        float f3 = Float.MAX_VALUE;
        float f4 = Vars.mobile ? 14.0f : 6.0f;
        for (Unit unit2 : Groups.unit) {
            float f5;
            if (!UnitMenu.eligible(dualUnit, unit2) || !((f5 = unit2.dst(f, f2)) <= unit2.hitSize / 2.0f + f4) || !(f5 < f3)) continue;
            f3 = f5;
            unit = unit2;
        }
        if (unit != null) {
            sel = unit;
            lastSig = Integer.MIN_VALUE;
            placed = false;
        } else {
            UnitMenu.clear();
        }
    }

    static int signature(DualUnit dualUnit) {
        int n = UnitMenu.sel.id;
        if (sel instanceof CrateUnit) {
            CrateUnit crateUnit = (CrateUnit)sel;
            for (ItemStack itemStack : crateUnit.stacks) {
                n = n * 31 + (itemStack.item == null ? -1 : (int)itemStack.item.id) * 1009 + itemStack.amount;
            }
            n = n * 31 + (crateUnit.owner == dualUnit ? 1 : 0);
        } else if (sel instanceof GumpyUnit) {
            n = n * 31 + (((GumpyUnit)UnitMenu.sel).tower == dualUnit ? 1 : 0);
        } else if (sel instanceof DualUnit) {
            DualUnit dualUnit2 = (DualUnit)sel;
            n = n * 31 + (sel == dualUnit ? 7 : 3);
            for (int i = 0; i < dualUnit2.ownSlots(); ++i) {
                ItemStack itemStack = dualUnit2.slot(i);
                n = n * 31 + (itemStack.item == null ? -1 : (int)itemStack.item.id) * 1009 + itemStack.amount;
            }
        }
        return n;
    }

    static void refresh(DualUnit dualUnit) {
        boolean bl;
        int n;
        if (menu == null) {
            menu = new Table();
            UnitMenu.menu.touchable = Touchable.enabled;
            menu.setBackground(Styles.black6);
            Core.scene.add((Element)menu);
        }
        if ((n = UnitMenu.signature(dualUnit)) != lastSig) {
            lastSig = n;
            UnitMenu.rebuild(dualUnit);
        }
        UnitMenu.menu.visible = true;
        Vec2 vec2 = Core.input.mouseScreen(UnitMenu.sel.x, UnitMenu.sel.y - UnitMenu.sel.hitSize / 2.0f - 6.0f);
        menu.pack();
        float f = menu.getWidth();
        float f2 = menu.getHeight();
        float f3 = Mathf.clamp((float)vec2.x, (float)(f / 2.0f + 4.0f), (float)((float)Core.graphics.getWidth() - f / 2.0f - 4.0f));
        float f4 = vec2.y;
        if (f4 - f2 < 4.0f) {
            Vec2 vec22 = Core.input.mouseScreen(UnitMenu.sel.x, UnitMenu.sel.y + UnitMenu.sel.hitSize / 2.0f + 6.0f);
            f4 = vec22.y + f2;
        }
        boolean bl2 = bl = Core.input.isTouched() && Core.scene.hasMouse();
        if (!placed) {
            menuX = f3;
            menuY = f4;
            placed = true;
        } else if (!bl) {
            menuX = Mathf.lerp((float)menuX, (float)f3, (float)0.3f);
            menuY = Mathf.lerp((float)menuY, (float)f4, (float)0.3f);
        }
        menu.setPosition(menuX, menuY, 2);
    }

    static String tip(String string) {
        return Core.bundle.get(string);
    }

    static void rebuild(DualUnit dualUnit) {
        menu.clear();
        menu.defaults().pad(3.0f);
        Unit unit = sel;
        float f = 60.0f;
        if (unit == dualUnit) {
            for (int i = 0; i < dualUnit.ownSlots(); ++i) {
                UnitMenu.slotButton(dualUnit.slot(i), i, true);
            }
            if (!dualUnit.hasItem()) {
                menu.add((CharSequence)Core.bundle.get("menu.empty")).pad(8.0f);
            }
            menu.pack();
            return;
        }
        if (unit instanceof CrateUnit) {
            var3_4 = (CrateUnit)unit;
            for (int i = 0; i < ((CrateUnit)((Object)var3_4)).stacks.length; ++i) {
                UnitMenu.slotButton(((CrateUnit)((Object)var3_4)).stacks[i], i, false);
            }
        } else if (unit instanceof DualUnit && ((DualUnit)unit).isTasky()) {
            var3_4 = (DualUnit)unit;
            for (int i = 0; i < ((DualUnit)((Object)var3_4)).ownSlots(); ++i) {
                UnitMenu.slotButton(((DualUnit)((Object)var3_4)).slot(i), i, false);
            }
        }
        if (unit instanceof CrateUnit) {
            boolean bl = ((CrateUnit)unit).owner == dualUnit;
            menu.button((Drawable)(bl ? Icon.cancel : Icon.link), Styles.cleari, 34.0f, () -> UnitMenu.hook(dualUnit)).size(f).tooltip(UnitMenu.tip(bl ? "menu.unhook" : "menu.hook"));
        } else if (unit instanceof GumpyUnit) {
            boolean bl = ((GumpyUnit)unit).tower == dualUnit;
            menu.button((Drawable)(bl ? Icon.cancel : Icon.link), Styles.cleari, 34.0f, () -> UnitMenu.hook(dualUnit)).size(f).tooltip(UnitMenu.tip(bl ? "menu.unhook" : "menu.hook"));
        } else if (dualUnit instanceof GumpyUnit) {
            menu.button((Drawable)Icon.download, Styles.cleari, 34.0f, () -> UnitMenu.hook(dualUnit)).size(f).tooltip(UnitMenu.tip("menu.swallow"));
        }
        if (unit instanceof CrateUnit || unit instanceof DualUnit && ((DualUnit)unit).isTasky()) {
            menu.button((Drawable)Icon.list, Styles.cleari, 34.0f, () -> CrateUI.open(unit)).size(f).tooltip(UnitMenu.tip("menu.open"));
        }
        menu.pack();
    }

    static void slotButton(ItemStack itemStack, int n, boolean bl) {
        if (itemStack.amount <= 0 || itemStack.item == null) {
            return;
        }
        Item item = itemStack.item;
        int n2 = itemStack.amount;
        boolean bl2 = bl;
        menu.button(button -> {
            button.image(item.uiIcon).size(32.0f);
            button.row();
            Label label = new Label((CharSequence)("" + n2));
            label.setFontScale(0.85f);
            label.setColor(bl2 ? Pal.remove : Pal.accent);
            button.add((Element)label);
        }, Styles.defaultb, () -> {
            if (bl2) {
                UnitMenu.drop(n);
            } else {
                UnitMenu.take(n);
            }
        }).size(64.0f, 72.0f).tooltip(UnitMenu.tip(bl ? "menu.drop" : "menu.take") + " " + item.localizedName);
    }

    static void hook(DualUnit dualUnit) {
        if (sel == null) {
            return;
        }
        if (Vars.net.client()) {
            HookPacket hookPacket = new HookPacket();
            hookPacket.targetId = UnitMenu.sel.id;
            Vars.net.send((Object)hookPacket, true);
        } else {
            TestMod.applyHook(dualUnit, sel);
        }
        lastSig = Integer.MIN_VALUE;
    }

    static void drop(int n) {
        if (!(Vars.player.unit() instanceof DualUnit)) {
            return;
        }
        DualUnit dualUnit = (DualUnit)Vars.player.unit();
        if (Vars.net.client()) {
            SlotDropPacket slotDropPacket = new SlotDropPacket();
            slotDropPacket.slot = (byte)n;
            Vars.net.send((Object)slotDropPacket, true);
        } else {
            dualUnit.dropSlot(n);
        }
        lastSig = Integer.MIN_VALUE;
    }

    static void take(int n) {
        ItemStack itemStack;
        if (sel == null || !(Vars.player.unit() instanceof DualUnit)) {
            return;
        }
        DualUnit dualUnit = (DualUnit)Vars.player.unit();
        ItemStack itemStack2 = itemStack = sel instanceof CrateUnit ? ((CrateUnit)UnitMenu.sel).stacks[n] : ((DualUnit)sel).slot(n);
        if (itemStack.item != null && itemStack.amount > 0 && !dualUnit.acceptsItem(itemStack.item)) {
            Vars.ui.showInfoToast(Core.bundle.get("menu.cannot"), 2.0f);
            return;
        }
        if (Vars.net.client()) {
            CrateTransferPacket crateTransferPacket = new CrateTransferPacket();
            crateTransferPacket.crateId = UnitMenu.sel.id;
            crateTransferPacket.crateSlot = (byte)n;
            crateTransferPacket.unitSlot = (byte)-1;
            crateTransferPacket.take = true;
            Vars.net.send((Object)crateTransferPacket, true);
        } else {
            TestMod.applyTransfer(dualUnit, sel, n, -1, true);
        }
        lastSig = Integer.MIN_VALUE;
    }

    static void draw() {
        TextureRegion[] textureRegionArray;
        if (Vars.headless || sel == null || !sel.isAdded() || UnitMenu.sel.dead) {
            return;
        }
        Unit unit = sel;
        float f = 0.0f;
        float f2 = 0.0f;
        if (unit instanceof DualUnit) {
            DualUnit dualUnit = (DualUnit)unit;
            PartHealth.Set set = PartHealth.setFor(unit);
            if (!set.ready()) {
                return;
            }
            float[] fArray = PartDraw.sway(unit);
            f = fArray[0];
            f2 = fArray[1];
            textureRegionArray = new TextureRegion[5];
            for (int i = 0; i < 5; ++i) {
                textureRegionArray[i] = dualUnit.parts.broken(i) ? null : set.part[i];
            }
        } else {
            textureRegionArray = new TextureRegion[]{unit.type.region};
        }
        float f3 = unit.rotation - 90.0f;
        float f4 = unit instanceof DualUnit ? 1.2f : 1.0f;
        Draw.z((float)(unit.type.groundLayer + Mathf.clamp((float)(unit.type.hitSize / 4000.0f), (float)0.0f, (float)0.01f) - 0.0012f));
        Draw.mixcol((Color)yellow, (float)1.0f);
        for (int i = 0; i < 8; ++i) {
            float f5 = Mathf.cosDeg((float)((float)i * 45.0f)) * f4;
            float f6 = Mathf.sinDeg((float)((float)i * 45.0f)) * f4;
            for (TextureRegion textureRegion : textureRegionArray) {
                if (textureRegion == null) continue;
                Draw.rect((TextureRegion)textureRegion, (float)(unit.x + f + f5), (float)(unit.y + f2 + f6), (float)f3);
            }
        }
        Draw.mixcol();
        Draw.reset();
    }

    static {
        lastSig = Integer.MIN_VALUE;
        yellow = Color.valueOf((String)"ffd34a");
    }
}
