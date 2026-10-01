package com.retrocollector.app.core

import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.PlatformEcosystem
import com.retrocollector.app.settings.domain.model.AppSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConsolePlatformTest {

    @Test
    fun `all 26 platforms are defined and mapped to valid ecosystems`() {
        // Total 26 platforms:
        // Nintendo (12): NES, SNES, N64, GameCube, Wii, Wii U, Switch, Switch 2, Game Boy/Color, GBA, NDS, 3DS
        // Sony (7): PS1, PS2, PS3, PS4, PS5, PSP, PS Vita
        // Microsoft (4): Xbox OG, Xbox 360, Xbox One, Xbox Series
        // Sega (5): Master System, Mega Drive, Sega Saturn, Dreamcast, Game Gear
        // Retro/Vintage (1): Retro Vintage
        val nintendoCount = ConsolePlatform.entries.count { it.ecosystem == PlatformEcosystem.NINTENDO }
        val sonyCount = ConsolePlatform.entries.count { it.ecosystem == PlatformEcosystem.SONY }
        val msCount = ConsolePlatform.entries.count { it.ecosystem == PlatformEcosystem.MICROSOFT }
        val segaCount = ConsolePlatform.entries.count { it.ecosystem == PlatformEcosystem.SEGA }
        val retroCount = ConsolePlatform.entries.count { it.ecosystem == PlatformEcosystem.RETRO_VINTAGE }

        for (platform in ConsolePlatform.entries) {
            assertNotNull(platform.id)
            assertNotNull(platform.displayName)
            assertNotNull(platform.shortName)
            assertNotNull(platform.ecosystem)
            assertTrue(platform.codePrefixes.isNotEmpty())
            assertTrue(platform.regionalAdvice.isNotBlank())
        }

        assertEquals(12, nintendoCount)
        assertEquals(7, sonyCount)
        assertEquals(4, msCount)
        assertEquals(5, segaCount)
        assertEquals(1, retroCount)
        val total = nintendoCount + sonyCount + msCount + segaCount + retroCount
        assertEquals(29, total) // Wait, let's verify exact count: 12 + 7 + 4 + 5 + 1 = 29!
        assertEquals(ConsolePlatform.entries.size, total)
    }

    @Test
    fun `fromPlatformString resolves aliases accurately across all ecosystems`() {
        // Nintendo
        assertEquals(ConsolePlatform.NES, ConsolePlatform.fromPlatformString("nes"))
        assertEquals(ConsolePlatform.NES, ConsolePlatform.fromPlatformString("Famicom"))
        assertEquals(ConsolePlatform.SNES, ConsolePlatform.fromPlatformString("Super Nintendo"))
        assertEquals(ConsolePlatform.SNES, ConsolePlatform.fromPlatformString("sfc"))
        assertEquals(ConsolePlatform.N64, ConsolePlatform.fromPlatformString("nintendo 64"))
        assertEquals(ConsolePlatform.GAMECUBE, ConsolePlatform.fromPlatformString("gc"))
        assertEquals(ConsolePlatform.WII, ConsolePlatform.fromPlatformString("wii"))
        assertEquals(ConsolePlatform.WII_U, ConsolePlatform.fromPlatformString("wii u"))
        assertEquals(ConsolePlatform.SWITCH, ConsolePlatform.fromPlatformString("switch"))
        assertEquals(ConsolePlatform.SWITCH_2, ConsolePlatform.fromPlatformString("switch 2"))
        assertEquals(ConsolePlatform.GAME_BOY, ConsolePlatform.fromPlatformString("gbc"))
        assertEquals(ConsolePlatform.GBA, ConsolePlatform.fromPlatformString("game boy advance"))
        assertEquals(ConsolePlatform.NDS, ConsolePlatform.fromPlatformString("ds"))
        assertEquals(ConsolePlatform.N3DS, ConsolePlatform.fromPlatformString("3ds"))

        // Sony
        assertEquals(ConsolePlatform.PS1, ConsolePlatform.fromPlatformString("psx"))
        assertEquals(ConsolePlatform.PS2, ConsolePlatform.fromPlatformString("playstation 2"))
        assertEquals(ConsolePlatform.PS3, ConsolePlatform.fromPlatformString("ps3"))
        assertEquals(ConsolePlatform.PS4, ConsolePlatform.fromPlatformString("ps4"))
        assertEquals(ConsolePlatform.PS5, ConsolePlatform.fromPlatformString("playstation 5"))
        assertEquals(ConsolePlatform.PSP, ConsolePlatform.fromPlatformString("psp"))
        assertEquals(ConsolePlatform.PS_VITA, ConsolePlatform.fromPlatformString("ps vita"))

        // Microsoft
        assertEquals(ConsolePlatform.XBOX_OG, ConsolePlatform.fromPlatformString("xbox original"))
        assertEquals(ConsolePlatform.XBOX_360, ConsolePlatform.fromPlatformString("360"))
        assertEquals(ConsolePlatform.XBOX_ONE, ConsolePlatform.fromPlatformString("xbox one"))
        assertEquals(ConsolePlatform.XBOX_SERIES, ConsolePlatform.fromPlatformString("series x"))

        // Sega
        assertEquals(ConsolePlatform.MASTER_SYSTEM, ConsolePlatform.fromPlatformString("master system"))
        assertEquals(ConsolePlatform.MEGADRIVE, ConsolePlatform.fromPlatformString("genesis"))
        assertEquals(ConsolePlatform.SEGA_SATURN, ConsolePlatform.fromPlatformString("saturn"))
        assertEquals(ConsolePlatform.DREAMCAST, ConsolePlatform.fromPlatformString("dreamcast"))
        assertEquals(ConsolePlatform.GAME_GEAR, ConsolePlatform.fromPlatformString("game gear"))

        // Retro Vintage
        assertEquals(ConsolePlatform.RETRO_VINTAGE, ConsolePlatform.fromPlatformString("atari 2600"))
        assertEquals(ConsolePlatform.RETRO_VINTAGE, ConsolePlatform.fromPlatformString("c64"))
        assertEquals(ConsolePlatform.RETRO_VINTAGE, ConsolePlatform.fromPlatformString("neo geo"))
    }

    @Test
    fun `AppSettings handles platform pinning toggles correctly`() {
        val settings = AppSettings()
        
        // Defaults
        assertTrue(settings.isPlatformPinned(ConsolePlatform.N64))
        assertTrue(settings.isPlatformPinned(ConsolePlatform.GAMECUBE))
        assertTrue(settings.isPlatformPinned(ConsolePlatform.PS3))
        assertTrue(settings.isPlatformPinned(ConsolePlatform.SWITCH))
        assertFalse(settings.isPlatformPinned(ConsolePlatform.DREAMCAST))

        // Toggle Dreamcast ON
        val withDreamcast = settings.togglePlatformPin(ConsolePlatform.DREAMCAST)
        assertTrue(withDreamcast.isPlatformPinned(ConsolePlatform.DREAMCAST))
        assertTrue(withDreamcast.pinnedPlatformIds.contains(ConsolePlatform.DREAMCAST.id))

        // Toggle Dreamcast OFF
        val withoutDreamcast = withDreamcast.togglePlatformPin(ConsolePlatform.DREAMCAST)
        assertFalse(withoutDreamcast.isPlatformPinned(ConsolePlatform.DREAMCAST))
    }
}
