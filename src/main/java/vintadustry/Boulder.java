package vintadustry;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.meta.BuildVisibility;

public class Boulder
extends Block {
    public static final String[] oreNames = new String[]{"gray", "bluegray", "copper", "gold", "ruby"};
    public static Item[] oreItems;
    public final String ground;
    public TextureRegion[] base = new TextureRegion[9];
    public TextureRegion[][] top = new TextureRegion[5][9];
    public TextureRegion[][] side = new TextureRegion[5][9];
    public TextureRegion[][] crack = new TextureRegion[5][9];

    public Boulder(String string, String string2) {
        super(string);
        this.ground = string2;
        this.size = 1;
        this.breakable = true;
        this.solid = true;
        this.update = false;
        this.destructible = true;
        this.configurable = true;
        this.saveConfig = true;
        this.health = 60;
        this.category = Category.effect;
        this.requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.with((Object[])new Object[]{Items.copper, 1}));
        this.alwaysUnlocked = true;
        this.buildType = () -> new BoulderBuild();
        this.config(Item.class, (boulderBuild, item) -> {
            boulderBuild.ore = item;
        });
        this.config(Boolean.class, (boulderBuild, bl) -> {
            boulderBuild.cracked = bl;
        });
        this.configClear(boulderBuild -> {
            boulderBuild.ore = null;
        });
    }

    public static int oreIndex(Item item) {
        for (int i = 0; i < oreItems.length; ++i) {
            if (oreItems[i] != item) continue;
            return i;
        }
        return -1;
    }

    public void load() {
        super.load();
        for (int i = 0; i < 9; ++i) {
            this.base[i] = Core.atlas.find("vintadustry-boulder-base-" + this.ground + "-" + (i + 1));
            for (int j = 0; j < 5; ++j) {
                this.top[j][i] = Core.atlas.find("vintadustry-boulder-top-" + this.ground + "-" + oreNames[j] + "-" + (i + 1));
                this.side[j][i] = Core.atlas.find("vintadustry-boulder-side-" + this.ground + "-" + oreNames[j] + "-" + (i + 1));
                this.crack[j][i] = Core.atlas.find("vintadustry-boulder-crack-" + this.ground + "-" + oreNames[j] + "-" + (i + 1));
            }
        }
        this.region = this.base[0];
        this.fullIcon = this.uiIcon = this.base[0];
    }

    public void drawPlan(BuildPlan buildPlan, Eachable<BuildPlan> eachable, boolean bl) {
        Draw.rect((TextureRegion)this.base[0], (float)buildPlan.drawx(), (float)buildPlan.drawy());
    }

    public TextureRegion[] icons() {
        return new TextureRegion[]{this.base[0]};
    }

    public class BoulderBuild
    extends Building {
        public Item ore;
        public boolean cracked;

        public int variant() {
            return Mathf.randomSeed((long)this.tile.pos(), (int)0, (int)8);
        }

        public TextureRegion baseR(int n) {
            return Boulder.this.base[n];
        }

        public TextureRegion sideR(int n) {
            return Boulder.this.side[Boulder.oreIndex(this.ore)][n];
        }

        public TextureRegion topR(int n) {
            return Boulder.this.top[Boulder.oreIndex(this.ore)][n];
        }

        public TextureRegion crackR(int n) {
            return Boulder.this.crack[Boulder.oreIndex(this.ore)][n];
        }

        public void draw() {
            int n = this.variant();
            Draw.rect((TextureRegion)Boulder.this.base[n], (float)this.x, (float)this.y);
            if (this.ore != null && Boulder.oreIndex(this.ore) >= 0) {
                if (!this.cracked) {
                    Draw.rect((TextureRegion)this.sideR(n), (float)this.x, (float)this.y);
                    Draw.rect((TextureRegion)this.topR(n), (float)this.x, (float)this.y);
                } else {
                    Draw.rect((TextureRegion)this.crackR(n), (float)this.x, (float)this.y);
                }
            }
        }

        public void buildConfiguration(Table table) {
            ItemSelection.buildTable((Block)Boulder.this, (Table)table, (Seq)Seq.with((Object[])oreItems), () -> this.ore, arg_0 -> ((BoulderBuild)this).configure(arg_0));
        }

        public Object config() {
            return this.ore;
        }

        public void write(Writes writes) {
            super.write(writes);
            writes.s(this.ore == null ? -1 : (int)this.ore.id);
            writes.bool(this.cracked);
        }

        public void read(Reads reads, byte by) {
            super.read(reads, by);
            short s = reads.s();
            this.ore = s == -1 ? null : Vars.content.item((int)s);
            this.cracked = reads.bool();
        }
    }
}
