# Gestión de Solicitudes (DSY1105) – Evaluación Parcial 2

**Integrantes:** _(completar con nombre y apellido de ambos integrantes)_

App Android (Kotlin, Jetpack Compose, Material 3) para registrar y consultar solicitudes de clientes de una empresa de servicios informáticos y digitales. Usa solo datos y cuentas ficticias.

## Funcionalidades
- **Login con perfiles ficticios** (cliente, administrador, técnico) y control de acceso: el cliente solo ve sus solicitudes y las edita mientras estén "Recibida"; el administrador edita todas; el técnico consulta los datos y solo gestiona estado y solución de las solicitudes que tiene asignadas. **Eliminar** (D del CRUD): solo técnico y administrador, con diálogo de confirmación (el cliente no ve el botón y el ViewModel además valida el permiso).
- **Gestión de la solicitud** (3 campos nuevos + solución): **prioridad** (Baja, Media, Alta, Muy alta), **técnico asignado** y **estado** (Recibida, En revisión, En proceso, Resuelta, Cerrada, Cancelada). Solo el **administrador** asigna prioridad y técnico; el **estado** lo fija el administrador y también puede modificarlo el **técnico asignado**, que además escribe la **solución**. El cliente ve estos datos en modo lectura. Una solicitud Cerrada o Cancelada ya no se gestiona. Una solicitud nueva nace "Recibida", sin prioridad ni técnico.
- **Lista de solicitudes** (código, título, categoría, estado con color, prioridad y técnico), búsqueda y botón para crear/consultar/editar. Se actualiza sola al guardar (Room + Flow).
- **Formulario** con validaciones por campo (íconos + mensajes), código único `SOL-AAAAMMDD-####`, fecha y hora, estado inicial "Recibida".
- **Recursos nativos:**
  1. **Cámara**: fotografía de evidencia (o galería) vinculada a la solicitud.
  2. **Lector de códigos QR** (CameraX + ML Kit): lee el QR de una solicitud y abre su detalle. Cada solicitud guardada muestra su propio QR.
  3. **Notificación local** al registrar una solicitud.
  4. **Compartir por WhatsApp** un mensaje estructurado (código, título, categoría, enlace ficticio de seguimiento).
- **API REST (Retrofit + Gson):** pantalla "Posts (API REST)" del menú que consume `https://jsonplaceholder.typicode.com/posts` (integrada desde AppBrause) con estados de carga, error y reintento. Requiere internet.
- **Animaciones:** transiciones entre pantallas, aparición/ocultamiento animado de mensajes y evidencia, cambio animado de color del estado, flecha de listas desplegables, línea de escaneo del lector QR, reordenamiento animado de la lista.

## Arquitectura (MVVM)
`data/local` (Entity, Dao, AppDatabase) → `data/repository` → `ui/*/…ViewModel` (UiState + StateFlow) → pantallas Compose.
La lógica de validación está fuera de la UI (`model/SolicitudValidador`) y se prueba en `app/src/test`.

## Pruebas
- `app/src/test` (Kotest + MockK + JUnit Platform): `PostRepositoryTest`, `PostViewModelTest`, `PermisosTest` (incluye eliminar) y las pruebas JUnit4 previas (vía vintage engine).
- `app/src/androidTest`: `PostScreenTest` (Compose UI Test, requiere emulador/dispositivo).

## Cuentas de prueba
cliente@demo.test / cliente123 · admin@demo.test / admin123 · tecnico@demo.test / tecnico123 · tecnico2@demo.test / tecnico123

## Cómo ejecutar
1. Abrir la carpeta en Android Studio y sincronizar Gradle.
2. Ejecutar en un emulador o dispositivo Android 7.0+ (API 24+).
3. Para probar el QR: crear una solicitud, abrirla (muestra su QR) y escanearlo con otro dispositivo/imagen en la cámara virtual del emulador.
