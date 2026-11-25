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
    static boolean starting;
    static int retryCount;

    public static void start() {
        if (Vars.headless) {
            return;
        }
        if (active || starting) {
            return; // Already playing or starting
        }
        starting = true;
        
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
            
            // Stop any existing music
            try {
                if (Vars.control != null && Vars.control.sound != null) {
                    // Try to stop via control if available
                    Object soundControl = Vars.control.sound;
                    // Use reflection to call stop if available
                    try {
                        soundControl.getClass().getMethod("stop").invoke(soundControl);
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
            
            if (music != null) {
                playMusicDirect();
            } else if (sound != null) {
                if (sound.countPlaying() == 0) {
                    sound.loop(0.55f);
                }
                active = true;
            }
        } catch (Throwable throwable) {
            Log.err("Lobby ambient start failed", throwable);
        } finally {
            starting = false;
        }
    }
    
    private static void playMusicDirect() {
        if (music != null && !music.isPlaying()) {
            music.setLooping(true);
            music.setVolume(0.55f);
            music.play();
        }
        active = true;
    }

    public static void stop() {
        try {
            active = false;
            starting = false;
            if (music != null) {
                music.stop();
            }
            if (sound != null) {
                sound.stop();
            }
            try {
                if (Vars.control != null && Vars.control.sound != null) {
                    // Try to stop via control if available
                    Object soundControl = Vars.control.sound;
                    try {
                        soundControl.getClass().getMethod("stop").invoke(soundControl);
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    public static void sync(boolean bl) {
        if (bl) {
            Core.app.post(LobbyAmbient::start);
        } else {
            LobbyAmbient.stop();
        }
    }
}
