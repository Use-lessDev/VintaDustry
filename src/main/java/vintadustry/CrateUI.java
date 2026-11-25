/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.graphics.Color
 *  arc.input.KeyCode
 *  arc.math.geom.Position
 *  arc.math.geom.Vec2
 *  arc.scene.Element
 *  arc.scene.event.EventListener
 *  arc.scene.event.InputEvent
 *  arc.scene.event.InputListener
 *  arc.scene.event.Touchable
 *  arc.scene.ui.Image
 *  arc.scene.ui.layout.Table
 *  mindustry.Vars
 *  mindustry.gen.Tex
 *  mindustry.gen.Unit
 *  mindustry.type.Item
 *  mindustry.type.ItemStack
 *  mindustry.ui.dialogs.BaseDialog
 */
package vintadustry;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.EventListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.dialogs.BaseDialog;
import vintadustry.CrateTransferPacket;
import vintadustry.CrateUnit;
import vintadustry.DualUnit;
import vintadustry.TestMod;

public class CrateUI {
    static BaseDialog dialog;
    static Unit crate;
    static Table crateTable;
    static Table ownTable;
    static int lastSig;
    static boolean dragging;
    static Image ghost;

    static int count(Unit unit) {
        if (unit instanceof CrateUnit) {
            return ((CrateUnit)unit).stacks.length;
        }
        return ((DualUnit)unit).ownSlots();
    }

    static ItemStack st(Unit unit, int n) {
        if (unit instanceof CrateUnit) {
            return ((CrateUnit)unit).stacks[n];
        }
        return ((DualUnit)unit).slot(n);
    }

    static int signature() {
        ItemStack itemStack;
        int n;
        int n2 = 17;
        if (crate == null || !(Vars.player.unit() instanceof DualUnit)) {
            return n2;
        }
        DualUnit dualUnit = (DualUnit)Vars.player.unit();
        for (n = 0; n < CrateUI.count(crate); ++n) {
            itemStack = CrateUI.st(crate, n);
            n2 = n2 * 31 + (itemStack.item == null ? -1 : (int)itemStack.item.id) * 1009 + itemStack.amount;
        }
        for (n = 0; n < dualUnit.ownSlots(); ++n) {
            itemStack = dualUnit.slot(n);
            n2 = n2 * 31 + (itemStack.item == null ? -1 : (int)itemStack.item.id) * 1009 + itemStack.amount;
        }
        return n2;
    }

    public static void open(Unit unit) {
        crate = unit;
        if (dialog == null) {
            dialog = new BaseDialog("Crate");
            dialog.addCloseButton();
            dialog.hidden(() -> CrateUI.stopDrag());
            CrateUI.dialog.cont.update(() -> {
                if (crate == null || CrateUI.crate.dead || !crate.isAdded() || Vars.player.unit() == null || !Vars.player.unit().within((Position)crate, 180.0f)) {
                    CrateUI.stopDrag();
                    dialog.hide();
                    return;
                }
                int n = CrateUI.signature();
                if (!dragging && n != lastSig) {
                    CrateUI.rebuild();
                }
            });
        }
        CrateUI.stopDrag();
        CrateUI.rebuild();
        dialog.show();
    }

    static void stopDrag() {
        dragging = false;
        if (ghost != null) {
            ghost.remove();
            ghost = null;
        }
    }

    static boolean over(Table table, float f, float f2) {
        if (table == null || table.getScene() == null) {
            return false;
        }
        Vec2 vec2 = table.stageToLocalCoordinates(new Vec2(f, f2));
        return vec2.x >= 0.0f && vec2.y >= 0.0f && vec2.x <= table.getWidth() && vec2.y <= table.getHeight();
    }

