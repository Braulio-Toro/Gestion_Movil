# Arquitectura MVVM – Gestión de Solicitudes

La app sigue el patrón **Model – View – ViewModel** con flujo de datos unidireccional,
tomando como referencia los proyectos *Practica* (UiState + ViewModel con `StateFlow`),
*AppBrause_Grupo4* (Repository + Retrofit + ViewModel) y *camara_prueba* (lector QR con CameraX/ML Kit
 y `QrRepository` que procesa el contenido leído).

```
 View (Compose)            ViewModel                 Model / Data
 ui/screens, ui/components viewmodel/                model/, data/
 ───────────────────────   ───────────────────────   ─────────────────────────────
 Dibuja el UiState    ◄──  StateFlow<UiState>   ◄──  Repository ─► Room / Retrofit /
 Envía eventos        ───► onXxxChange(), guardar()   archivos / notificaciones
```

## Estructura de paquetes

| Paquete | Rol | Contenido |
|---|---|---|
| `model/` | **Model** (dominio) | `Solicitud`, `QrResult`, `Usuario`, `Permisos`, `SolicitudValidador`, `Catalogos`, y los `*UiState` |
| `data/local` | Fuente local | Room: `AppDatabase`, `SolicitudDao`, `SolicitudEntity`, `SolicitudMapper` |
| `data/remote` | Fuente remota | Retrofit: `ApiService`, `RetrofitInstance` |
| `data/repository` | Repositorios | `SolicitudRepository`, `QrRepository`, `AuthRepository`, `SesionRepository`, `PostRepository`, `EvidenciaRepository`, `NotificacionRepository` |
| `viewmodel/` | **ViewModel** | `LoginViewModel`, `SesionViewModel`, `ListaSolicitudesViewModel`, `SolicitudFormViewModel`, `QrViewModel`, `PostViewModel`, `ViewModelFactory` |
| `ui/screens` | **View** | `LoginScreen`, `MenuScreen`, `ListaSolicitudesScreen`, `SolicitudFormScreen`, `QrScannerScreen`, `PostScreen` |
| `ui/components`, `ui/theme` | View reutilizable | Tarjeta, BarraApp, QrScanner, tema… |
| `navigation/` | Navegación | `AppNavigation` (rutas y grafo) |
| `di/` | Inyección manual | `AppContainer` (+ `GestionSolicitudesApp`) |
| `util/` | Utilidades de plataforma | Cámara, archivos, notificaciones, QR (las usan los repositorios o la View) |

## Qué cambió respecto a la versión anterior

| Antes | Ahora |
|---|---|
| `SolicitudViewModel` era un ViewModel único para lista **y** formulario, con `AndroidViewModel(app)` | `ListaSolicitudesViewModel` + `SolicitudFormViewModel`: uno por pantalla, sin `Application` ni `Context` |
| Los ViewModels creaban su repositorio (`AppDatabase.getDatabase(app)`, `AuthRepository()`) | Reciben sus repositorios por constructor; los crea `AppContainer` y los entrega `ViewModelFactory` |
| La sesión vivía en `LoginViewModel` y `AppNavigation` se la copiaba a los demás con `setUsuario()` | `SesionRepository` es la fuente única; los ViewModels la observan solos |
| El ViewModel llamaba a `NotificacionHelper` y `EvidenciaUtils` (Android) | `NotificacionRepository` y `EvidenciaRepository` encapsulan esa lógica |
| La pantalla del formulario creaba archivos, copiaba imágenes (`Dispatchers.IO`) y armaba el mensaje de WhatsApp | La pantalla solo lanza cámara/galería y delega en el ViewModel (`prepararFoto`, `onFotoResultado`, `onImagenGaleria`); el mensaje sale de `SolicitudFormUiState.mensajeSeguimiento` |
| La lista llamaba a `Permisos.puedeCrear` | `ListaSolicitudesUiState.puedeCrear` |
| El UI y el ViewModel usaban `SolicitudEntity` (Room) | Usan el modelo `Solicitud`; el repositorio convierte con `SolicitudMapper` |
| Los mensajes de confirmación viajaban por un `Channel` del ViewModel compartido | El formulario publica `mensajeConfirmacion` y la navegación se lo entrega a la lista por `savedStateHandle` |
| `iniciarSesion(onExito)` recibía un callback | `LoginUiState.loginExitoso` (evento que la pantalla consume) |
| `AppNavigation` instanciaba los ViewModels | Cada `composable` pide el suyo a la fábrica; vive y muere con su pantalla |

