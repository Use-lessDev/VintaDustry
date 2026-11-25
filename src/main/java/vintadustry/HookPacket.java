package vintadustry;

import arc.math.geom.Position;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.DualUnit;
import vintadustry.TestMod;

public class HookPacket
extends Packet {
    public int targetId;

    public void write(Writes writes) {
        writes.i(this.targetId);
    }

    public void read(Reads reads) {
        this.targetId = reads.i();
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
        unit = (Unit)Groups.unit.getByID(this.targetId);
        if (unit == null || unit.team != dualUnit.team || unit == dualUnit || !dualUnit.within((Position)unit, 300.0f)) {
            return;
        }
        TestMod.applyHook(dualUnit, unit);
    }
}