    static void row(Table table, ItemStack itemStack, final boolean bl, final int n) {
        Table table2 = new Table();
        if (itemStack.amount > 0 && itemStack.item != null) {
            final Item item = itemStack.item;
            Table table3 = new Table();
            table3.image(item.uiIcon).size(32.0f).padRight(6.0f);
            table3.add((CharSequence)(item.localizedName + " x" + itemStack.amount)).width(170.0f).left();
            table3.addListener((EventListener)new InputListener(){

                public boolean touchDown(InputEvent inputEvent, float f, float f2, int n2, KeyCode keyCode) {
                    return true;
                }

                public void touchDragged(InputEvent inputEvent, float f, float f2, int n2) {
                    if (!dragging) {
                        dragging = true;
                        ghost = new Image(item.uiIcon);
                        ghost.setSize(44.0f, 44.0f);
                        CrateUI.ghost.touchable = Touchable.disabled;
                        Core.scene.add((Element)ghost);
                    }
                    if (ghost != null) {
                        ghost.setPosition(inputEvent.stageX - 22.0f, inputEvent.stageY - 22.0f);
                    }
                }

                public void touchUp(InputEvent inputEvent, float f, float f2, int n2, KeyCode keyCode) {
                    if (!dragging) {
                        return;
                    }
                    float f3 = inputEvent.stageX;
                    float f4 = inputEvent.stageY;
                    CrateUI.stopDrag();
                    if (bl && CrateUI.over(ownTable, f3, f4)) {
                        CrateUI.send(n, -1, true);
                    } else if (!bl && CrateUI.over(crateTable, f3, f4)) {
                        CrateUI.send(0, n, false);
                    }
                }
            });
            table2.add((Element)table3).left();
            if (bl) {
                table2.button("Take", () -> CrateUI.send(n, -1, true)).size(80.0f, 40.0f);
            } else {
                table2.button("Put", () -> CrateUI.send(0, n, false)).size(80.0f, 40.0f);
            }
        } else {
            table2.add((CharSequence)"(empty)").width(210.0f).left();
        }
        table.add((Element)table2).pad(4.0f).row();
    }

    static void rebuild() {
        int n;
        Table table = CrateUI.dialog.cont;
        table.clear();
        lastSig = CrateUI.signature();
        if (crate == null || !(Vars.player.unit() instanceof DualUnit)) {
            return;
        }
        DualUnit dualUnit = (DualUnit)Vars.player.unit();
        table.add((CharSequence)(crate instanceof CrateUnit ? "Crate" : "Tasky")).row();
        table.add((CharSequence)"Drag items between the panels, or use the buttons.").color(Color.lightGray).padBottom(6.0f).row();
        crateTable = new Table();
        crateTable.background(Tex.button);
        crateTable.defaults().pad(2.0f);
        for (n = 0; n < CrateUI.count(crate); ++n) {
            CrateUI.row(crateTable, CrateUI.st(crate, n), true, n);
        }
        crateTable.button("Take all", () -> {
            for (int i = 0; i < CrateUI.count(crate); ++i) {
                ItemStack itemStack = CrateUI.st(crate, i);
                if (itemStack.amount <= 0 || itemStack.item == null) continue;
                CrateUI.send(i, -1, true);
            }
        }).size(170.0f, 40.0f).padTop(6.0f).row();
        table.add((Element)crateTable).minWidth(300.0f).row();
        table.add((CharSequence)"Your items").padTop(14.0f).row();
        ownTable = new Table();
        ownTable.background(Tex.button);
        ownTable.defaults().pad(2.0f);
        for (n = 0; n < dualUnit.ownSlots(); ++n) {
            CrateUI.row(ownTable, dualUnit.slot(n), false, n);
        }
        table.add((Element)ownTable).minWidth(300.0f).row();
    }

    static void send(int n, int n2, boolean bl) {
        Object object;
        if (!(Vars.player.unit() instanceof DualUnit) || crate == null) {
            return;
        }
        DualUnit dualUnit = (DualUnit)Vars.player.unit();
        if (!bl) {
            ItemStack itemStack;
            int n3;
            object = dualUnit.slot(n2);
            if (object.amount <= 0 || object.item == null) {
                return;
            }
            n = -1;
            for (n3 = 0; n3 < CrateUI.count(crate) && n < 0; ++n3) {
                itemStack = CrateUI.st(crate, n3);
                if (itemStack.amount <= 0 || itemStack.item != object.item) continue;
                n = n3;
            }
            for (n3 = 0; n3 < CrateUI.count(crate) && n < 0; ++n3) {
                itemStack = CrateUI.st(crate, n3);
                if (itemStack.amount > 0 && itemStack.item != null) continue;
                n = n3;
            }
            if (n < 0 && crate instanceof DualUnit) {
                n = 0;
            }
            if (n < 0) {
                return;
            }
        }
        if (Vars.net.client()) {
            object = new CrateTransferPacket();
            object.crateId = CrateUI.crate.id;
            object.crateSlot = (byte)n;
            object.unitSlot = (byte)n2;
            object.take = bl;
            Vars.net.send(object, true);
        } else {
            TestMod.applyTransfer(dualUnit, crate, n, n2, bl);
        }
        lastSig = Integer.MIN_VALUE;
    }

    static {
        lastSig = Integer.MIN_VALUE;
    }
}
