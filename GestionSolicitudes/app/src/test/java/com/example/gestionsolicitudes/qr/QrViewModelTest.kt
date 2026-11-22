package com.example.gestionsolicitudes.qr

import com.example.gestionsolicitudes.data.repository.QrRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.model.QrResult
import com.example.gestionsolicitudes.viewmodel.QrViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class QrViewModelTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    fun crear(resultado: QrResult): QrViewModel {
        val repo = mockk<QrRepository>()
        coEvery { repo.procesarContenido(any(), any()) } returns resultado
        return QrViewModel(repo, SesionRepository())
    }

    "una solicitud encontrada deja su id en el estado" {
        val vm = crear(QrResult.Encontrada(5, "SOL-20261116-1234"))

        vm.onQrDetectado("SOL-20261116-1234")

        vm.estado.value.solicitudId shouldBe 5L
        vm.estado.value.mensaje.shouldBeNull()
        vm.estado.value.buscando shouldBe false
    }

    "un QR no valido informa el mensaje" {
        val vm = crear(QrResult.NoValido)

        vm.onQrDetectado("cualquier cosa")

        vm.estado.value.mensaje.shouldNotBeNull()
        vm.estado.value.solicitudId.shouldBeNull()
    }

    "reiniciar vuelve al estado inicial para escanear otro codigo" {
        val vm = crear(QrResult.NoEncontrada("SOL-20261116-9999"))
        vm.onQrDetectado("SOL-20261116-9999")

        vm.reiniciar()

        vm.estado.value.mensaje.shouldBeNull()
        vm.estado.value.codigo.shouldBeNull()
    }
})
