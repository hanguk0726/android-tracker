package com.openreplay.tracker

import com.openreplay.tracker.managers.ScreenshotArchiveFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ScreenshotLeftoversTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun touch(dir: File, name: String) = File(dir.apply { mkdirs() }, name).apply { writeText("x") }

    // An archive that failed to upload under an earlier session (401 after its token expired)
    // must not survive into the next session, whose token the uploader would send it with.
    @Test
    fun `new session start removes earlier sessions' archives and frames`() {
        val filesDir = tmp.root
        val archives = File(filesDir, ScreenshotArchiveFormat.ARCHIVES_DIR)
        val screenshots = File(filesDir, ScreenshotArchiveFormat.SCREENSHOTS_DIR)
        touch(archives, ScreenshotArchiveFormat.archiveName("4079209627341967775", "1761609240000", frames = true))
        touch(archives, ScreenshotArchiveFormat.archiveName("4079209627341967775", "1761609250000", frames = false))
        touch(archives, ScreenshotArchiveFormat.tmpName("unknown-1761609260000.gz"))
        touch(screenshots, "1761609270000.jpeg")

        val removed = ScreenshotArchiveFormat.purgeLeftovers(filesDir)

        assertEquals(4, removed)
        assertTrue(archives.listFiles().orEmpty().isEmpty())
        assertTrue(screenshots.listFiles().orEmpty().isEmpty())
    }

    @Test
    fun `purge leaves other files in filesDir alone and tolerates missing folders`() {
        val filesDir = tmp.root
        val unrelated = touch(filesDir, "lateMessages.dat")

        assertEquals(0, ScreenshotArchiveFormat.purgeLeftovers(filesDir))
        assertTrue(unrelated.exists())
    }
}
