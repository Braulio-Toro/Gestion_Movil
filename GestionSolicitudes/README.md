# Gestión de Solicitudes (DSY1105) – Evaluación Parcial 2

**Integrantes:** _(completar con nombre y apellido de ambos integrantes)_

App Android (Kotlin, Jetpack Compose, Material 3) para registrar y consultar solicitudes de clientes de una empresa de servicios informáticos y digitales. Usa solo datos y cuentas ficticias.

## Funcionalidades
- **Login con perfiles ficticios** (cliente, administrador, técnico) y control de acceso: el cliente solo ve sus solicitudes y las edita mientras estén "Recibida"; el administrador edita todas; el técnico solo consulta.
- **Lista de solicitudes** (código, título, categoría, estado con color), búsqueda y botón para crear/consultar/editar. Se actualiza sola al guardar (Room + Flow).
- **Formulario** con validaciones por campo (íconos + mensajes), código único `SOL-AAAAMMDD-####`, fecha y hora, estado inicial "Recibida".
- **Recursos nativos:**
  1. **Cámara**: fotografía de evidencia (o galería) vinculada a la solicitud.
  2. **Lector de códigos QR** (CameraX + ML Kit): lee el QR de una solicitud y abre su detalle. Cada solicitud guardada muestra su propio QR.
  3. **Notificación local** al registrar una solicitud.
  4. **Compartir por WhatsApp** un mensaje estructurado (código, título, categoría, enlace ficticio de seguimiento).
- **Animaciones:** transiciones entre pantallas, aparición/ocultamiento animado de mensajes y evidencia, cambio animado de color del estado, flecha de listas desplegables, línea de escaneo del lector QR, reordenamiento animado de la lista.

## Arquitectura (MVVM)
`data/local` (Entity, Dao, AppDatabase) → `data/repository` → `ui/*/…ViewModel` (UiState + StateFlow) → pantallas Compose.
La lógica de validación está fuera de la UI (`model/SolicitudValidador`) y se prueba en `app/src/test`.

## Cuentas de prueba
cliente@demo.test / cliente123 · admin@demo.test / admin123 · tecnico@demo.test / tecnico123

## Cómo ejecutar
1. Abrir la carpeta en Android Studio y sincronizar Gradle.
2. Ejecutar en un emulador o dispositivo Android 7.0+ (API 24+).
3. Para probar el QR: crear una solicitud, abrirla (muestra su QR) y escanearlo con otro dispositivo/imagen en la cámara virtual del emulador.
