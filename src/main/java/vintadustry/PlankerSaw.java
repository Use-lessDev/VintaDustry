package vintadustry;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.io.Reads;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.BuildVisibility;
import vintadustry.TestMod;

public class PlankerSaw
extends GenericCrafter {
    public static PlankerSaw saw;
    public static Block part;
    public static Item plank;
    static final int[][] cells;
    public TextureRegion art;

    public static void create() {
        plank = new Item("plank", Color.valueOf((String)"c9965b"));
        part = new Block("planker-saw-part"){
            {
                this.solid = true;
                this.breakable = true;
                this.update = false;
                this.destructible = false;
                this.rebuildable = false;
                this.hasShadow = false;
                this.buildVisibility = BuildVisibility.hidden;
            }
        };
        saw = new PlankerSaw("planker-saw");
    }

    public PlankerSaw(String string) {
        super(string);
        this.size = 1;
        this.rotate = true;
        this.solid = true;
        this.update = true;
        this.hasItems = true;
        this.itemCapacity = 12;
        this.health = 260;
        this.craftTime = 70.0f;
        this.outputItem = new ItemStack(plank, 1);
        this.consumeItems(ItemStack.with((Object[])new Object[]{TestMod.galho, 3}));
        this.craftEffect = Fx.pulverizeMedium;
        this.updateEffect = Fx.none;
        this.requirements(Category.crafting, ItemStack.with((Object[])new Object[]{Items.copper, 15, TestMod.galho, 10}));
        this.alwaysUnlocked = true;
        this.buildType = () -> new PlankerSawBuild();
    }

    public void load() {
        super.load();
        this.region = this.art = Core.atlas.find(this.name);
        this.fullIcon = this.art;
        this.uiIcon = this.art;
    }

    static int[] rot(int n, int n2, int n3) {
        switch (n3 & 3) {
            case 0: {
                return new int[]{n, n2};
            }
            case 1: {
                return new int[]{-n2, n};
            }
            case 2: {
                return new int[]{-n, -n2};
            }
        }
        return new int[]{n2, -n};
    }

    static float[] center(float f, float f2, int n) {
        int[] nArray = PlankerSaw.rot(0, 4, n);
        return new float[]{f + (float)nArray[0], f2 + (float)nArray[1]};
    }

    public boolean canPlaceOn(Tile tile, Team team, int n) {
        if (!super.canPlaceOn(tile, team, n)) {
            return false;
        }
        for (int[] nArray : cells) {
            int[] nArray2;
            Tile tile2;
            if (nArray[0] == 0 && nArray[1] == 0 || (tile2 = Vars.world.tile(tile.x + (nArray2 = PlankerSaw.rot(nArray[0], nArray[1], n))[0], tile.y + nArray2[1])) != null && tile2.block() == Blocks.air && tile2.build == null && !tile2.floor().isDeep() && tile2.floor().placeableOn) continue;
            return false;
        }
        return true;
    }

    public void drawPlace(int n, int n2, int n3, boolean bl) {
        float[] fArray = PlankerSaw.center((float)n * 8.0f, (float)n2 * 8.0f, n3);
        boolean bl2 = (n3 & 1) == 1;
        float f = bl2 ? 16.0f : 24.0f;
        float f2 = bl2 ? 24.0f : 16.0f;
        Draw.reset();
        Draw.z((float)30.5f);
        Draw.color((float)1.0f, (float)1.0f, (float)1.0f, (float)0.55f);
        Draw.rect((TextureRegion)this.art, (float)fArray[0], (float)fArray[1], (float)24.0f, (float)16.0f, (float)(n3 * 90));
        Draw.color((Color)(bl ? Pal.accent : Pal.remove));
        Lines.stroke((float)2.0f);
        Lines.rect((float)(fArray[0] - f / 2.0f), (float)(fArray[1] - f2 / 2.0f), (float)f, (float)f2);
        Draw.reset();
    }

    public void drawPlan(BuildPlan buildPlan, Eachable<BuildPlan> eachable, boolean bl) {
        float[] fArray = PlankerSaw.center((float)buildPlan.x * 8.0f, (float)buildPlan.y * 8.0f, buildPlan.rotation);
        Draw.reset();
        Draw.rect((TextureRegion)this.art, (float)fArray[0], (float)fArray[1], (float)24.0f, (float)16.0f, (float)(buildPlan.rotation * 90));
        if (!bl) {
            boolean bl2 = (buildPlan.rotation & 1) == 1;
            Draw.color((Color)Pal.remove, (float)0.5f);
            Lines.stroke((float)2.0f);
            Lines.rect((float)(fArray[0] - (bl2 ? 8.0f : 12.0f)), (float)(fArray[1] - (bl2 ? 12.0f : 8.0f)), (float)(bl2 ? 16.0f : 24.0f), (float)(bl2 ? 24.0f : 16.0f));
            Draw.reset();
        }
    }

    static {
        cells = new int[][]{{0, 0}, {-1, 0}, {1, 0}, {-1, 1}, {0, 1}, {1, 1}};
    }

    public class PlankerSawBuild
    extends GenericCrafter.GenericCrafterBuild {
        float checkT;
        final Seq<Building> tmp;

        public PlankerSawBuild() {
            super((GenericCrafter)PlankerSaw.this);
            this.tmp = new Seq();
        }

        Tile cell(int n) {
            int[] nArray = PlankerSaw.rot(cells[n][0], cells[n][1], this.rotation);
            return Vars.world.tile(this.tile.x + nArray[0], this.tile.y + nArray[1]);
        }

        void link() {
            for (int i = 1; i < cells.length; ++i) {
                Tile tile = this.cell(i);
                if (tile == null || tile.build == this || tile.build != null) continue;
                if (tile.block() != part) {
                    if (tile.block() != Blocks.air) continue;
                    tile.setBlock(part, this.team, 0);
                }
                tile.build = this;
            }
            this.updateProximity();
        }

        void unlink() {
            for (int i = 1; i < cells.length; ++i) {
                Tile tile = this.cell(i);
                if (tile == null || tile.block() != part || tile.build != this && tile.build != null) continue;
                tile.build = null;
                tile.setBlock(Blocks.air);
            }
        }

        public void created() {
            super.created();
            if (!Vars.world.isGenerating()) {
                this.link();
            }
        }

        public void readAll(Reads reads, byte by) {
            super.readAll(reads, by);
            this.link();
        }

        public void onRemoved() {
            this.unlink();
            super.onRemoved();
        }

        public void updateProximity() {
            this.proximity.clear();
            this.tmp.clear();
            for (int i = 0; i < cells.length; ++i) {
                Tile tile = this.cell(i);
                if (tile == null) continue;
                for (Point2 point2 : Geometry.d4) {
                    Building building = Vars.world.build(tile.x + point2.x, tile.y + point2.y);
                    if (building == null || building == this || !building.tile.interactable(this.team)) continue;
                    if (!building.proximity.contains((Object)this, true)) {
                        building.proximity.add((Object)this);
                    }
                    if (this.tmp.contains((Object)building, true)) continue;
                    this.tmp.add((Object)building);
                }
            }
            this.proximity.addAll(this.tmp);
            this.onProximityAdded();
            this.onProximityUpdate();
            for (Tile tile : this.tmp) {
                tile.onProximityUpdate();
            }
            this.updatePowerGraph();
        }

        public byte relativeTo(int n, int n2) {
            for (int i = 0; i < cells.length; ++i) {
                byte by;
                Tile tile = this.cell(i);
                if (tile == null || (by = tile.relativeTo(n, n2)) == -1) continue;
                return by;
            }
            return super.relativeTo(n, n2);
        }

        public byte relativeTo(Tile tile) {
            return this.relativeTo(tile.x, tile.y);
        }

        public byte relativeTo(Building building) {
            for (int i = 0; i < cells.length; ++i) {
                Tile tile = this.cell(i);
                if (tile == null) continue;
                for (int j = 0; j < 4; ++j) {
                    Building building2 = Vars.world.build(tile.x + Geometry.d4x[j], tile.y + Geometry.d4y[j]);
                    if (building2 != building) continue;
                    return (byte)j;
                }
            }
            return super.relativeTo(building);
        }

        public void updateTile() {
            super.updateTile();
            this.checkT += Time.delta;
            if (this.checkT >= 90.0f) {
                this.checkT = 0.0f;
                this.link();
            }
            if (!Vars.headless && this.efficiency > 0.0f && this.warmup > 0.3f && Mathf.chance((double)(0.18 * (double)Time.delta))) {
                float[] fArray = PlankerSaw.center(this.x, this.y, this.rotation);
                Fx.pulverizeSmall.at(fArray[0] + Mathf.range((float)9.0f), fArray[1] + Mathf.range((float)6.0f));
            }
        }

        public void draw() {
            float[] fArray = PlankerSaw.center(this.x, this.y, this.rotation);
            float f = this.warmup > 0.01f && this.efficiency > 0.0f ? Mathf.sin((float)(Time.time * 1.9f)) * 0.28f * this.warmup : 0.0f;
            Draw.z((float)30.0f);
            Draw.rect((TextureRegion)PlankerSaw.this.art, (float)(fArray[0] + f), (float)fArray[1], (float)24.0f, (float)16.0f, (float)(this.rotation * 90));
            Draw.reset();
        }
    }
}
