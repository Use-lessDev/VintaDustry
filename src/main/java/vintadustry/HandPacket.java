/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.util.io.Reads
 *  arc.util.io.Writes
 *  mindustry.Vars
 *  mindustry.net.NetConnection
 *  mindustry.net.Packet
 */
package vintadustry;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;
import vintadustry.Hand;

public class HandPacket
extends Packet {
    public int pid;
    public byte mode;
    public float x;
    public float y;
    public float tension;
    public boolean rel;

    public void write(Writes writes) {
        writes.i(this.pid);
        writes.b((int)this.mode);
        writes.f(this.x);
        writes.f(this.y);
        writes.f(this.tension);
        writes.bool(this.rel);
    }

    public void read(Reads reads) {
        this.pid = reads.i();
        this.mode = reads.b();
        this.x = reads.f();
        this.y = reads.f();
        this.tension = reads.f();
        this.rel = reads.bool();
    }

    public void handleServer(NetConnection netConnection) {
        if (netConnection.player == null) {
            return;
        }
        this.pid = netConnection.player.id();
        Hand.remoteUpdate(this);
        Vars.net.sendExcept(netConnection, (Object)this, this.rel);
    }

    public void handleClient() {
        Hand.remoteUpdate(this);
    }
}
