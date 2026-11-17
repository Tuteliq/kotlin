package ai.tuteliq

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes an enum by its wire value. A value the SDK does not know decodes to
 * [fallback], so a status the API adds later does not break decoding; with no
 * fallback it throws. Several entries may share one wire value (a deprecated
 * alias): decoding returns the first.
 */
internal open class WireEnumSerializer<T : Enum<T>>(
    serialName: String,
    private val values: List<T>,
    private val wireValue: (T) -> String,
    private val fallback: T?
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(wireValue(value))

    override fun deserialize(decoder: Decoder): T {
        val raw = decoder.decodeString()
        return values.firstOrNull { wireValue(it) == raw }
            ?: fallback
            ?: throw SerializationException("Unknown value '$raw' for ${descriptor.serialName}")
    }
}
