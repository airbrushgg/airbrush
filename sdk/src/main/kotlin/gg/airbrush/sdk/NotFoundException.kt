package gg.airbrush.sdk
import kotlin.Exception

// Not really too useful, but it helps distinguish errors.
// Ex:
// Exception in thread "main" NotFoundException: Player with UUID of ac2553d3-dbc6-45d2-ba79-86638fbf5701 not found.

class NotFoundException(message: String) : Exception(message) {}