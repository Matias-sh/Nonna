package com.cocido.nonna.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class NetworkFailureMessageResolverTest {

    @Test
    fun timeoutIOException_retornaMensajeTimeout() {
        val message = NetworkFailureMessageResolver.fromIOException(SocketTimeoutException("timeout"))
        assertEquals(UserMessages.REQUEST_TIMEOUT, message)
    }

    @Test
    fun ioExceptionGenerica_sinContextoRetornaServerUnreachable() {
        val message = NetworkFailureMessageResolver.fromIOException(IOException("connection reset"))
        assertEquals(UserMessages.SERVER_UNREACHABLE, message)
    }
}
