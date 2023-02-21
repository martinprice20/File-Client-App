package com.martinprice20.fileclientapp


import org.spongycastle.asn1.ASN1ObjectIdentifier
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x509.AlgorithmIdentifier
import org.spongycastle.asn1.x509.BasicConstraints
import org.spongycastle.asn1.x509.Extension
import org.spongycastle.asn1.x509.ExtensionsGenerator
import org.spongycastle.operator.ContentSigner
import org.spongycastle.operator.OperatorCreationException
import org.spongycastle.pkcs.PKCS10CertificationRequest
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.PrivateKey
import java.security.Signature


class CsrHelper {

    private class JCESigner(privateKey: PrivateKey, sigAlgo: String) : ContentSigner {

        private val ALGOS = HashMap<String, AlgorithmIdentifier>().apply {
            this["SHA256withRSA".lowercase()] = AlgorithmIdentifier(
                ASN1ObjectIdentifier("1.2.840.113549.1.1.11")
            )
            this["SHA1withRSA".lowercase()] = AlgorithmIdentifier(
                ASN1ObjectIdentifier("1.2.840.113549.1.1.5")
            )
        }

        private val mAlgo = sigAlgo.lowercase()
        private var signature: Signature
        private var outputStream: ByteArrayOutputStream

        init {
            try {
                this.outputStream = ByteArrayOutputStream()
                this.signature = Signature.getInstance(sigAlgo)
                this.signature.initSign(privateKey)
            } catch (gse: GeneralSecurityException) {
                throw java.lang.IllegalArgumentException(gse.toString())
            }
        }

        override fun getAlgorithmIdentifier(): AlgorithmIdentifier {
            val id = ALGOS.get(mAlgo)
            if (id == null) {
                throw java.lang.IllegalArgumentException("Does not support algo: $mAlgo")
            }
            return id
        }

        override fun getOutputStream(): OutputStream {
            return outputStream
        }

        override fun getSignature(): ByteArray {
            try {
                signature.update(outputStream.toByteArray())
                return signature.sign()
            } catch (gse: GeneralSecurityException) {
                gse.printStackTrace()
                return ByteArray(0)
            }
        }
    }

    //Create the certificate signing request (CSR) from private and public keys
    @Throws(IOException::class, OperatorCreationException::class)
    fun generateCSR(keyPair: KeyPair, cn: String?): PKCS10CertificationRequest? {
        val principal = String.format(CN_PATTERN, cn)
        val signer: ContentSigner = JCESigner(keyPair.private, DEFAULT_SIGNATURE_ALGORITHM)
        val csrBuilder: PKCS10CertificationRequestBuilder =
            JcaPKCS10CertificationRequestBuilder(
                X500Name(principal), keyPair.public
            )
        val extensionsGenerator = ExtensionsGenerator()
        extensionsGenerator.addExtension(
            Extension.basicConstraints, true, BasicConstraints(
                true
            )
        )
        csrBuilder.addAttribute(
            PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
            extensionsGenerator.generate()
        )
        return csrBuilder.build(signer)
    }

    companion object {
        private const val CN_PATTERN = "CN=%s, O=Aralink, OU=OrgUnit"
        private const val DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA"
    }
}