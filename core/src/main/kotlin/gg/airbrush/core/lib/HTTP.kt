package gg.airbrush.core.lib

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

// TODO: Move to SDK one day.

private val client = HttpClient.newBuilder().build()

fun get(uri: String): HttpResponse<String> {
    val request = HttpRequest
        .newBuilder()
        .uri(URI.create(uri))
        .build()

    return client.send(request, HttpResponse.BodyHandlers.ofString())
}