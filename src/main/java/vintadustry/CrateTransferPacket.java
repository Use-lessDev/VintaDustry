package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.DualUnit;
import vintadustry.TestMod;

public class CrateTransferPacket
extends Packet {
    public int crateId;
    public byte crateSlot;
    public byte unitSlot;
    public boolean take;

    public void write(Writes writes) {
        writes.i(this.crateId);
        writes.b((int)this.crateSlot);
        writes.b((int)this.unitSlot);
        writes.bool(this.take);
    }

    public void read(Reads reads) {
        this.crateId = reads.i();
        this.crateSlot = reads.b();
        this.unitSlot = reads.b();
        this.take = reads.bool();
    }

    public void handleServer(NetConnection netConnection) {
        DualUnit dualUnit;
        Unit unit;
        block5: {
            block4: {
                Player player = netConnection.player;
                if (player == null || !((unit = player.unit()) instanceof DualUnit)) break block4;
                dualUnit = (DualUnit)unit;
                if (!dualUnit.dead) break block5;
            }
            return;
        }
        unit = (Unit)Groups.unit.getByID(this.crateId);
        if (unit != null) {
            TestMod.applyTransfer(dualUnit, unit, this.crateSlot, this.unitSlot, this.take);
        }
    }
}
