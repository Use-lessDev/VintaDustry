/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.ApplicationListener
 *  arc.Core
 *  arc.files.Fi
 *  arc.util.Time
 *  mindustry.Vars
 *  mindustry.content.Blocks
 *  mindustry.ctype.ContentType
 *  mindustry.world.Block
 *  mindustry.world.Tile
 *  mindustry.world.blocks.environment.Floor
 */
package vintadustry;

import arc.ApplicationListener;
import arc.Core;
import arc.files.Fi;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.ctype.ContentType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import vintadustry.Boulder;

public class BoulderOres {
    static final String prefix = "vintadustry-ore-boulder-";
    static Block[] ores;
    static float scanT;
    static boolean wasDown;

    public static void init() {
        if (Vars.headless) {
            return;
        }
        Core.app.addListener((ApplicationListener)new BoulderOres$1());
    }

    static Block[] find() {
        if (ores == null || ores[0] == null) {
            ores = new Block[Boulder.oreNames.length];
            for (int i = 0; i < ores.length; ++i) {
                BoulderOres.ores[i] = (Block)Vars.content.getByName(ContentType.block, prefix + Boulder.oreNames[i]);
            }
        }
        return ores;
    }

    static void tick() {
        if (Vars.state == null || !Vars.state.isEditor() || Vars.world == null || Vars.world.tiles == null) {
            wasDown = false;
            return;
        }
        boolean bl = Core.input.isTouched();
        boolean bl2 = wasDown && !bl;
        wasDown = bl;
        scanT += Time.delta;
        if (!bl2 && scanT < 40.0f) {
            return;
        }
        scanT = 0.0f;
        BoulderOres.scan();
    }

    static void scan() {
        Block[] blockArray = BoulderOres.find();
        if (blockArray[0] == null) {
            return;
        }
        int n = Vars.world.width();
        int n2 = Vars.world.height();
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n2; ++j) {
                Tile tile = Vars.world.rawTile(i, j);
                Floor floor = tile.overlay();
                if (floor == Blocks.air) continue;
                int n3 = -1;
                for (int k = 0; k < blockArray.length; ++k) {
                    if (floor != blockArray[k]) continue;
                    n3 = k;
                    break;
                }
                if (n3 < 0) continue;
                if (tile.build instanceof Boulder.BoulderBuild && n3 < Boulder.oreItems.length) {
                    ((Boulder.BoulderBuild)tile.build).ore = Boulder.oreItems[n3];
                }
                tile.setOverlay(Blocks.air);
                tile.recache();
            }
        }
    }

    class BoulderOres$1
    implements ApplicationListener {
        BoulderOres$1() {
        }

        public void init() {
        }

        public void resize(int n, int n2) {
        }

        public void update() {
            BoulderOres.tick();
        }

        public void pause() {
        }

        public void resume() {
        }

        public void dispose() {
        }

        public void exit() {
        }

        public void fileDropped(Fi fi) {
        }
    }
}
