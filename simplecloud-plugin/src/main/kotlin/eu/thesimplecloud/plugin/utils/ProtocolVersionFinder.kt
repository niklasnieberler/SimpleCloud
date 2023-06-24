package eu.thesimplecloud.plugin.utils

import com.google.gson.JsonParser
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.experimental.or


/**
 * Created by MrManHD
 * Class create at 24.06.2023 14:53
 */

class ProtocolVersionFinder(
    private val host: String,
    private val port: Int
) {

    fun getProtocolVersion(): Int {
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), 1000)

        val out = DataOutputStream(socket.getOutputStream())
        val `in` = DataInputStream(socket.getInputStream())

        // Handshake packet
        out.writeByte(0x0F) // packet size
        out.writeByte(0x00) // packet ID for handshake
        writeVarInt(out, 578) // protocol version 578 corresponds to 1.15.2

        writeString(out, host)
        out.writeShort(port)
        out.writeByte(1) // status handshake state

        // Status request packet
        out.writeByte(0x01) // size of packet
        out.writeByte(0x00) // packet ID for status request

        // Reading status response packet
        readVarInt(`in`) // size of packet

        val id = readVarInt(`in`) // packet ID


        if (id == -1) {
            throw IOException("Premature end of stream.")
        }
        if (id != 0x00) {
            throw IOException("Invalid packet ID")
        }

        // Parsing the JSON response to extract the protocol version
        val data = ByteArray(readVarInt(`in`))
        `in`.readFully(data)
        `in`.close()
        out.close()
        socket.close()

        val element = JsonParser().parse(String(data, StandardCharsets.UTF_8))
        return element.asJsonObject["version"].asJsonObject["protocol"].asInt
    }

    fun writeVarInt(out: DataOutputStream, value: Int) {
        var value = value
        do {
            var temp = (value and 0b01111111).toByte()
            // Note that bitwise shift with assignment will produce an error
            value = value ushr 7
            if (value != 0) {
                temp = temp or 0b10000000.toByte()
            }
            out.writeByte(temp.toInt())
        } while (value != 0)
    }

    private fun writeString(out: DataOutputStream, string: String) {
        val bytes = string.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(out, bytes.size)
        out.write(bytes)
    }

    private fun readVarInt(`in`: DataInputStream): Int {
        var numRead = 0
        var result = 0
        var read: Byte
        do {
            read = `in`.readByte()
            val value = (read.toInt() and 0b01111111)
            result = result or (value shl 7 * numRead)
            numRead++
            if (numRead > 5) {
                throw RuntimeException("VarInt is too big")
            }
        } while ((read.toInt() and 0b10000000) != 0)

        return result
    }

}