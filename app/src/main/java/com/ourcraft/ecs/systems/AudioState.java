package com.ourcraft.ecs.systems;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;

/**
 * Looping, non-positional background music for the whole session. Loads tolerantly — if the audio
 * asset is missing the game runs silently rather than crashing.
 */
public class AudioState extends BaseAppState {

    private static final String MUSIC_ASSET = "Sound/blockside-drizzle.wav";
    private static final float VOLUME = 0.6f;

    private AudioNode music;

    @Override
    protected void initialize(Application app) {
        AssetManager assetManager = app.getAssetManager();
        try {
            music = new AudioNode(assetManager, MUSIC_ASSET, AudioData.DataType.Stream);
            music.setLooping(true);
            music.setPositional(false);
            music.setVolume(VOLUME);
        } catch (RuntimeException e) {
            music = null;
            System.err.println("[audio] background music unavailable (" + MUSIC_ASSET + "): " + e.getMessage());
        }
    }

    @Override
    protected void cleanup(Application app) {
        if (music != null) {
            music.stop();
        }
    }

    @Override
    protected void onEnable() {
        if (music != null) {
            music.play();
        }
    }

    @Override
    protected void onDisable() {
        if (music != null) {
            music.stop();
        }
    }
}
