/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.graphics.g2d.TextureAtlas$AtlasRegion
 *  arc.graphics.g2d.TextureRegion
 *  arc.util.Log
 */
package vintadustry;

import arc.Core;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.util.Log;
import vintadustry.MenuLobby;

public class LobbyLogo {
    static boolean done;

    public static void swap() {
        if (done) {
            return;
        }
        done = true;
        TextureAtlas.AtlasRegion atlasRegion = null;
        atlasRegion = Core.atlas.find("lobbylogo");
        if (atlasRegion == null || !atlasRegion.found()) {
            atlasRegion = null;
        }
        if (atlasRegion == null) {
            for (TextureAtlas.AtlasRegion atlasRegion2 : Core.atlas.getRegions()) {
                if (atlasRegion2.name == null || !atlasRegion2.name.endsWith("-lobbylogo")) continue;
                atlasRegion = atlasRegion2;
                break;
            }
        }
        if (atlasRegion == null) {
            Log.info((Object)"LobbyLogo: no lobbylogo sprite found");
            return;
        }
        Core.atlas.addRegion("logo", (TextureRegion)atlasRegion);
        Log.info((Object)("LobbyLogo: replaced logo with " + atlasRegion.name));
        Core.app.post(() -> {
            try {
                MenuLobby.layout();
            }
            catch (Throwable throwable) {
                Log.err((String)"Logo relayout failed", (Throwable)throwable);
            }
        });
    }
}
