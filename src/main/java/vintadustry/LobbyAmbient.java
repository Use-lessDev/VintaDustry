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

    public static void start() {
        if (Vars.headless) {
            return;
        }
        if (active || starting) {
            Log.debug("[LobbyAmbient] Already playing or starting, skipping");
            return;
        }
        starting = true;
        Log.info("[LobbyAmbient] Starting lobby ambient music...");
        
        try {
            if (!tried) {
                tried = true;
                Log.debug("[LobbyAmbient] Loading music asset: vd-lobby-ambient");
                music = Vars.tree.loadMusic("vd-lobby-ambient");
                if (music == null) {
                    Log.warn("[LobbyAmbient] Music not found, trying sound fallback");
                    sound = Vars.tree.loadSound("vd-lobby-ambient");
                } else {
                    music.setLooping(true);
                    music.setVolume(0.55f);
                    Log.debug("[LobbyAmbient] Music loaded successfully, volume=0.55, looping=true");
                }
            }
            
            // Stop any existing music
            try {
                if (Vars.control != null && Vars.control.sound != null) {
                    Object soundControl = Vars.control.sound;
                    try {
                        soundControl.getClass().getMethod("stop").invoke(soundControl);
                        Log.debug("[LobbyAmbient] Stopped existing music via SoundControl");
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
                Log.info("[LobbyAmbient] Started lobby ambient via Sound fallback");
            }
        } catch (Throwable throwable) {
            Log.err("[LobbyAmbient] Failed to start lobby ambient", throwable);
        } finally {
            starting = false;
        }
    }
    
    private static void playMusicDirect() {
        if (music != null && !music.isPlaying()) {
            music.setLooping(true);
            music.setVolume(0.55f);
            music.play();
            Log.info("[LobbyAmbient] Playing lobby music directly (Music.play())");
        } else if (music != null && music.isPlaying()) {
            Log.debug("[LobbyAmbient] Music already playing");
        } else {
            Log.warn("[LobbyAmbient] playMusicDirect called but music is null");
        }
        active = true;
    }
    
    public static void ensurePlaying() {
        if (!active || !tried) {
            return;
        }
        if (music != null && !music.isPlaying()) {
            Log.warn("[LobbyAmbient] Music stopped unexpectedly, restarting...");
            playMusicDirect();
        }
        // Check and stop any other music that might have started (Mindustry default menu music)
        if (Vars.control != null && Vars.control.sound != null) {
            Object soundControl = Vars.control.sound;
            try {
                Object currentMusic = soundControl.getClass().getMethod("getCurrent").invoke(soundControl);
                if (currentMusic != null && currentMusic != music) {
                    Log.warn("[LobbyAmbient] Different music detected, stopping it and restarting ours");
                    soundControl.getClass().getMethod("stop").invoke(soundControl);
                    playMusicDirect();
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void stop() {
        try {
            active = false;
            starting = false;
            if (music != null) {
                music.stop();
                Log.debug("[LobbyAmbient] Music stopped");
            }
            if (sound != null) {
                sound.stop();
                Log.debug("[LobbyAmbient] Sound stopped");
            }
            try {
                if (Vars.control != null && Vars.control.sound != null) {
                    Object soundControl = Vars.control.sound;
                    try {
                        soundControl.getClass().getMethod("stop").invoke(soundControl);
                        Log.debug("[LobbyAmbient] Stopped via SoundControl reflection");
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
