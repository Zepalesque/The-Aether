package com.aetherteam.aether.client.event;

import com.aetherteam.aether.event.EggLayEvent;
import net.minecraft.sounds.Music;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

public class AetherClientEventDispatch {

    /**
     * @see SelectMusicEvent
     */
    @Nullable
    public static Music selectMusic(Music music) {
        SelectMusicEvent e = new SelectMusicEvent(music);
        MinecraftForge.EVENT_BUS.post(e);
        return e.getMusic();
    }
}
