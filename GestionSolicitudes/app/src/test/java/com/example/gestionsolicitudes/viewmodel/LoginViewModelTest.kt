package com.example.gestionsolicitudes.viewmodel

import com.example.gestionsolicitudes.data.repository.AuthRepository
import com.example.gestionsolicitudes.data.repository.SesionRepository
import com.example.gestionsolicitudes.model.Perfil
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

/** El ViewModel ya no depende de Android: se prueba con repositorios reales en memoria. */
class LoginViewModelTest : StringSpec({

    fun crear(): Pair<LoginViewModel, SesionRepository> {
        val sesion = SesionRepository()
        return LoginViewModel(AuthRepository(), sesion) to sesion
    }

    "con campos vacios informa los errores y no abre sesion" {
        val (vm, sesion) = crear()

        vm.iniciarSesion()

        vm.estado.value.correoError.shouldNotBeNull()
        vm.estado.value.claveError.shouldNotBeNull()
        vm.estado.value.loginExitoso shouldBe false
        sesion.usuario.value.shouldBeNull()
    }

    "con credenciales incorrectas informa el error general" {
        val (vm, sesion) = crear()
        vm.onCorreoChange("cliente@demo.test")
        vm.onClaveChange("incorrecta")

        vm.iniciarSesion()

        vm.estado.value.errorGeneral.shouldNotBeNull()
        sesion.usuario.value.shouldBeNull()
    }

    "con credenciales validas guarda la sesion y marca el login como exitoso" {
        val (vm, sesion) = crear()
        vm.onCorreoChange("admin@demo.test")
        vm.onClaveChange("admin123")

        vm.iniciarSesion()

        vm.estado.value.loginExitoso shouldBe true
        sesion.usuario.value?.perfil shouldBe Perfil.ADMINISTRADOR
    }

    "consumirLoginExitoso limpia el evento de un solo uso" {
        val (vm, _) = crear()
        vm.usarCuenta(vm.cuentasDemo.first())
        vm.iniciarSesion()

        vm.consumirLoginExitoso()

        vm.estado.value.loginExitoso shouldBe false
    }
})
