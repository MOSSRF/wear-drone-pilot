package io.github.mossrf.dronewatch.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WearProtocolTest {

    @Test
    fun `state round trips through JSON without data loss`() {
        val state = DroneState(
            timestamp = 123456L,
            connected = true,
            model = "MINI_4_PRO",
            sdkStatus = "REGISTERED",
            batteryPercent = 87,
            satellites = 16,
            altitudeM = 12.5f,
            distanceM = 42f,
            speedMS = 3.2f,
            signalQuality = 95,
            remainingFlightTimeS = 900,
            recording = true,
            cameraAvailable = true,
        )

        val json = WearProtocol.json.encodeToString(DroneState.serializer(), state)
        val decoded = WearProtocol.json.decodeFromString(DroneState.serializer(), json)

        assertEquals(state, decoded)
    }

    @Test
    fun `command envelope round trips and stays in supported set`() {
        val envelope = CommandEnvelope(DroneCommand.RETURN_TO_HOME, 999L)
        val json = WearProtocol.json.encodeToString(CommandEnvelope.serializer(), envelope)
        val decoded = WearProtocol.json.decodeFromString(CommandEnvelope.serializer(), json)

        assertEquals(envelope, decoded)
        assertTrue(decoded.command in DroneCommand.entries)
    }

    @Test
    fun `unknown JSON fields are ignored for forward compatibility`() {
        val raw = """{"timestamp":1,"connected":true,"futureField":"x"}"""
        val state = WearProtocol.json.decodeFromString(DroneState.serializer(), raw)
        assertTrue(state.connected)
    }
}
