/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.audio.Music
 *  arc.audio.Sound
 *  arc.util.Log
 *  mindustry.Vars
 */
package vintadustry;

import arc.Core;
import arc.audio.Music;
import arc.audio.Sound;
import arc.util.Log;
import mindustry.Vars;

public class LobbyAmbient {
    static Music music;
    static Sound sound;
    static boolean tried;
    static boolean active;

    public static void start() {
        block16: {
            if (Vars.headless) {
                return;
            }
            try {
                if (!tried) {
                    tried = true;
                    music = Vars.tree.loadMusic("vd-lobby-ambient");
                    if (music == null) {
                        sound = Vars.tree.loadSound("vd-lobby-ambient");
                    } else {
                        music.setLooping(true);
                        music.setVolume(0.55f);
                    }
                }
                try {
                    if (Vars.control != null && Vars.control.sound != null) {
                        Vars.control.sound.stop();
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                if (music != null) {
                    try {
                        if (Vars.control != null && Vars.control.sound != null) {
                            Vars.control.sound.playMusic(music, true);
                            active = true;
                            return;
                        }
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    if (!music.isPlaying()) {
                        music.setLooping(true);
                        music.setVolume(0.55f);
                        music.play();
                    }
                    active = true;
                    break block16;
                }
                if (sound != null) {
                    if (sound.countPlaying() == 0) {
                        sound.loop(0.55f);
                    }
                    active = true;
                }
            }
            catch (Throwable throwable) {
                Log.err((String)"Lobby ambient start failed", (Throwable)throwable);
            }
        }
    }

    public static void stop() {
        try {
            active = false;
            if (music != null) {
                music.stop();
            }
            if (sound != null) {
                sound.stop();
            }
            try {
                if (Vars.control != null && Vars.control.sound != null && Vars.control.sound.getCurrent() == music) {
                    Vars.control.sound.stop();
                }
            }
            catch (Throwable throwable) {}
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void sync(boolean bl) {
        if (bl) {
            Core.app.post(LobbyAmbient::start);
        } else {
            LobbyAmbient.stop();
        }
    }
}
