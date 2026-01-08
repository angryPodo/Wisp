package com.angrypodo.wisp.runtime.parser

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultWispUriParserTest {

    private val parser = DefaultWispUriParser()

    @Test
    fun parse_valid_path_returns_list_of_segments() {
        val uri = Uri.parse("app://wisp/home/product")
        val result = parser.parse(uri)
        assertEquals(listOf("home", "product"), result)
    }

    @Test
    fun parse_empty_path_returns_empty_list() {
        val uri = Uri.parse("app://wisp")
        val result = parser.parse(uri)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun parse_trailing_slash_returns_empty_list_for_root() {
        val uri = Uri.parse("app://wisp/")
        val result = parser.parse(uri)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun parse_path_with_query_parameters_ignores_query() {
        val uri = Uri.parse("app://wisp/product?id=123")
        val result = parser.parse(uri)
        assertEquals(listOf("product"), result)
    }
}