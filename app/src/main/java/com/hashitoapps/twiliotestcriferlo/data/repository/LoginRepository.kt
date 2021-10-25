package com.hashitoapps.twiliotestcriferlo.data.repository

import android.content.Context
import com.hashitoapps.twiliotestcriferlo.data.extra.ReceivedCookiesInterceptor
import com.hashitoapps.twiliotestcriferlo.data.extra.AddCookiesInterceptor
import com.hashitoapps.twiliotestcriferlo.data.extra.Call.safeApiCall
import com.hashitoapps.twiliotestcriferlo.data.extra.ResultWrapper
import com.hashitoapps.twiliotestcriferlo.data.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class LoginRepository(
    val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    class ServiceBuilder(val context: Context) {
        private val url = "http://192.168.1.13"
        private val endpoint = "$url:5000"

        fun <T> buildService(service: Class<T>): T {

            var interceptor = HttpLoggingInterceptor()
            interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
            val client: OkHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(AddCookiesInterceptor(context))
                .addInterceptor(ReceivedCookiesInterceptor(context))
                .connectTimeout(10, TimeUnit.MINUTES)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(endpoint)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(service)
        }
    }

    fun logout() {
        user = null
    }

    suspend fun login(name: String, password: String): ResultWrapper<LoggedInUserResponse> {
        val service = ServiceBuilder(context).buildService(IRemoteRepository::class.java)

        return safeApiCall(dispatcher) {
            service.login(LoggedInUser(name, password))
        }

    }

    suspend fun tokenDevice(id: String): ResultWrapper<TokenDeviceResponse> {
        val service = ServiceBuilder(context).buildService(IRemoteRepository::class.java)
        return safeApiCall(dispatcher) {
            service.tokenDevice(TokenDevice(id))
        }
    }

    suspend fun registerDevice(id: String, sid: String): ResultWrapper<RegisterDeviceResponse> {
        val service = ServiceBuilder(context).buildService(IRemoteRepository::class.java)
        return safeApiCall(dispatcher) {
            service.registerDevice(RegisterDevice(id, sid))
        }
    }
}