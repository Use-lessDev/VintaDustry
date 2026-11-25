package vintadustry;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import vintadustry.BranchUnit;
import vintadustry.TreeBreakPacket;
import vintadustry.TreeFx;
import vintadustry.TreeSyncPacket;

public class DonaTree
extends Block {
    public static final float S = 0.5f;
    public static final float growTime = 5400.0f;
    public static final float branchLen = 32.5f;
    static final int[] order = new int[]{0, 4, 2, 6, 1, 5, 3, 7};
    public static TextureRegion branch;
    public static TextureRegion handOpen;
    public static TextureRegion handClosed;
    public static final Seq<DonaTreeBuild> trees;
    public final boolean modern;
    public String art;
    public int variant;
    public TextureRegion[] trunks = new TextureRegion[2];
    public TextureRegion[] tops = new TextureRegion[2];
    public TextureRegion[] branchVars = new TextureRegion[3];

    public DonaTree(String string) {
        this(string, true);
    }

    public DonaTree(String string, boolean bl) {
        super(string);
        this.modern = bl;
        boolean bl2 = string.equals("dona-tree");
        this.art = bl2 ? "vintadustry-leafy-tree" : this.name;
        this.variant = DonaTree.variantOf(bl2 ? "leafy" : string.replace("-tree", ""));
        this.size = 1;
        this.solid = true;
        this.update = true;
        this.sync = true;
        this.health = 600;
        this.requirements(Category.effect, string.equals("dona-tree") ? BuildVisibility.hidden : BuildVisibility.sandboxOnly, ItemStack.with((Object[])new Object[]{Items.copper, 1}));
        this.buildType = () -> new DonaTreeBuild();
    }

    public static void playTopple(float f, float f2, float f3, Color color, String string) {
        if (Vars.headless) {
            return;
        }
        TreeFx.topple.at(f, f2, f3, color, (Object)string);
        Time.run((float)30.0f, () -> TreeFx.impact.at(f + Mathf.cosDeg((float)f3) * 42.0f, f2 + Mathf.sinDeg((float)f3) * 42.0f, f3, color));
    }

    static int variantOf(String string) {
        switch (string) {
            case "maple": {
                return 0;
            }
            case "cherry": {
                return 1;
            }
            case "leafy": {
                return 2;
            }
            case "palm": {
                return 3;
            }
        }
        return 4;
    }

    public void load() {
        super.load();
        branch = Core.atlas.find("vintadustry-dona-tree-branch");
        handOpen = Core.atlas.find("vintadustry-hand-open");
        handClosed = Core.atlas.find("vintadustry-hand-closed");
        if (this.modern) {
            int n;
            for (n = 0; n < 2; ++n) {
                this.trunks[n] = Core.atlas.find(this.art + "-trunk-" + (n + 1));
                this.tops[n] = Core.atlas.find(this.art + "-top-" + (n + 1));
            }
            for (n = 0; n < 3; ++n) {
                this.branchVars[n] = Core.atlas.find(this.art + "-branch-" + (n + 1));
            }
            BranchUnit.variantRegions[this.variant] = this.branchVars[0];
            TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find(this.art);
            this.fullIcon = atlasRegion;
            this.uiIcon = atlasRegion;
            return;
        }
        TextureAtlas.AtlasRegion atlasRegion = Core.atlas.find("vintadustry-dona-tree-full");
        if (atlasRegion.found()) {
            this.fullIcon = atlasRegion;
            this.uiIcon = atlasRegion;
        }
    }

    public static void angleTip(int n, float f, Vec2 vec2) {
        float f2 = 90.0f + 45.0f * (float)n;
        vec2.trns(f2, 32.5f * f);
    }

    static {
        trees = new Seq();
    }

    public class DonaTreeBuild
    extends Building {
        public float crownAlpha = 1.0f;
        public float shake;
        public boolean breaking;
        public float sx;
        public float sy;
        public float svx;
        public float svy;
        public float fx;
        public float fy;
        public int lastKick = -1;
        public float breakDir;
        public boolean registered;
        Color lc;
        public int pullSlot = -1;
        public float pullAmt;
        public float pullAng;
        public float pullStamp;
        public boolean seeded;
        public float breakT;
        public float shakeT;
        public float breakTime;
        public float breakStamp;
        public int breakDrops;
        public Team breakTeam;
        public float[] growth = new float[]{0.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f};

        public Vec2 tip(int n, Vec2 vec2) {
            DonaTree.angleTip(n, 1.0f, vec2);
            return vec2.add(this.x, this.y);
        }

        void spring() {
            float f = Math.min(Time.delta, 3.0f);
            this.svx += (-0.12f * this.sx - 0.075f * this.svx + this.fx) * f;
            this.svy += (-0.12f * this.sy - 0.075f * this.svy + this.fy) * f;
            this.sx = Mathf.clamp((float)(this.sx + this.svx * f), (float)-10.0f, (float)10.0f);
            this.sy = Mathf.clamp((float)(this.sy + this.svy * f), (float)-10.0f, (float)10.0f);
            this.fx = 0.0f;
            this.fy = 0.0f;
        }

        public void kick(float f, float f2) {
            this.svx += Mathf.cosDeg((float)f) * f2;
            this.svy += Mathf.sinDeg((float)f) * f2;
        }

        void syncGrowth() {
            if (!Vars.net.server()) {
                return;
            }
            TreeSyncPacket treeSyncPacket = new TreeSyncPacket();
            treeSyncPacket.pos = this.tile.pos();
            for (int i = 0; i < 8; ++i) {
                treeSyncPacket.g[i] = (byte)(this.growth[i] < 0.0f ? -1 : Math.round(Mathf.clamp((float)this.growth[i]) * 100.0f));
            }
            Vars.net.send((Object)treeSyncPacket, true);
        }

        public void applySync(byte[] byArray) {
            for (int i = 0; i < 8; ++i) {
                this.growth[i] = byArray[i] < 0 ? -1.0f : (float)byArray[i] / 100.0f;
            }
            this.seeded = true;
        }

        public void startBreakVisual(float f) {
            this.breaking = true;
            this.breakT = 0.0f;
            this.breakDir = f;
        }

        public byte version() {
            return 1;
        }

        void seed() {
            int n = 0;
            for (float f : this.growth) {
                if (!(f >= 1.0f)) continue;
                ++n;
            }
            int n2 = 3 + Mathf.random((int)2);
            for (int n3 : order) {
                if (n >= n2) break;
                if (this.growth[n3] >= 1.0f) continue;
                this.growth[n3] = 1.0f;
                ++n;
            }
        }

        public void updateTile() {
            if (!this.seeded) {
                this.seeded = true;
                if (!Vars.net.client()) {
                    this.seed();
                    this.syncGrowth();
                }
            }
            if (!this.registered) {
                this.registered = true;
                if (!trees.contains((Object)this)) {
                    trees.add((Object)this);
                }
            }
            this.shake = Mathf.lerpDelta((float)this.shake, (float)0.0f, (float)0.06f);
            this.spring();
            if (DonaTree.this.modern && !Vars.headless) {
                Unit unit = Vars.player == null ? null : Vars.player.unit();
                float f = unit != null && !unit.dead && unit.within((Position)this, 70.0f) ? 0.35f : 1.0f;
                this.crownAlpha = Mathf.lerpDelta((float)this.crownAlpha, (float)f, (float)0.1f);
            }
            if (this.breaking) {
                float f = Mathf.clamp((float)(this.breakT / 88.0f));
                this.shake = Math.max(this.shake, 0.5f + 1.9f * f * f);
                if (Vars.net.client()) {
                    this.breakT += Time.delta;
                } else {
                    this.breakTick();
                }
                return;
            }
            boolean bl = true;
            int n = 0;
            for (int i = 0; i < 8; ++i) {
                if (this.growth[i] < 0.0f) continue;
                ++n;
                if (!(this.growth[i] < 1.0f)) continue;
                this.growth[i] = Math.min(1.0f, this.growth[i] + Time.delta / 5400.0f);
                bl = false;
            }
            if (bl && n < 8 && !Vars.net.client()) {
                for (int n2 : order) {
                    if (!(this.growth[n2] < 0.0f)) continue;
                    this.growth[n2] = 0.0f;
                    this.syncGrowth();
                    break;
                }
            }
        }

        public void onRemoved() {
            trees.remove((Object)this);
            super.onRemoved();
        }

        public void rip(int n, Unit unit) {
            if (n < 0 || n > 7 || this.growth[n] < 1.0f) {
                this.syncGrowth();
                return;
            }
            Vec2 vec2 = this.tip(n, new Vec2());
            if (!unit.within((Position)vec2, 360.0f)) {
                this.syncGrowth();
                return;
            }
            this.growth[n] = 0.0f;
            this.syncGrowth();
            Call.effect((Effect)TreeFx.snap, (float)vec2.x, (float)vec2.y, (float)(90.0f + 45.0f * (float)n), (Color)this.leafColor());
            Call.effect((Effect)TreeFx.shake, (float)this.x, (float)this.y, (float)0.8f, (Color)Color.white);
            Call.effect((Effect)TreeFx.kick, (float)this.x, (float)this.y, (float)(270.0f + 45.0f * (float)n), (Color)Color.white, (Object)Float.valueOf(2.6f));
            float f = 90.0f + 45.0f * (float)n;
            BranchUnit.dropV(unit.team, vec2.x + Angles.trnsx((float)f, (float)9.0f) + Mathf.range((float)3.0f), vec2.y + Angles.trnsy((float)f, (float)9.0f) + Mathf.range((float)3.0f), DonaTree.this.variant, 1, 0.0f, vec2.x, vec2.y, 20.0f, 0.4f);
        }

        public Color leafColor() {
            if (this.lc == null) {
                String string = this.block.name;
                this.lc = Color.valueOf((String)(string.contains("maple") ? "d2601e" : (string.contains("cherry") ? "f2a5c4" : (string.contains("dead") ? "8a7a5a" : (string.contains("palm") ? "6dbb4a" : "4f9a3e")))));
            }
            return this.lc;
        }

        public boolean trunkFree() {
            if (this.breaking) {
                return false;
            }
            for (float f : this.growth) {
                if (!(f >= 1.0f)) continue;
                return false;
            }
            return true;
        }

        public void ripTrunk(Unit unit) {
            if (this.breaking || !this.trunkFree() || !unit.within((Position)this, 360.0f)) {
                return;
            }
            this.breaking = true;
            this.breakDir = Angles.angle((float)unit.x, (float)unit.y, (float)this.x, (float)this.y);
            this.breakT = 0.0f;
            this.shakeT = 99.0f;
            this.breakDrops = 0;
            this.breakTeam = unit.team;
            if (Vars.net.server()) {
                TreeBreakPacket treeBreakPacket = new TreeBreakPacket();
                treeBreakPacket.phase = 0;
                treeBreakPacket.pos = this.tile.pos();
                treeBreakPacket.dir = this.breakDir;
                Vars.net.send((Object)treeBreakPacket, true);
            }
        }

        void breakTick() {
            float f;
            float f2;
            int n;
            float f3 = 3.5f;
            this.breakT += Time.delta;
            this.shakeT += Time.delta;
            while (this.breakDrops < 25 && this.breakT >= (float)this.breakDrops * f3) {
                ++this.breakDrops;
                float f4 = (float)n * 137.5f + Mathf.range((float)8.0f);
                float f5 = 14.0f + 11.0f * Mathf.sqrt((float)n) + Mathf.random((float)3.0f);
                f2 = this.x + Mathf.range((float)8.0f);
                f = this.y + Mathf.range((float)8.0f);
                BranchUnit.dropV(this.breakTeam, this.x + Angles.trnsx((float)f4, (float)f5), this.y + Angles.trnsy((float)f4, (float)f5), DonaTree.this.variant, 1, 20.0f, f2, f, 30.0f + Mathf.random((float)8.0f), 0.6f);
                Call.effect((Effect)TreeFx.kick, (float)this.x, (float)this.y, (float)(f4 + 180.0f), (Color)Color.white, (Object)Float.valueOf(1.7f));
                Call.effect((Effect)TreeFx.chips, (float)(this.x + Mathf.range((float)10.0f)), (float)(this.y + Mathf.range((float)10.0f)), (float)f4, (Color)this.leafColor());
            }
            if (this.breakDrops >= 25 && this.breakT >= 110.0f) {
                n = this.tile.pos();
                String string = DonaTree.this.art + "-trunk-" + (Mathf.mod((int)n, (int)2) + 1);
                String string2 = DonaTree.this.art + "-top-" + (Mathf.mod((int)(n / 2), (int)2) + 1);
                f2 = this.x;
                f = this.y;
                float f6 = this.breakDir;
                Color color = this.leafColor();
                DonaTree.playTopple(f2, f, f6, color, string + ";" + string2);
                if (Vars.net.server()) {
                    TreeBreakPacket treeBreakPacket = new TreeBreakPacket();
                    treeBreakPacket.phase = 1;
                    treeBreakPacket.pos = this.tile.pos();
                    treeBreakPacket.dir = f6;
                    treeBreakPacket.x = f2;
                    treeBreakPacket.y = f;
                    treeBreakPacket.color = color.rgba();
                    treeBreakPacket.names = string + ";" + string2;
                    Vars.net.send((Object)treeBreakPacket, true);
                }
                this.tile.setNet(Blocks.air);
            }
        }

        public void draw() {
            int n = this.tile.pos();
            float f = Mathf.range((float)(this.shake * 1.6f));
            float f2 = Mathf.range((float)(this.shake * 0.8f));
            float f3 = this.breaking ? this.breakT : 0.0f;
            float f4 = f3 > 88.0f ? 1.0f + 0.09f * Mathf.clamp((float)((f3 - 88.0f) / 22.0f)) : 1.0f;
            float f5 = f3 > 0.0f ? 1.0f : 0.4f;
            float f6 = 0.0f;
            float f7 = Mathf.cosDeg((float)this.breakDir) * 34.0f * f6;
            float f8 = Mathf.sinDeg((float)this.breakDir) * 34.0f * f6;
            float f9 = (Mathf.cosDeg((float)this.breakDir) >= 0.0f ? -1.0f : 1.0f) * 70.0f * f6;
            float f10 = this.sx;
            float f11 = this.sy;
            boolean bl = this.pullSlot >= 0 && Time.time - this.pullStamp < 3.0f;
            Draw.z((float)30.0f);
            TextureRegion textureRegion = DonaTree.this.modern ? DonaTree.this.trunks[Mathf.mod((int)n, (int)2)] : this.block.region;
            Draw.rect((TextureRegion)textureRegion, (float)(this.x + f * f5 + f10 * 0.12f + f7 * 0.25f), (float)(this.y + f2 * f5 + f11 * 0.12f + f8 * 0.25f), (float)(64.0f * f4), (float)(64.0f * f4));
            Draw.z((float)35.0f);
            for (int i = 0; i < 8; ++i) {
                float f12 = this.growth[i];
                if (f12 < 0.0f) continue;
                float f13 = 0.2f + 0.8f * f12;
                float f14 = 90.0f + 45.0f * (float)i;
                Tmp.v1.set(0.25f * f13, 1.0f * f13).rotate(f14 - 90.0f);
                float f15 = Mathf.sin((float)(Time.time / 70.0f + (float)i * 1.3f)) * 2.5f * f12;
                Tmp.v1.rotate(f15);
                TextureRegion textureRegion2 = DonaTree.this.modern ? DonaTree.this.branchVars[Mathf.mod((int)(n / 3 + i * 7), (int)3)] : branch;
                float f16 = -f10 * 0.9f;
                float f17 = 1.0f;
                if (bl && i == this.pullSlot) {
                    f16 += Mathf.clamp((float)(((this.pullAng - f14 + 540.0f) % 360.0f - 180.0f) * 0.4f), (float)-35.0f, (float)35.0f) * this.pullAmt;
                    f17 = 1.0f + 0.1f * this.pullAmt;
                    f13 *= f17;
                }
                Draw.rect((TextureRegion)textureRegion2, (float)(this.x + Tmp.v1.x + f + f10 * 0.55f), (float)(this.y + Tmp.v1.y + f2 + f11 * 0.55f), (float)(64.0f * f13 * f4), (float)(64.0f * f13 * f4), (float)(f14 - 90.0f + f15 + f16));
            }
            if (DonaTree.this.modern) {
                Draw.z((float)115.5f);
                Draw.alpha((float)this.crownAlpha);
                float f18 = Mathf.sin((float)(Time.time / 90.0f + (float)n)) * 1.2f;
                Draw.rect((TextureRegion)DonaTree.this.tops[Mathf.mod((int)(n / 2), (int)2)], (float)(this.x + f + f10 + f7), (float)(this.y + f2 + f11 + f8), (float)(64.0f * f4), (float)(64.0f * f4), (float)(f18 - f10 * 1.4f + f9));
                Draw.reset();
            }
        }

        public void write(Writes writes) {
            super.write(writes);
            for (float f : this.growth) {
                writes.f(f);
            }
            writes.bool(this.seeded);
        }

        public void read(Reads reads, byte by) {
            super.read(reads, by);
            for (int i = 0; i < 8; ++i) {
                this.growth[i] = reads.f();
            }
            this.seeded = by >= 1 && reads.bool();
        }
    }
}
