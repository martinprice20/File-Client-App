package com.martinprice20.fileclientapp

import android.util.Base64
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom


class CryptoHelper {

    private lateinit var keyStore: KeyStore
    private var csrHelper: CsrHelper = CsrHelper()

    init {
        generateECkeys()
    }

    fun getPublicKey(): String {
        return DEVICE_PUBLIC
    }

    fun generateECkeys(): String {
        try {
            val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
            val keyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider())
            keyPairGenerator.initialize(ecSpec, SecureRandom())
            val pair = keyPairGenerator!!.generateKeyPair()

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val publicKey: String = Base64.encodeToString(
                pair.public.encoded,
                Base64.DEFAULT
            )
            val privateKey: String = Base64.encodeToString(
                pair.private.encoded,
                Base64.DEFAULT
            )

            DEVICE_PUBLIC = publicKey
            DEVICE_PRIVATE = privateKey

//            val csr: PKCS10CertificationRequest? = csrHelper.generateCSR(pair, "google.com")
//            val byteArray = csr?.encoded
//            val decoded = Base64.decode(byteArray, Base64.NO_WRAP)
//            val inputStream = ByteArrayInputStream(decoded)
//
//            val certificate: X509Certificate = CertificateFactory.getInstance("X.509").generateCertificate(
//                inputStream) as X509Certificate
//
//            keyStore.setKeyEntry(ALIAS, pair.private.encoded, arrayOf(certificate))

            return publicKey

        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            return DEVICE_PUBLIC
        }
    }

    companion object {
        const val ALIAS = "SECRET_KEY"
        private var DEVICE_PUBLIC = ""
        private var DEVICE_PRIVATE = ""
    }
}