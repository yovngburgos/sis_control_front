package com.siscontrol.mobile.di

import com.siscontrol.mobile.domain.usecase.RegisterNfcScanUseCase
import com.siscontrol.mobile.core.Config
import com.siscontrol.mobile.data.remote.AuthApiService
import com.siscontrol.mobile.data.remote.GuardFlowApiService
import com.siscontrol.mobile.data.remote.InstallationApiService
import com.siscontrol.mobile.data.remote.PersonnelApiService
import com.siscontrol.mobile.data.repository.AuthRepositoryImpl
import com.siscontrol.mobile.data.repository.GuardFlowRepositoryImpl
import com.siscontrol.mobile.data.repository.InstallationRepositoryImpl
import com.siscontrol.mobile.data.repository.PersonnelRepositoryImpl
import com.siscontrol.mobile.domain.usecase.CreatePersonnelUseCase
import com.siscontrol.mobile.domain.usecase.GetCheckpointsUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import com.siscontrol.mobile.domain.usecase.GetPersonnelUseCase
import com.siscontrol.mobile.domain.usecase.FinishRoundUseCase
import com.siscontrol.mobile.domain.usecase.FinishShiftUseCase
import com.siscontrol.mobile.domain.usecase.LoginUseCase
import com.siscontrol.mobile.domain.usecase.RegisterScanUseCase
import com.siscontrol.mobile.domain.usecase.StartRoundUseCase
import com.siscontrol.mobile.domain.usecase.StartShiftUseCase
import com.siscontrol.mobile.presentation.guard.GuardFlowViewModel
import com.siscontrol.mobile.presentation.login.LoginViewModel
import com.siscontrol.mobile.presentation.management.CreatePersonnelViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking

/**
 * Módulo de Inyección de Dependencias manual (sin Hilt/Dagger).
 *
 * Actúa como el único punto de ensamblaje de la aplicación:
 *   Retrofit → ApiService → RepositoryImpl → UseCase → ViewModel
 *
 * La URL base es configurable por Gradle con API_BASE_URL y debe apuntar
 * a una IP/dominio accesible desde el emulador o dispositivo físico.
 */
object AppModule {

    /** URL del backend, configurable vía -PAPI_BASE_URL. */
    private val BASE_URL: String = Config.BASE_URL

    // Gestor de sesión persistente (DataStore)
    private lateinit var sessionManager: SessionManager
    
    // Canal de eventos para notificar errores 401 a la UI
    private val _unauthorizedEvent = MutableSharedFlow<Unit>()
    val unauthorizedEvent = _unauthorizedEvent.asSharedFlow()

    /**
     * Inicializa el módulo con el contexto de la aplicación.
     */
    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    fun getSessionManager() = sessionManager

    // -------------------------------------------------------------------------
    // OkHttpClient — con Interceptor de Autenticación y Manejo de 401
    // -------------------------------------------------------------------------

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val originalRequest = chain.request()
                val token = sessionManager.getTokenSync()

                val newRequest = if (!token.isNullOrBlank()) {
                    Log.d("SISControlAPI", "Inyectando JWT en petición a: ${originalRequest.url()}")
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }

                val response: Response = chain.proceed(newRequest)

                // Si detectamos un 401, limpiamos la sesión y notificamos a la app
                if (response.code() == 401) {
                    Log.e("SISControlAPI", "Sesión expirada o inválida (401). Cerrando sesión...")
                    runBlocking {
                        sessionManager.clearSession()
                        _unauthorizedEvent.emit(Unit)
                    }
                } else if (response.isSuccessful) {
                    Log.d("SISControlAPI", "Respuesta exitosa de: ${originalRequest.url()}")
                }

                response
            }
            .build()
    }

    // -------------------------------------------------------------------------
    // Retrofit — cliente HTTP único compartido por toda la app
    // -------------------------------------------------------------------------

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <--- Usamos el cliente con interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // -------------------------------------------------------------------------
    // API Services
    // -------------------------------------------------------------------------

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val personnelApiService: PersonnelApiService by lazy {
        retrofit.create(PersonnelApiService::class.java)
    }

    val installationApiService: InstallationApiService by lazy {
        retrofit.create(InstallationApiService::class.java)
    }

    val guardFlowApiService: GuardFlowApiService by lazy {
        retrofit.create(GuardFlowApiService::class.java)
    }

    // -------------------------------------------------------------------------
    // Repositories (implementaciones concretas de las interfaces del Dominio)
    // -------------------------------------------------------------------------

    private val authRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }

    private val personnelRepository by lazy {
        PersonnelRepositoryImpl(personnelApiService)
    }

    private val installationRepository by lazy {
        InstallationRepositoryImpl(installationApiService)
    }

    private val guardFlowRepository by lazy {
        GuardFlowRepositoryImpl(guardFlowApiService)
    }

    // -------------------------------------------------------------------------
    // Use Cases
    // -------------------------------------------------------------------------

    private val loginUseCase         by lazy { LoginUseCase(authRepository) }
    val getPersonnelUseCase          by lazy { GetPersonnelUseCase(personnelRepository) }
    val createPersonnelUseCase       by lazy { CreatePersonnelUseCase(personnelRepository) }
    val getInstallationsUseCase      by lazy { GetInstallationsUseCase(installationRepository) }
    private val getCheckpointsUseCase    by lazy { GetCheckpointsUseCase(installationRepository) }
    private val startShiftUseCase     by lazy { StartShiftUseCase(guardFlowRepository) }
    private val finishShiftUseCase    by lazy { FinishShiftUseCase(guardFlowRepository) }
    private val startRoundUseCase     by lazy { StartRoundUseCase(guardFlowRepository) }
    private val finishRoundUseCase    by lazy { FinishRoundUseCase(guardFlowRepository) }
    private val registerScanUseCase   by lazy { RegisterScanUseCase(guardFlowRepository) }

    private val registerNfcScanUseCase by lazy {
        RegisterNfcScanUseCase(guardFlowRepository)
    }

    // -------------------------------------------------------------------------
    // ViewModel Factories
    // -------------------------------------------------------------------------

    /**
     * Crea una nueva instancia de [LoginViewModel].
     */
    fun provideLoginViewModel(): LoginViewModel =
        LoginViewModel(loginUseCase)

    /**
     * Crea una nueva instancia de [CreatePersonnelViewModel].
     */
    fun provideCreatePersonnelViewModel(): CreatePersonnelViewModel =
        CreatePersonnelViewModel(createPersonnelUseCase, getInstallationsUseCase)

    fun provideGuardFlowViewModel(): GuardFlowViewModel =
        GuardFlowViewModel(
            sessionManager = sessionManager,
            startShiftUseCase = startShiftUseCase,
            finishShiftUseCase = finishShiftUseCase,
            startRoundUseCase = startRoundUseCase,
            finishRoundUseCase = finishRoundUseCase,
            registerScanUseCase = registerScanUseCase,
            getInstallationsUseCase = getInstallationsUseCase,
            getCheckpointsUseCase = getCheckpointsUseCase,
            registerNfcScanUseCase = registerNfcScanUseCase,
        )
}
