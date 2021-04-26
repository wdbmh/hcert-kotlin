package ehn.techiop.hcert.kotlin.chain.impl

import com.google.zxing.BarcodeFormat
import ehn.techiop.hcert.kotlin.chain.asBase64
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.random.Random

class DefaultTwoDimCodeServiceTest {

    @ParameterizedTest
    @MethodSource("inputProvider")
    fun randomEncodeDecode(input: TestInput) {
        val service = DefaultTwoDimCodeService(input.size, input.format)

        val encoded = service.encode(input.input)
        assertThat(encoded, notNullValue())
        assertThat(encoded.length, greaterThan(input.size))

        val decoded = service.decode(encoded)
        assertThat(decoded, equalTo(input.input))
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun inputProvider() = listOf(
            TestInput(Random.nextBytes(32).asBase64(), BarcodeFormat.AZTEC, 300),
            TestInput(Random.nextBytes(32).asBase64(), BarcodeFormat.AZTEC, 500),
            TestInput(Random.nextBytes(32).asBase64(), BarcodeFormat.QR_CODE, 300),
            TestInput(Random.nextBytes(32).asBase64(), BarcodeFormat.QR_CODE, 500),
        )

    }

    data class TestInput(val input: String, val format: BarcodeFormat, val size: Int)

}