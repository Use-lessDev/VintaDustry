/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.graphics.Color
 *  arc.util.io.Reads
 *  arc.util.io.Writes
 *  mindustry.Vars
 *  mindustry.gen.Building
 *  mindustry.net.Packet
 */
package vintadustry;

import arc.graphics.Color;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.net.Packet;
import vintadustry.DonaTree;

public class TreeBreakPacket
extends Packet {
    public byte phase;
    public int pos;
    public float dir;
    public float x;
    public float y;
    public int color;
    public String names = "";

    public void write(Writes writes) {
        writes.b((int)this.phase);
        writes.i(this.pos);
        writes.f(this.dir);
        writes.f(this.x);
        writes.f(this.y);
        writes.i(this.color);
        writes.str(this.names);
    }

    public void read(Reads reads) {
        this.phase = reads.b();
        this.pos = reads.i();
        this.dir = reads.f();
        this.x = reads.f();
        this.y = reads.f();
        this.color = reads.i();
        this.names = reads.str();
        if (this.names == null) {
            this.names = "";
        }
    }

    public void handleClient() {
        if (this.phase == 0) {
            Building building = Vars.world.build(this.pos);
            if (building instanceof DonaTree.DonaTreeBuild) {
                ((DonaTree.DonaTreeBuild)building).startBreakVisual(this.dir);
            }
        } else {
            DonaTree.playTopple(this.x, this.y, this.dir, new Color(this.color), this.names);
        }
    }
}
