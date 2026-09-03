package com.pega.constellation.sdk.kmp.samples.basecmpapp.data

import com.pega.constellation.sdk.kmp.core.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface CaseTypesRepository {
    suspend fun fetchCaseTypes(): List<CaseType>

    companion object {
        operator fun invoke(httpClient: HttpClient, pegaUrl: String): CaseTypesRepository =
            CaseTypesRepositoryImpl(httpClient, pegaUrl)
    }
}

class CaseTypesRepositoryImpl(
    private val httpClient: HttpClient,
    private val pegaUrl: String,
) : CaseTypesRepository {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun fetchCaseTypes(): List<CaseType> {
        val endpoint = "${pegaUrl.trimEnd('/')}/api/application/v2/data_views/D_pxBootstrapConfig"
        Log.i(TAG, "Fetching case types from D_pxBootstrapConfig.")

        return try {
            val response = httpClient.get(endpoint)
            val responseBody = response.bodyAsText()
            Log.i(TAG, "Case type catalog response received: status=${response.status.value}.")

            check(response.status.value in 200..299) {
                "Case type catalog request failed with HTTP ${response.status.value}."
            }

            val bootstrap = json.decodeFromString<BootstrapEnvelope>(responseBody)
            val environment = json.decodeFromString<BootstrapConfig>(bootstrap.pyConfigJSON).environmentInfo
            val caseTypes = environment?.pyCaseTypeList
                .orEmpty()
                .asSequence()
                .filter { it.pyWorkTypeName.isNullOrBlank().not() }
                .filter { it.pyWorkTypeImplementationClassName.isNullOrBlank().not() }
                // The server normally returns only creatable types. If the optional
                // flag is present, an explicit false must still be respected.
                .filter { it.pyHasCreateAccess?.equals("false", ignoreCase = true) != true }
                .map {
                    CaseType(
                        label = it.pyWorkTypeName.orEmpty(),
                        className = it.pyWorkTypeImplementationClassName.orEmpty(),
                    )
                }
                .distinctBy { it.className }
                .sortedBy { it.label.lowercase() }
                .toList()

            Log.i(TAG, "Case type catalog parsed: count=${caseTypes.size}.")
            caseTypes
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            Log.e(TAG, "Failed to fetch case type catalog: ${error.message}", error)
            throw error
        }
    }

    @Serializable
    private data class BootstrapEnvelope(
        val pyConfigJSON: String,
    )

    @Serializable
    private data class BootstrapConfig(
        val environmentInfo: BootstrapEnvironmentInfo? = null,
    )

    @Serializable
    private data class BootstrapEnvironmentInfo(
        val pyCaseTypeList: List<BootstrapCaseType> = emptyList(),
    )

    @Serializable
    private data class BootstrapCaseType(
        val pyWorkTypeName: String? = null,
        val pyWorkTypeImplementationClassName: String? = null,
        @SerialName("pyHasCreateAccess")
        val pyHasCreateAccess: String? = null,
    )

    companion object {
        private const val TAG = "CaseTypesRepository"
    }
}
