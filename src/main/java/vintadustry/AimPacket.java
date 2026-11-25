/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.util.io.Reads
 *  arc.util.io.Writes
 *  mindustry.gen.Player
 *  mindustry.gen.Unit
 *  mindustry.net.NetConnection
 *  mindustry.net.Packet
 */
package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.DualUnit;

public class AimPacket
extends Packet {
    public boolean on;
    public float angle;

    public void write(Writes writes) {
        writes.bool(this.on);
        writes.f(this.angle);
    }

    public void read(Reads reads) {
        this.on = reads.bool();
        this.angle = reads.f();
    }

    public void handleServer(NetConnection netConnection) {
        block3: {
            block2: {
                Unit unit;
                Player player = netConnection.player;
                if (player == null || !((unit = player.unit()) instanceof DualUnit)) break block2;
                DualUnit dualUnit = (DualUnit)unit;
                if (!dualUnit.dead && dualUnit.isTasky()) break block3;
            }
            return;
        }
        dualUnit.aiming = this.on;
        dualUnit.aimAng = this.angle;
    }
}
