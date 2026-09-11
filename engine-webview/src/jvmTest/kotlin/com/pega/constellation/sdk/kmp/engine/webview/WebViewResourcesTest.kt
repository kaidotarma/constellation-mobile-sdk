package com.pega.constellation.sdk.kmp.engine.webview

import com.pega.constellation.sdk.kmp.engine_webview.generated.resources.Res
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContains

class WebViewResourcesTest {
    @Test
    fun bundledWebViewEntryPointsAreAvailable() = runBlocking {
        val indexHtml = Res.readBytes("files/scripts/index.html").decodeToString()
        val initScript = Res.readBytes("files/scripts/init/init.js").decodeToString()
        val nativeBridge = Res.readBytes("files/scripts/bridge/native-bridge.js").decodeToString()

        assertContains(indexHtml, "/constellation-mobile-sdk-assets/scripts/init/init.js")
        assertContains(initScript, "window.init = init")
        assertContains(nativeBridge, "export const bridge")
    }
}
