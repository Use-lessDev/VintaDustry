package vintadustry;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Itemsc;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Fonts;
import vintadustry.CrateUnit;
import vintadustry.DonaTree;
import vintadustry.DualUnit;
import vintadustry.Hand;
import vintadustry.TestMod;
import vintadustry.TreeFx;

public class BranchUnit
extends UnitEntity {
    public static int classId = -1;
    public int count = 1;
    public int variant = -1;
    public static final TextureRegion[] variantRegions = new TextureRegion[8];
    public static final int maxSeparate = 50;
    public static final int maxLoose = 220;
    public float noPickup;
    public float ox;
    public float oy;
    public float h0;
    public float vy0;
    public float spin;
    public float age;
    int bounced;
    boolean syncedOnce;
    float pickT;
    public static final float G = 0.1f;
    public static final float absorbDist = 16.0f;
    boolean prepared;
    float t1;
    float imp;
    float rest;
    float f1;
    float f2;
    float totalT;
    static final float[] POSE = new float[4];
    static TextureRegion shadow;
    final Boolf<Unit> pickFilter = unit -> unit != this && unit.team == this.team && !unit.dead && !(unit instanceof BranchUnit) && !(unit instanceof CrateUnit) && (unit.isPlayer() || unit instanceof DualUnit) && unit.acceptsItem(TestMod.galho);

    public static void drop(Team team, float f, float f2, int n) {
        BranchUnit.drop(team, f, f2, n, 0.0f);
    }

    public static void drop(Team team, float f, float f2, int n, float f3) {
        BranchUnit.drop(team, f, f2, n, f3, f, f2, 0.0f, 0.0f);
    }

    public static void drop(Team team, float f, float f2, int n, float f3, float f4, float f5, float f6, float f7) {
        BranchUnit.dropV(team, f, f2, -1, n, f3, f4, f5, f6, f7);
    }

    static int randomVariant() {
        int n = 0;
        TextureRegion[] textureRegionArray = variantRegions;
        int n2 = textureRegionArray.length;
        for (int i = 0; i < n2; ++i) {
            TextureRegion textureRegion = textureRegionArray[i];
            if (textureRegion == null) continue;
            ++n;
        }
        if (n == 0) {
            return 0;
        }
        int n3 = Mathf.random((int)(n - 1));
        for (n2 = 0; n2 < variantRegions.length; ++n2) {
            if (variantRegions[n2] == null || n3-- != 0) continue;
            return n2;
        }
        return 0;
    }

    static void capLoose(int n) {
        Unit unit2;
        if (Vars.net.client()) {
            return;
        }
        int n2 = 0;
        for (Unit unit2 : Groups.unit) {
            if (!(unit2 instanceof BranchUnit)) continue;
            ++n2;
        }
        int n3 = n2 + n - 220;
        if (n3 <= 0) {
            return;
        }
        unit2 = new Seq();
        for (Unit unit3 : Groups.unit) {
            BranchUnit branchUnit3;
            if (!(unit3 instanceof BranchUnit) || !(branchUnit3.age > (branchUnit3 = (BranchUnit)unit3).total() + 10.0f)) continue;
            unit2.add((Object)branchUnit3);
        }
        unit2.sort((branchUnit, branchUnit2) -> Float.compare(branchUnit2.age, branchUnit.age));
        for (int i = 0; i < Math.min(n3, unit2.size); ++i) {
            Call.unitDespawn((Unit)((Unit)unit2.get(i)));
        }
    }

    public static void scatter(Team team, float f, float f2, int n, int n2, float f3, float f4, float f5, float f6, float f7) {
        int n3 = Math.min(n2, 50);
        BranchUnit.capLoose(n3 + 1);
        for (int i = 0; i < n3; ++i) {
            float f8 = (float)i * 137.5f + Mathf.range((float)8.0f);
            float f9 = 14.0f + 11.0f * Mathf.sqrt((float)i) + Mathf.random((float)3.0f);
            BranchUnit.dropV(team, f + Mathf.cosDeg((float)f8) * f9, f2 + Mathf.sinDeg((float)f8) * f9, n, 1, f3, f4, f5, f6, f7 * Mathf.random((float)0.8f, (float)1.2f));
        }
        if (n2 > n3) {
            BranchUnit.dropV(team, f, f2, n, n2 - n3, f3, f4, f5, f6, f7);
        }
    }

    public static void dropV(Team team, float f, float f2, int n, int n2, float f3, float f4, float f5, float f6, float f7) {
        Unit unit = TestMod.looseBranch.create(team);
        unit.set(f, f2);
        unit.rotation = Mathf.random((float)360.0f);
        if (unit instanceof BranchUnit) {
            BranchUnit branchUnit = (BranchUnit)unit;
            branchUnit.count = n2;
            branchUnit.variant = n < 0 ? BranchUnit.randomVariant() : n;
            branchUnit.ox = f4 - f;
            branchUnit.oy = f5 - f2;
            branchUnit.h0 = f6;
            branchUnit.vy0 = f7;
            branchUnit.spin = (Mathf.chance((double)0.5) ? 1.0f : -1.0f) * Mathf.random((float)240.0f, (float)540.0f);
            branchUnit.noPickup = Math.max(f3, branchUnit.total() + 6.0f);
        }
        unit.add();
    }

    void ensure() {
        if (this.prepared) {
            return;
        }
        this.prepared = true;
        if (this.h0 <= 0.0f && this.vy0 <= 0.0f) {
            this.totalT = 0.0f;
            return;
        }
        this.t1 = (this.vy0 + Mathf.sqrt((float)(this.vy0 * this.vy0 + 0.2f * this.h0))) / 0.1f;
        this.imp = 0.1f * this.t1 - this.vy0;
        float f = (float)this.id * 0.61803f % 1.0f;
        this.rest = 0.3f + 0.14f * f;
        this.f1 = 2.0f * this.imp * this.rest / 0.1f;
        this.f2 = 2.0f * this.imp * this.rest * this.rest / 0.1f;
        this.totalT = this.t1 + this.f1 + this.f2;
    }

    public float total() {
        this.ensure();
        return this.totalT;
    }

    public float z() {
        this.ensure();
        if (this.totalT <= 0.0f) {
            return 0.0f;
        }
        float f = this.age;
        if (f < this.t1) {
            return this.h0 + this.vy0 * f - 0.05f * f * f;
        }
        float f2 = this.imp * this.rest;
        float f3 = f - this.t1;
        if (f3 < this.f1) {
            return f2 * f3 - 0.05f * f3 * f3;
        }
        f3 -= this.f1;
        f2 *= this.rest;
        if (f3 < this.f2) {
            return f2 * f3 - 0.05f * f3 * f3;
        }
        return 0.0f;
    }

    public float[] pose() {
        float f = this.total();
        if (f <= 0.0f || this.age >= f) {
            return null;
        }
        float f2 = 1.0f - this.age / f;
        float f3 = f2 * f2;
        BranchUnit.POSE[0] = this.ox * f3;
        BranchUnit.POSE[1] = this.oy * f3;
        BranchUnit.POSE[2] = this.z();
        BranchUnit.POSE[3] = this.spin * f3;
        return POSE;
    }

    int packed() {
        return this.count & 0xFFFFF | Math.max(this.variant, 0) + 1 << 20;
    }

    void unpack(int n) {
        this.count = n & 0xFFFFF;
        int n2 = n >>> 20 & 0x7FF;
        this.variant = n2 == 0 ? 2 : n2 - 1;
    }

    public void write(Writes writes) {
        super.write(writes);
        writes.i(this.packed());
    }

    public void read(Reads reads) {
        super.read(reads);
        this.unpack(reads.i());
    }

    public void writeSync(Writes writes) {
        super.writeSync(writes);
        writes.i(this.packed());
        boolean bl = this.age >= this.total() + 10.0f;
        writes.bool(bl);
        if (!bl) {
            writes.f(this.ox);
            writes.f(this.oy);
            writes.f(this.h0);
            writes.f(this.vy0);
            writes.f(this.spin);
            writes.f(this.age);
        }
    }

    public void readSync(Reads reads) {
        super.readSync(reads);
        this.unpack(reads.i());
        boolean bl = reads.bool();
        if (bl) {
            float f = this.total() + 10.0f;
            if (this.age < f) {
                this.age = f;
            }
            this.syncedOnce = true;
            return;
        }
        this.ox = reads.f();
        this.oy = reads.f();
        float f = reads.f();
        float f2 = reads.f();
        if (f != this.h0 || f2 != this.vy0) {
            this.h0 = f;
            this.vy0 = f2;
            this.prepared = false;
        }
        this.spin = reads.f();
        float f3 = reads.f();
        if (!this.syncedOnce || Math.abs(this.age - f3) > 8.0f) {
            this.age = f3;
        }
        this.syncedOnce = true;
    }

    public int classId() {
        return classId;
    }

    public boolean isLocal() {
        return super.isLocal() || Hand.gLoose == this;
    }

    public void cancelFlight() {
        this.age = Math.max(this.age, this.total() + 10.0f);
    }

    public void draw() {
        TextureRegion textureRegion;
        TextureRegion textureRegion2 = textureRegion = this.variant >= 0 && this.variant < variantRegions.length ? variantRegions[this.variant] : null;
        if (textureRegion == null) {
            textureRegion = DonaTree.branch;
        }
        if (textureRegion == null) {
            return;
        }
        float f = 38.4f;
        float[] fArray = this.pose();
        if (fArray == null) {
            Draw.z((float)59.5f);
            Draw.rect((TextureRegion)textureRegion, (float)this.x, (float)this.y, (float)f, (float)f, (float)this.rotation);
        } else {
            if (shadow == null) {
                shadow = Core.atlas.find("circle-shadow");
            }
            Draw.z((float)59.4f);
            float f2 = fArray[2];
            float f3 = 9.0f * (1.0f - Mathf.clamp((float)(f2 / 40.0f)) * 0.5f);
            Draw.color((float)0.0f, (float)0.0f, (float)0.0f, (float)(0.28f * (1.0f - Mathf.clamp((float)(f2 / 60.0f)) * 0.6f)));
            Draw.rect((TextureRegion)shadow, (float)(this.x + fArray[0]), (float)(this.y + fArray[1]), (float)(f3 * 2.2f), (float)(f3 * 1.2f));
            Draw.color();
            Draw.z((float)(f2 > 1.5f ? 114.0f : 59.5f));
            float f4 = 1.0f + Mathf.clamp((float)(f2 / 60.0f)) * 0.18f;
            Draw.rect((TextureRegion)textureRegion, (float)(this.x + fArray[0]), (float)(this.y + fArray[1] + f2), (float)(f * f4), (float)(f * f4), (float)(this.rotation + fArray[3]));
        }
        Draw.reset();
        if (this.count > 1 && !Vars.renderer.pixelator.enabled()) {
            Draw.z((float)60.1f);
            Fonts.outline.draw((CharSequence)("" + this.count), this.x, this.y - 8.0f, Pal.accent, 0.22f / Scl.scl((float)1.0f), false, 1);
            Draw.reset();
        }
    }

    public void update() {
        super.update();
        this.vel.setZero();
        float f = this.total();
        if (this.age < f + 10.0f) {
            this.age += Time.delta;
        }
        if (!Vars.headless && f > 0.0f && this.bounced < 1 && this.age >= this.t1) {
            this.bounced = 1;
            float f2 = 1.0f - this.t1 / f;
            TreeFx.land.at(this.x + this.ox * f2 * f2, this.y + this.oy * f2 * f2, 1.0f, Pal.accent);
        }
        if (Vars.net.client()) {
            return;
        }
        if (this.noPickup > 0.0f) {
            this.noPickup -= Time.delta;
            return;
        }
        this.pickT += Time.delta;
        if (this.pickT < 4.0f) {
            return;
        }
        this.pickT = 0.0f;
        Item item = TestMod.galho;
        Unit unit = Units.closest((Team)this.team, (float)this.x, (float)this.y, (float)48.0f, this.pickFilter);
        if (unit == null || !unit.within((Position)this, 16.0f + unit.hitSize / 2.0f)) {
            return;
        }
        int n = Math.min(this.count, unit.maxAccepted(item));
        if (n <= 0) {
            return;
        }
        unit.addItem(item, n);
        Call.transferItemEffect((Item)item, (float)this.x, (float)this.y, (Itemsc)unit);
        this.count -= n;
        if (this.count > 0) {
            return;
        }
        Fx.mineHuge.at(this.x, this.y, 0.0f, Pal.accent);
        Call.unitDespawn((Unit)this);
    }
}
