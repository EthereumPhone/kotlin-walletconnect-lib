package org.walletconnect.impls

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.komputing.khex.extensions.toNoPrefixHexString
import org.walletconnect.Session
import java.io.File
import java.util.*

class SimpleImpl {
    // For easy setup
    lateinit var client : OkHttpClient
    lateinit var moshi : Moshi
    lateinit var storage : FileWCSessionStore
    lateinit var sessionConfig: Session.Config
    lateinit var session: Session
    var initialized : Boolean = false

    fun createConnection(host: String, cb: Session.Callback, appName: String, sessionStoreDir: File): String {
        if (!initialized) {
            initEasy(sessionStoreDir)
        }
        val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
        sessionConfig = Session.Config(UUID.randomUUID().toString(), host, key)
        session = WCSession(sessionConfig.toFullyQualifiedConfig(),
            MoshiPayloadAdapter(moshi),
            storage,
            OkHttpTransport.Builder(client, moshi),
            Session.PeerMeta(name = appName)
        )
        session.addCallback(cb)
        session.offer()
        return sessionConfig.toWCUri()
    }

    fun initEasy(dir: File) {
        client = OkHttpClient.Builder()
            .build()
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        storage = FileWCSessionStore(File(dir, "session_store.json").apply { createNewFile() }, moshi)
        initialized = true
    }
}
