/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.math.geom.Position
 *  arc.struct.ObjectFloatMap
 *  arc.struct.ObjectMap
 *  arc.struct.Seq
 *  arc.util.Time
 *  mindustry.Vars
 *  mindustry.content.Blocks
 *  mindustry.game.Team
 *  mindustry.gen.Call
 *  mindustry.gen.Groups
 *  mindustry.gen.Player
 *  mindustry.world.Block
 *  mindustry.world.Tile
 *  mindustry.world.blocks.storage.CoreBlock
 *  mindustry.world.blocks.storage.CoreBlock$CoreBuild
 *  mindustry.world.meta.BlockFlag
 */
package vintadustry;

import arc.math.geom.Position;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import vintadustry.DualUnit;
import vintadustry.TestMod;

public class TaskyNest
extends CoreBlock {
    public static final Seq<Player> waiting = new Seq();
    public static final ObjectMap<Team, TaskyNestBuild> mobile = new ObjectMap();
    static final ObjectMap<Team, Tile> lastPath = new ObjectMap();
    static final ObjectFloatMap<Team> lastPathTime = new ObjectFloatMap();

    public TaskyNest(String string) {
        super(string);
        this.buildType = () -> new TaskyNestBuild();
        this.solid = false;
        this.underBullets = true;
    }

    public static void follow(DualUnit dualUnit, boolean bl) {
        Tile tile;
        TaskyNestBuild taskyNestBuild = TaskyNest.ensureMobileCore(dualUnit);
        taskyNestBuild.x = dualUnit.x;
        taskyNestBuild.y = dualUnit.y;
        if (bl && (tile = Vars.world.tileWorld(dualUnit.x, dualUnit.y)) != null && tile != taskyNestBuild.tile) {
            taskyNestBuild.tile = tile;
            Tile tile2 = (Tile)lastPath.get((Object)dualUnit.team);
            float f = lastPathTime.get((Object)dualUnit.team, -1000.0f);
            if (tile2 == null || tile2.dst((Position)tile) > 40.0f && Time.time - f > 90.0f) {
                lastPath.put((Object)dualUnit.team, (Object)tile);
                lastPathTime.put((Object)dualUnit.team, Time.time);
                Vars.pathfinder.updateTile(tile);
            }
        }
    }

    public static TaskyNestBuild ensureMobileCore(DualUnit dualUnit) {
        Seq seq;
        Team team = dualUnit.team;
        TaskyNestBuild taskyNestBuild = (TaskyNestBuild)((Object)mobile.get((Object)team));
        if (taskyNestBuild == null) {
            taskyNestBuild = (TaskyNestBuild)TestMod.nest.newBuilding().create((Block)TestMod.nest, team);
            taskyNestBuild.storageCapacity = TestMod.nest.itemCapacity;
            taskyNestBuild.x = dualUnit.x;
            taskyNestBuild.y = dualUnit.y;
            taskyNestBuild.tile = Vars.world.tileWorld(dualUnit.x, dualUnit.y);
            mobile.put((Object)team, (Object)taskyNestBuild);
            team.data().unitCap += TestMod.nest.unitCapModifier;
        }
        if (!Vars.state.teams.cores(team).contains((Object)taskyNestBuild)) {
            Vars.state.teams.registerCore((CoreBlock.CoreBuild)taskyNestBuild);
        }
        if (!(seq = Vars.indexer.getFlagged(team, BlockFlag.core)).contains((Object)taskyNestBuild)) {
            seq.add((Object)taskyNestBuild);
        }
        for (CoreBlock.CoreBuild coreBuild : new Seq(Vars.state.teams.cores(team))) {
            if (coreBuild == taskyNestBuild || !(coreBuild instanceof TaskyNestBuild) || coreBuild.tile == null || coreBuild.tile.build != coreBuild) continue;
            coreBuild.tile.setNet(Blocks.air);
        }
        return taskyNestBuild;
    }

    public static void onWorldLoad() {
        mobile.clear();
        lastPath.clear();
        lastPathTime.clear();
        Groups.unit.each(unit -> {
            DualUnit dualUnit;
            if (unit instanceof DualUnit && (dualUnit = (DualUnit)((Object)unit)).isTasky() && !dualUnit.dead) {
                TaskyNest.ensureMobileCore(dualUnit);
            }
        });
    }

    public static Player nextWaiting(Team team) {
        waiting.removeAll(player -> player == null || player.team() != team || Vars.net.active() && !player.isAdded() || !player.dead());
        return waiting.isEmpty() ? null : (Player)waiting.first();
    }

    public void init() {
        super.init();
        this.solid = false;
        this.underBullets = true;
    }

    public boolean canPlaceOn(Tile tile, Team team, int n) {
        return tile != null;
    }

    public class TaskyNestBuild
    extends CoreBlock.CoreBuild {
        public TaskyNestBuild() {
            super((CoreBlock)TaskyNest.this);
        }

        public void placed() {
            super.placed();
            if (Vars.net.client()) {
                return;
            }
            boolean bl = Groups.unit.contains(unit -> {
                DualUnit dualUnit;
                return unit instanceof DualUnit && (dualUnit = (DualUnit)((Object)unit)).isTasky() && dualUnit.team == this.team && !dualUnit.dead;
            });
            if (bl) {
                return;
            }
            Seq seq = new Seq();
            Groups.player.each(player -> {
                if (player.team() == this.team) {
                    seq.add(player);
                }
            });
            if (!seq.isEmpty()) {
                Call.playerSpawn((Tile)this.tile, (Player)((Player)seq.first()));
            }
        }

        public boolean isValid() {
            return !this.dead();
        }

        public void draw() {
        }

        public void drawTeam() {
        }

        public void requestSpawn(Player player) {
            boolean bl = Groups.unit.contains(unit -> {
                DualUnit dualUnit;
                return unit instanceof DualUnit && (dualUnit = (DualUnit)((Object)unit)).isTasky() && dualUnit.team == this.team && !dualUnit.dead;
            });
            if (!bl) {
                super.requestSpawn(player);
            } else if (!waiting.contains((Object)player) && !DualUnit.isLoaded(player)) {
                waiting.add((Object)player);
            }
        }
    }
}