## Aporte de `camara_prueba`

| camara_prueba | Proyecto principal |
|---|---|
| `QrResult` + `QrRepository.processQrContent()` | `QrResult` (sellado: `Encontrada`, `NoValido`, `NoEncontrada`) + `QrRepository.procesarContenido()`: extrae el código, busca la solicitud y valida el acceso. `QrViewModel` solo traduce el resultado a `QrUiState` |
| `QrScanner` (CameraX + ML Kit, limpieza en `DisposableEffect`) | Mismo componente, en `ui/components/QrScanner.kt` |
| `LiveData` + `observeAsState` | Se mantiene `StateFlow` + `collectAsState`, como en *Practica* y *AppBrause* |
| La Activity observa el resultado y muestra `Toast` | No se replica: la pantalla reacciona al `QrUiState` y navega al detalle |

## Reglas que se respetan

1. La **View** solo observa `StateFlow<UiState>` y envía eventos; no accede a repositorios, Room ni archivos.
2. El **ViewModel** no importa nada de `android.*` (solo `androidx.lifecycle`), así que se prueba con JUnit/Kotest + MockK.
3. El **Repository** es la única puerta a los datos y devuelve modelos de dominio, nunca entidades de Room.
4. Las dependencias entran por constructor (`AppContainer` → `ViewModelFactory`).

## Pruebas

* Se conservan las pruebas de modelo y de `Post`, con los imports actualizados.
* Nuevas: `LoginViewModelTest`, `SolicitudFormViewModelTest`, `QrRepositoryTest` y `QrViewModelTest`, que antes no eran posibles
  porque el ViewModel dependía de `Application` y Room.

## Prioridad, técnico asignado, estado y solución

| Capa | Cambio |
|---|---|
| `model/` | `Solicitud` suma `prioridad`, `tecnicoAsignado` (correo) y `solucion`. `PrioridadSolicitud` y `EstadoSolicitud` (con `esFinal`) viven en `Catalogos.kt`. `Permisos` incorpora `puedeAsignar`, `puedeCambiarEstado` y `puedeEscribirSolucion`. `SolicitudFormUiState` expone esos permisos ya resueltos para que la pantalla solo decida qué campos habilitar |
| `data/local` | `SolicitudEntity` con las 3 columnas nuevas (opcionales) y `AppDatabase` v2 con `MIGRACION_1_2`, que conserva las solicitudes ya guardadas |
| `data/repository` | `SolicitudRepository.actualizar` recibe también prioridad, técnico, estado y solución. `AuthRepository.tecnicos` entrega los técnicos asignables |
| `viewmodel/` | `SolicitudFormViewModel` vuelve a comprobar los permisos contra lo guardado en Room y solo aplica los campos que el perfil puede cambiar (aunque la pantalla enviara otros). `ListaSolicitudesViewModel` entrega los nombres de los técnicos para la lista |
| `ui/` | Tarjeta "Gestión de la solicitud" en el formulario y `PrioridadChip` en la lista |

### Quién puede cambiar qué

| Campo | Cliente | Administrador | Técnico |
|---|---|---|---|
| Datos básicos (título, descripción…) | Solo mientras esté "Recibida" | Sí (si no está cerrada/cancelada) | No |
| Prioridad y técnico asignado | No | Sí | No |
| Estado | No | Sí | Solo si está asignado a él |
| Solución | No (la consulta) | No (la consulta) | Solo si está asignado a él |
