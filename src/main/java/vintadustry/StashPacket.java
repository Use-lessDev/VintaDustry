package vintadustry;

import arc.math.geom.Position;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import vintadustry.BranchUnit;
import vintadustry.CrateUnit;
import vintadustry.TestMod;

public class StashPacket
extends Packet {
    public int branchId;
    public int crateId;
    public float x;
    public float y;

    public void write(Writes writes) {
        writes.i(this.branchId);
        writes.i(this.crateId);
        writes.f(this.x);
        writes.f(this.y);
    }

    public void read(Reads reads) {
        this.branchId = reads.i();
        this.crateId = reads.i();
        this.x = reads.f();
        this.y = reads.f();
    }

    public void handleServer(NetConnection netConnection) {
        Player player = netConnection.player;
        if (player == null || player.unit() == null || player.unit().dead) {
            return;
        }
        Unit unit = (Unit)Groups.unit.getByID(this.branchId);
        Unit unit2 = (Unit)Groups.unit.getByID(this.crateId);
        if (unit instanceof BranchUnit && unit2 instanceof CrateUnit) {
            if (player.unit().within(this.x, this.y, 240.0f)) {
                ((BranchUnit)unit).cancelFlight();
                unit.set(this.x, this.y);
            }
            StashPacket.apply(player.unit(), (BranchUnit)unit, (CrateUnit)unit2);
        }
    }

    public static void apply(Unit unit, BranchUnit branchUnit, CrateUnit crateUnit) {
        if (unit == null || branchUnit.dead || crateUnit.dead || !branchUnit.isAdded() || !crateUnit.isAdded()) {
            return;
        }
        if (branchUnit.team != unit.team || crateUnit.team != unit.team) {
            return;
        }
        if (!unit.within((Position)crateUnit, 260.0f) || !branchUnit.within((Position)crateUnit, crateUnit.hitSize / 2.0f + 26.0f)) {
            return;
        }
        Item item = TestMod.galho;
        int n = crateUnit.type.itemCapacity;
        block0: for (int i = 0; i < 2 && branchUnit.count > 0; ++i) {
            for (ItemStack itemStack : crateUnit.stacks) {
                int n2;
                boolean bl;
                if (branchUnit.count <= 0) continue block0;
                boolean bl2 = i == 0 ? itemStack.amount > 0 && itemStack.item == item : (bl = itemStack.amount <= 0 || itemStack.item == null);
                if (!bl || (n2 = Math.min(branchUnit.count, n - itemStack.amount)) <= 0) continue;
                itemStack.item = item;
                itemStack.amount += n2;
                branchUnit.count -= n2;
            }
        }
        if (branchUnit.count <= 0) {
            Fx.mineHuge.at(branchUnit.x, branchUnit.y, 0.0f, Pal.accent);
            Call.unitDespawn((Unit)branchUnit);
        }
    }
}
