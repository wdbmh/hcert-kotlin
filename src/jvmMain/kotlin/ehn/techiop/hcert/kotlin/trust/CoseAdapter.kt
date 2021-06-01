package ehn.techiop.hcert.kotlin.trust

import COSE.MessageTag
import COSE.OneKey
import COSE.Sign1Message
import com.upokecenter.cbor.CBORObject
import ehn.techiop.hcert.kotlin.chain.CertificateRepository
import ehn.techiop.hcert.kotlin.chain.VerificationResult
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

actual class CoseAdapter actual constructor(private val input: ByteArray) {
    val sign1Message = Sign1Message.DecodeFromBytes(input, MessageTag.Sign1) as Sign1Message
    val cwtMap = CBORObject.DecodeFromBytes(sign1Message.GetContent())

    init {
        Security.addProvider(BouncyCastleProvider()) // for SHA256withRSA/PSS
    }

    actual fun getProtectedAttributeByteArray(key: Int) =
        sign1Message.protectedAttributes[key]?.GetByteString()

    actual fun getUnprotectedAttributeByteArray(key: Int) =
        sign1Message.unprotectedAttributes[key]?.GetByteString()

    actual fun getProtectedAttributeInt(key: Int) =
        sign1Message.protectedAttributes[key]?.AsInt32()

    actual fun validate(kid: ByteArray, repository: CertificateRepository): Boolean {
        repository.loadTrustedCertificates(kid, VerificationResult()).forEach {
            if (sign1Message.validate(it.cosePublicKey.toCoseRepresentation() as OneKey)) {
                return true
            }
        }
        return false
    }

    actual fun validate(
        kid: ByteArray,
        repository: CertificateRepository,
        verificationResult: VerificationResult
    ): Boolean {
        repository.loadTrustedCertificates(kid, verificationResult).forEach { trustedCert ->
            verificationResult.certificateValidFrom = trustedCert.validFrom
            verificationResult.certificateValidUntil = trustedCert.validUntil
            verificationResult.certificateValidContent = trustedCert.validContentTypes
            if (sign1Message.validate(trustedCert.cosePublicKey.toCoseRepresentation() as OneKey)) {
                verificationResult.coseVerified = true
                return true
            }
        }
        return false
    }

    actual fun getContent() = sign1Message.GetContent()

    actual fun getMapEntryByteArray(value: Int) = cwtMap[value]?.GetByteString()

    actual fun getMapEntryNumber(value: Int) = cwtMap[value]?.AsInt64() as Number?
}