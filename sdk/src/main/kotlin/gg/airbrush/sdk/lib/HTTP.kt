/*
 * This file is part of Airbrush
 *
 * Copyright (c) 2023 Airbrush Team
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package gg.airbrush.sdk.lib

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val client = HttpClient.newBuilder().build()

// from TDS mod, ty rosalyn <3

object HTTP {
    fun get(uri: String): HttpResponse<String> {
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(uri))
            .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun get(uri: String, headers: Map<String, String>): HttpResponse<String> {
        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(uri))

        headers.forEach {
            request.header(it.key, it.value)
        }

        return client.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    fun post(uri: String, body: Map<String, Any>): HttpResponse<String> {
        val stringBody = stringifyJSON(body)

        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.ofString(stringBody))
            .header("content-type", "application/json")
            .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun post(uri: String, body: Map<String, Any>, headers: Map<String, String>): HttpResponse<String> {
        val stringBody = stringifyJSON(body)

        val request = HttpRequest
            .newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.ofString(stringBody))
            .header("content-type", "application/json")

        headers.forEach {
            request.header(it.key, it.value)
        }

        return client.send(request.build(), HttpResponse.BodyHandlers.ofString())
    }

    fun stringifyJSON(body: Map<String, Any>): String {
        val entries = body
            .entries
            .map { entry ->
                escapeString(entry.key) + ": " + when (entry.value) {
                    is Map<*, *> -> stringifyJSON(entry.value as Map<String, Any>)
                    is Int, is Boolean -> entry.value
                    is List<*> -> stringifyList(entry.value as List<*>)
                    else -> escapeString(entry.value.toString())
                }
            }

        return "{${entries.joinToString(", ")}}"
    }

    private fun stringifyList(list: List<*>): String {
        val elements = list.map {
            when (it) {
                is Map<*, *> -> stringifyJSON(it as Map<String, Any>)
                is List<*> -> stringifyList(it)
                is Int, is Boolean -> it
                else -> escapeString(it.toString())
            }
        }
        return "[${elements.joinToString(", ")}]"
    }

    private fun escapeString(str: String): String {
        return "\"${
            str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
        }\""
    }
}