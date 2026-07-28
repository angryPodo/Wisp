package com.angrypodo.wisp.runtime

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.angrypodo.wisp.runtime.spi.RouteFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WispTest {

    private fun createWisp(onError: ((WispError) -> Unit)? = null): Wisp {
        return Wisp(
            routes = mapOf(
                "home" to RouteFactory { "home" },
                "product/{productId}" to RouteFactory { params ->
                    "product:${params["productId"]}:${params["showReviews"]}"
                },
                "user/{userId}" to RouteFactory { params ->
                    "user:${requireNotNull(params["userId"]).toInt()}"
                }
            ),
            onError = onError
        )
    }

    @Test
    fun resolve_success_builds_routes_in_backstack_order() {
        val result = createWisp()
            .resolve(Uri.parse("app://wisp/home/product/123?showReviews=true"))

        assertTrue(result is WispResult.Success)
        assertEquals(
            listOf("home", "product:123:true"),
            (result as WispResult.Success).routes
        )
    }

    @Test
    fun path_variable_overrides_query_parameter() {
        val result = createWisp()
            .resolve(Uri.parse("app://wisp/product/123?productId=999"))

        assertTrue(result is WispResult.Success)
        assertEquals(listOf("product:123:null"), (result as WispResult.Success).routes)
    }

    @Test
    fun unknown_segment_returns_failure_and_invokes_onError() {
        var captured: WispError? = null
        val result = createWisp(onError = { captured = it })
            .resolve(Uri.parse("app://wisp/home/unknown"))

        assertTrue(result is WispResult.Failure)
        val error = (result as WispResult.Failure).error
        assertTrue(error is WispError.UnknownPath)
        assertEquals("unknown", (error as WispError.UnknownPath).segment)
        assertEquals(error, captured)
    }

    @Test
    fun empty_path_returns_parsing_failed() {
        val result = createWisp().resolve(Uri.parse("app://wisp"))

        assertTrue(result is WispResult.Failure)
        assertTrue((result as WispResult.Failure).error is WispError.ParsingFailed)
    }

    @Test
    fun factory_conversion_error_returns_invalid_parameter() {
        val result = createWisp().resolve(Uri.parse("app://wisp/user/abc"))

        assertTrue(result is WispResult.Failure)
        assertTrue((result as WispResult.Failure).error is WispError.InvalidParameter)
    }

    @Test
    fun deferred_uri_is_consumed_only_once() {
        val wisp = createWisp()
        val uri = Uri.parse("app://wisp/home")

        wisp.defer(uri)

        assertEquals(uri, wisp.consumeDeferredUri())
        assertNull(wisp.consumeDeferredUri())
    }
}
