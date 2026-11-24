# MPAZ PRO 3° — Aplicación móvil de apoyo pedagógico

Aplicación Android para 3° básico, desarrollada con Kotlin, Jetpack Compose, Material 3, MVVM, Room y Retrofit. Incluye perfiles para Jefatura UTP, docente y estudiante, con funciones de revisión de contenidos, planificación, actividades, evaluación y seguimiento.

## Alcance y pauta de evaluación

El docente confirmó que el backend se entrega aparte y que el equipo no necesita crear microservicios Spring Boot. Para la defensa, la app puede demostrar sus funcionalidades con el modo local y Room; el docente acepta esa persistencia como equivalente a conectarse a una API REST. Por eso, el flujo recomendado de demostración usa el modo local y no depende del backend.

| Aspecto | Implementación de esta entrega |
|---|---|
| Aplicación | Kotlin, Jetpack Compose, Material 3 y arquitectura MVVM; perfiles UTP, docente y estudiante. |
| Persistencia demostrable | Room local, con las operaciones y reglas de negocio del proyecto. El docente acepta este modo para la evaluación. |
| Backend del curso | Lo proporciona el docente y se ejecuta por separado; no forma parte del código que desarrolla este equipo. |
| Pruebas | Pruebas JVM de las reglas de dominio y del flujo local; la sección 8 documenta cómo ejecutarlas y consultar JaCoCo. |
| APK firmado | Generar `release` con la llave del equipo y entregar el APK y `.jks` por el canal indicado por el docente. No publiques la llave ni sus contraseñas en GitHub público. |
| Colaboración | Mostrar en GitHub las ramas y commits de `alan`, `javiera`, `matias` y `sigrid`; completar los nombres y aportes reales del equipo en la exposición. |

El contrato Retrofit de `ApiService.kt` documenta cómo se comunicaría la app con el backend entregado por el docente. Para la demostración aceptada no es necesario afirmar que ese servicio corre dentro de este repositorio.

### Integrantes y colaboración

Ramas de integrantes visibles en este repositorio: `alan`, `javiera`, `matias` y `sigrid`. En la defensa, cada integrante debe identificar su aporte técnico y mostrar sus commits.

### Contrato de rutas declarado por Retrofit

Estas rutas están declaradas en `app/src/main/java/com/mpazpro3/app/data/remote/ApiService.kt` como contrato Retrofit para el backend que entrega el docente. La defensa de esta app puede realizarse en modo local, aceptado por el docente; Room provee la persistencia usada en esa demostración.

| Método | Ruta | Uso |
|---|---|---|
| GET | `/health` | Estado del servicio |
| POST | `/auth/login`, `/auth/logout` | Inicio y cierre de sesión |
| GET | `/contenidos`, `/contenidos/{id}` | Listar y consultar contenidos |
| POST | `/contenidos/{id}/aprobar`, `/contenidos/{id}/rechazar` | Aprobar o rechazar contenido |
| GET | `/recursos`, `/actividades` | Consultar recursos y actividades |
| GET | `/asignaciones`; PUT `/cursos/{curso}/asignaciones` | Consultar y modificar asignaciones |
| GET | `/estudiantes`, `/seguimientos` | Consultar estudiantes y avance |
| POST | `/estudiantes/{estudianteId}/contenidos/{contenidoId}/material-revisado` | Registrar lectura de material |
| POST | `/estudiantes/{estudianteId}/actividades/{actividadId}/respuestas` | Registrar respuesta a desafío |
| POST | `/estudiantes/{estudianteId}/contenidos/{contenidoId}/evaluacion` | Enviar evaluación |
| GET | `/cursos/{curso}/resultados.csv` | Descargar resultados CSV |
| GET | `/informes`; POST `/estudiantes/{estudianteId}/informe` | Consultar y generar informe |
| POST | `/informes/{id}/validar` | Validar informe pedagógico |

`ApiService` documenta la comunicación Retrofit con el backend proporcionado por el docente. En el alcance confirmado para esta evaluación, la persistencia local Room es suficiente para demostrar la app.

## 1. Descripción del caso

Un colegio necesita que la Jefatura UTP autorice los contenidos, que el docente los planifique para su curso y
que los estudiantes los trabajen con material, desafíos y una evaluación final. Hay tres perfiles:

| Perfil | Qué hace |
|---|---|
| Jefatura UTP | Revisa contenidos curriculares, los filtra por asignatura y estado, y los aprueba o rechaza (con motivo). |
| Docente | Ve solo los contenidos aprobados, los asigna a su curso, define el máximo de intentos de la evaluación (1 a 10), revisa resultados, descarga un CSV y genera/valida el informe pedagógico de cada estudiante. |
| Estudiante | Ve lo que se le asignó, estudia el material (imagen, texto y audio opcional), resuelve desafíos de dificultad progresiva, rinde la evaluación final y revisa sus propias respuestas. |

Reglas de negocio principales:

- **RN-01**: cada perfil accede solo a lo suyo; el estudiante nunca ve el informe. Se controla en la navegación y de nuevo en el `ViewModel`.
- **RN-02**: solo los contenidos aprobados por UTP se pueden asignar y mostrar. Si hay uno no autorizado en un lote, no se asigna ninguno.
- **RN-03**: la evaluación final respeta estrictamente el máximo de intentos del docente.
- **RN-04**: máximo una unidad aprobada por asignatura.
- **RN-05 / RN-06**: el informe nace en borrador, el docente lo valida y, una vez validado, no se regenera. El texto solo describe el desempeño: no diagnostica ni aplica consecuencias académicas.

Todos los datos de ejemplo son ficticios.

## 2. Perfiles y usuarios de prueba

| Perfil | Usuario | Clave |
|---|---|---|
| Jefatura UTP | `utp@mpazpro3.cl` | `utp123` |
| Docente (3° básico A) | `docente.3basicoA@mpazpro3.cl` | `docente123` |
| Estudiante | `est001`, `est002`, `est003`, `est004` | `1234` |

El usuario no distingue mayúsculas; la clave sí. Las claves **no** se guardan en texto plano: se almacena su SHA-256
con una sal fija de demostración (`ReglasAcceso.hashClave`). Un sistema real debe usar una sal por usuario y un
algoritmo de derivación de claves (bcrypt, Argon2).

Datos de ejemplo al abrir por primera vez: Matemática aprobada y asignada al curso; Lenguaje y Ciencias pendientes;
Historia rechazada con observación. `est002` ya terminó Matemática, `est003` va a la mitad y `est001` / `est004` no
han empezado.

## 3. Requerimientos funcionales

| RF | Descripción | Estado local | Dónde |
|---|---|---|---|
| RF-00 | Inicio de sesión por perfil con validación por campo | Implementado | `ui/screen/LoginScreen.kt`, `domain/ReglasAcceso.kt`, `viewmodel/MpazProViewModel.login` |
| RF-01 | UTP aprueba o rechaza contenidos (motivo obligatorio al rechazar, confirmación, RN-04) | Implementado | `ui/screen/utp/DetalleContenidoUtpScreen.kt`, `domain/ReglasContenido.kt` |
| RF-02 | Listado y filtros por asignatura y estado, pendientes primero | Implementado | `ui/screen/utp/ContenidosUtpScreen.kt`, `ReglasContenido.filtrar` |
| RF-03 | El docente ve solo contenidos autorizados | Implementado | `ui/screen/docente/PlanificacionDocenteScreen.kt`, `ReglasContenido.disponiblesParaAsignar` |
| RF-04 | Asignar contenidos al curso (todo o nada) | Implementado | `PlanificacionDocenteScreen`, `ReglasPlanificacion`, `MpazProRepositorio.asignarAlCurso` |
| RF-05 | Máximo de intentos de la evaluación (1 a 10) | Implementado | `PlanificacionDocenteScreen`, `ReglasPlanificacion.errorMaxIntentos` |
| RF-06 | Resultados del curso, avance por estudiante y descarga CSV | Implementado | `ui/screen/docente/ResultadosCursoScreen.kt`, `domain/ExportadorResultados.kt`, `ReglasProgreso` |
| RF-07 | El estudiante ve sus actividades y el material (imagen, texto, audio) | Implementado | `ui/screen/estudiante/InicioEstudianteScreen.kt`, `MaterialScreen.kt`, `ui/components/ReproductorAudio.kt` |
| RF-08 | Desafíos de dificultad progresiva con retroalimentación inmediata | Implementado | `ui/screen/estudiante/DesafiosScreen.kt`, `domain/ReglasDesafio.kt` |
| RF-09 | Evaluación final con intentos limitados | Implementado | `ui/screen/estudiante/EvaluacionFinalScreen.kt`, `domain/ReglasEvaluacion.kt` |
| RF-10 | El estudiante revisa sus respuestas (correcta / incorrecta) | Implementado | `ui/screen/estudiante/RevisionRespuestasScreen.kt`, `ReglasProgreso.respuestasVigentes` |
| RF-11 | Seguimiento de avance (material, desafíos, evaluación) | Implementado | `ReglasProgreso`, `MpazProRepositorio.confirmarMaterialRevisado` |
| RF-12 | Generar el informe pedagógico en borrador | Implementado | `ui/screen/docente/InformeDocenteScreen.kt`, `domain/GeneradorInforme.kt` |
| RF-13 | Validar el informe (un informe validado no se regenera) | Implementado | `InformeDocenteScreen`, `MpazProRepositorio.validarInforme` |
| RF-14 | El informe describe, no diagnostica, y lleva el aviso correspondiente | Implementado | `GeneradorInforme`, `InformeDocenteScreen` |

Fórmula del avance de un contenido:

```
avance = (material revisado + desafíos distintos respondidos + evaluación rendida) / (1 + n.º de desafíos + 1)
```

Repetir un desafío no suma de nuevo. El avance general es el promedio de los contenidos asignados al curso.
Las respuestas "vigentes" son la última de cada desafío y las del último intento de la evaluación.

### Recursos nativos del dispositivo

- **Selector de archivos del sistema (SAF)**: guardar el CSV de resultados donde el docente elija (`CreateDocument`), más la hoja de compartir del sistema.
- **Vibración**: refuerzo al responder un desafío y al confirmar acciones (permiso `VIBRATE`, que no pregunta al usuario).
- **Audio** (`MediaPlayer`): reproductor del material de apoyo (no se reproduce solo).

No se usan cámara, GPS, micrófono ni biometría.

### Formato del CSV

Separador `;`, saltos de línea CRLF, una fila por estudiante y contenido asignado. Columnas: `Curso;Estudiante ID;
Estudiante;Asignatura;Contenido;Avance (%);Correctas;Incorrectas;Intentos usados;Máximo de intentos;Estado`.
Los campos que empiezan con `=`, `+`, `-` o `@` se neutralizan para evitar fórmulas en planillas.

## 4. Arquitectura

MVVM con un único `ViewModel`, una única base de datos y una única navegación.

```
com.mpazpro3.app
├── model/        Datos puros del dominio (Contenido, Actividad, Asignacion, SeguimientoAvance, InformeDocente, Resultado…)
├── domain/       Reglas de negocio PURAS y probadas en JVM (ReglasAcceso, ReglasContenido, ReglasPlanificacion,
│                 ReglasDesafio, ReglasEvaluacion, ReglasProgreso, GeneradorInforme, ExportadorResultados)
├── data/         DatosIniciales (semilla), MpazProRepositorio (lecturas observables de Room), RepositorioLocal (modo local:
│   │             solo Room), SelectorFuente + PreferenciaModo (eligen el modo en cada operación), AppContenedor (arma las
│   │             piezas), CacheLocal (contrato de la copia local), RepositorioSincronizado (modo servidor: API + Room)
│   │             <-- TODO(E3-6..E3-9)
│   ├── local/    Room: entidades, DAOs, AppDatabase (dos archivos: base local y copia del servidor), mapeos entidad <-> modelo,
│   │             AlmacenRoom (base del modo local) y CacheRoom (copia del servidor)  <-- TODO(E3-6, E3-8)
│   └── remote/   Retrofit: Dtos y ApiService (completos: son el contrato), ApiClient, ErroresApi, Mapeadores,
│                 FuenteRemota, UrlServidor, ConfiguracionServidor, ProbarServidor  <-- TODO(E3-1 a E3-5, E3-10)
├── viewmodel/    MpazProViewModel: estado (StateFlow), permisos por perfil y acciones; usa una sola fuente (SelectorFuente)
├── navigation/   AppNavigation (NavHost, rutas, control de acceso por perfil y aviso de conexión)
└── ui/
    ├── theme/        Tema Material 3 (paleta del colegio)
    ├── components/   BarraSuperior, SelectorDesplegable, MensajeResultado, ReproductorAudio, AvisoConexion, ServidorDialog…
    └── screen/       login, utp, docente, estudiante
```

Decisiones de diseño:

- Las reglas viven en `domain/` sin dependencias de Android, por eso se prueban con JUnit en la JVM.
- Las acciones que modifican datos son `suspend`, se ejecutan fuera del hilo principal y devuelven un `Resultado(exito, mensaje)` que las pantallas muestran con `MensajeResultado`.
- Las pantallas dibujan lo que hay en Room. Con la API, cada sincronización actualiza Room y la pantalla se refresca sola.
- En el modo local, las reglas de negocio se ejecutan en la app y los cambios se guardan en Room. El backend proporcionado por el docente es una alternativa de integración; no hay que construirlo para esta entrega.
- La base de datos está en la versión 1 y no usa migración destructiva: si cambias el esquema, sube la versión y escribe una `Migration` (en desarrollo basta desinstalar la app).

## 5. Modos local y servidor

La app permite elegir el modo con el interruptor **Modo local (sin servidor)** del diálogo **Servidor**. El modo local usa Room y los datos de ejemplo, y es el modo aceptado por el docente para demostrar la aplicación. El modo servidor es una alternativa para conectarse al backend que entrega el docente; no es necesario para la evaluación local.

| Modo | Qué hace la app |
|---|---|
| **Modo local** (valor inicial con `USAR_API=false`) | Funciona completa y sin servidor, solo con Room: ingreso con el hash local, y todas las acciones con escritura (aprobar/rechazar, asignar, material, desafíos, evaluación con intentos, CSV, informes) con las mismas funciones puras de `domain/` y los mismos mensajes del servidor. No se ejecuta nada de la capa remota. Es el respaldo para demostrar sin servidor. |
| **Modo servidor** (valor inicial con `USAR_API=true`) | Alternativa de integración con el backend del curso. Algunas operaciones siguen marcadas `TODO(E3-n)`; no se requieren para presentar el modo local aceptado por el docente. |

**`USAR_API` solo define el valor INICIAL del interruptor** (`gradle.properties` → `BuildConfig.USAR_API`; también `-PUSAR_API=true` al compilar).
En cuanto eliges un modo en el diálogo Servidor, manda lo guardado en el dispositivo y `USAR_API` ya no influye (para volver al valor
inicial hay que borrar los datos de la app o reinstalarla).

- Si **Probar conexión** falla, el diálogo ofrece el botón **Usar modo local**.
- **Datos independientes**: cada modo tiene su propia base Room (`mpazpro3_local.db` para el modo local y `mpazpro3.db` para la copia del servidor).
  **No hay sincronización**: lo que se crea en modo local no se envía al servidor al volver al modo servidor, ni lo del servidor se mezcla con lo local.
- **Restablecer datos locales** (en el diálogo, con el modo local activo y con confirmación) borra lo creado en modo local y vuelve a cargar los datos de ejemplo.
- Cambiar de modo con una sesión abierta cierra la sesión.
- Si actualizas desde una versión anterior del proyecto, desinstala la app: la base local ahora es otro archivo.

Los `TODO(E3-*)` corresponden a la integración opcional del modo servidor. Para la defensa, selecciona el modo local y demuestra las operaciones guardadas en Room; el flujo no depende de completar esos TODO.

## 6. Integración opcional con el backend del curso

El docente proporciona el backend por separado. `Dtos.kt` y `ApiService.kt` describen el contrato Retrofit disponible en la app. Los `TODO(E3-n)` de esta sección pertenecen al modo servidor opcional; no impiden la demostración completa en modo local con Room.

| Código | Archivo | Qué se hace |
|---|---|---|
| E3-1 | `data/remote/UrlServidor.kt` | Validar y normalizar la URL del servidor (esquema, host, puerto, http solo local). |
| E3-2 | `data/remote/ApiClient.kt` | Interceptor del token, `OkHttpClient` con timeouts y `Retrofit` con Gson. |
| E3-3 | `data/remote/ErroresApi.kt` | Traducir códigos HTTP, cuerpo `{error, campo}`, timeout, sin red y JSON mal formado a mensajes en español. |
| E3-4 | `data/remote/Mapeadores.kt` | Mapeadores DTO ↔ modelo (hay dos de ejemplo ya resueltos). |
| E3-5 | `data/remote/FuenteRemota.kt` | Fuente de datos remota: ejecutar llamadas con red y sesión, y descargar todo lo del perfil. |
| E3-6 | `data/local/CacheRoom.kt`, `data/RepositorioSincronizado.kt` | Copia en Room (reemplazo atómico, UPDATE sin REPLACE) y `sincronizar`: API → Room → pantalla. |
| E3-7 | `data/RepositorioSincronizado.kt` | Escrituras con la API primero: aprobar, rechazar, asignar, material, desafío, evaluación, CSV e informes. |
| E3-8 | `CacheRoom.kt`, `RepositorioSincronizado.kt` | Comportamiento sin red: ingreso con datos guardados y (avanzado) cola de pendientes. |
| E3-9 | `data/RepositorioSincronizado.kt` | Login contra `POST /auth/login` y cierre de sesión. |
| E3-10 | `ConfiguracionServidor.kt`, `ProbarServidor.kt` | Guardar/restablecer la URL y "Probar conexión" contra `GET /health`. |
| E3-T1 a T6 | `src/test/...`, `src/androidTest/...` | Pruebas: API con MockWebServer, errores, mapeadores, URL, repositorio y una prueba de interfaz. |

Orden recomendado: E3-1 → E3-2 → E3-3 → E3-4 → E3-5 → E3-9 → E3-6 → E3-7 → E3-8 → E3-10, probando cada parte con su prueba.

## 7. Cómo ejecutar

Requisitos: Android Studio (Koala o posterior), JDK 17 y Android SDK 35.

En Android Studio, configura **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK** en JDK 17. Esta plantilla usa AGP 8.5.2; si Android Studio selecciona JDK 25, Gradle puede fallar antes de compilar. Instala JDK 17 si no aparece en la lista.

1. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle.
2. Elegir un emulador o dispositivo (Android 7.0 / API 24 o superior).
3. Ejecutar la configuración `app`, o por consola: `./gradlew assembleDebug` (en Windows: `gradlew.bat assembleDebug`).

**Modo local** (valor inicial con `USAR_API=false`): los datos de ejemplo se cargan solos la primera vez que se abre la aplicación y no
se necesita ningún servidor.

**Modo local (recomendado para la defensa):** no necesita backend; el docente confirmó que Room local es aceptado para esta evaluación. El backend del curso se entrega aparte. Si se demuestra el modo servidor, usa la URL que indique el docente y ten presente que algunas operaciones remotas son opcionales y conservan `TODO(E3-*)`.

1. Configurar la URL del servidor en el diálogo **Servidor** o mediante `API_BASE_URL`.
2. Elegir la URL que corresponda:

| Dónde corre la app | URL en la app |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:3000/` (valor por defecto de `API_BASE_URL`; requiere backend compatible) |
| Teléfono físico en la misma red Wi-Fi que el PC | `http://<IP del PC>:3000/` (ejemplo; requiere backend compatible) |
| Teléfono fuera de la red del PC | URL HTTPS pública del backend desplegado |

   La URL inicial sale de `API_BASE_URL` en `gradle.properties` (o `-PAPI_BASE_URL=...` al compilar). Para cambiarla sin
   recompilar, en el login toca **Servidor**, escribe la URL, **Probar conexión** y **Guardar**. Las direcciones
   `http://` solo se aceptan hacia el emulador, `localhost` y redes locales; cualquier servidor público debe usar `https://`.
3. Ingresar con los usuarios de la sección 2.

La ejecución local no requiere conexión de red. Si se usa el backend del curso, confirma con el docente la URL y que el servicio esté accesible desde el emulador o dispositivo.

## 8. Cómo correr las pruebas

Pruebas unitarias JVM (JUnit 4, sin emulador) en `app/src/test`:

```
./gradlew testDebugUnitTest
```

- **Ya funcionan**: reglas de negocio de `domain/` (acceso, contenidos, planificación, desafíos, evaluación, progreso, informe, CSV), coherencia de los datos de ejemplo, el contrato de los DTO (`ContratoDtoTest`) y la regla de hosts permitidos para `http` (`HostPermiteHttpTest`).
- **Modo local (ya funcionan, 44 casos activos)**: `SelectorFuenteTest` (el selector elige la fuente según la preferencia y la relee en cada operación, también al cambiar de modo en caliente), `RepositorioLocalTest` (ingreso con hash local y cada acción con escritura con sus reglas: casos correctos y errores de RN-02, RN-03, RN-04, informe validado y CSV) y `ReglasModoLocalTest`. Usan una base en memoria (`AlmacenLocalMemoria` en `data/FakesLocal.kt`); no los marques con `@Ignore`.
- **Cobertura de negocio**: `./gradlew jacocoDebugUnitTestCoverageVerification` exige al menos 80% de cobertura de líneas en `domain/`; `./gradlew jacocoDebugUnitTestReport` genera el reporte HTML en `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html`. La cobertura medida para `domain/` es 99,5%; no representa cobertura del proyecto completo.
- **Modo servidor opcional**: las pruebas `ApiServiceMockWebServerTest`, `ErroresApiTest`, `MapeadoresTest`, `UrlServidorTest` y `RepositorioSincronizadoTest` cubren la integración remota y permanecen ignoradas junto con los TODO correspondientes. No forman parte del flujo local aceptado por el docente.
- **Prueba de interfaz** (`app/src/androidTest/.../LoginUiTest.kt`, 3 casos): valida campos vacíos, credenciales incorrectas y el ingreso del estudiante. Los tres casos pasaron en un dispositivo Android 17/API 37 con `./gradlew connectedDebugAndroidTest`. Espresso 3.7.0 es necesario para esa versión. La variante debug usa el identificador `com.mpazpro3.app.debug` para no reemplazar una app release instalada y restaura la preferencia de modo al terminar.

El reporte queda en `app/build/reports/tests/testDebugUnitTest/index.html`. Este repositorio no contiene pruebas de microservicios.

## 9. Cómo generar el APK firmado

El APK firmado requerido para la entrega está en `entrega/MPAZPRO3-1.0.1-release.apk`. Su certificado público, huellas SHA-256 y verificación de firma V1/V2 están en `entrega/VALIDACION.md`. El `.jks` y sus contraseñas se mantienen fuera del repositorio público y se entregan al docente por el canal privado indicado. Para generar o reemplazar la llave y el APK:

1. Crear una llave propia (una sola vez, exclusiva para esta entrega):

```
keytool -genkeypair -v -keystore mi-llave.jks -alias mi-alias -keyalg RSA -keysize 2048 -validity 10000
```

2. Copiar `keystore.properties.example` como `keystore.properties` (queda fuera de Git) y completar los cuatro datos
   `MPAZPRO_STORE_FILE`, `MPAZPRO_STORE_PASSWORD`, `MPAZPRO_KEY_ALIAS` y `MPAZPRO_KEY_PASSWORD`. También se leen de
   `~/.gradle/gradle.properties` o de variables de entorno con esos mismos nombres.
3. Generar el APK: `./gradlew assembleRelease`. Queda en `app/build/outputs/apk/release/app-release.apk`, firmado con
   esquemas V1 y V2 (están activados en `app/build.gradle.kts`).

`.gitignore` excluye los archivos locales de Gradle y Android Studio. En un repositorio público, no subas el `.jks`, `keystore.properties`, contraseñas ni otros secretos de firma. Entrega la llave por el canal privado que indique el docente. En el README puedes incluir una captura de la firma y del APK, ocultando datos sensibles.
Versión actual: `versionName 1.0.1`, `versionCode 2`. El build release tiene la minificación desactivada. El APK firmado y el certificado público se encuentran en `entrega/`; su SHA-256 y la verificación V1/V2 se anotan en `entrega/VALIDACION.md`. La llave `.jks` y `keystore.properties` se conservan localmente y se entregan al docente por el canal privado que indique.

## 13. Validación de la entrega

- **Inicio y entorno**: ejecutar la app en emulador/dispositivo con Android Studio, JDK 17 y SDK 35. El modo local no requiere backend.
- **Arquitectura**: explicar MVVM, `model/`, `domain/`, `data/`, `viewmodel/`, `navigation/` y `ui/` según la sección 4. Esta base contiene la app Android; no contiene microservicios Spring Boot.
- **Backend del curso**: lo proporciona el docente y no es necesario construirlo para esta entrega. Para la demostración aceptada, usa el modo local con Room.
- **Persistencia disponible**: demostrar el flujo local con Room y las operaciones de UTP, docente y estudiante.
- **Pruebas**: ejecutar `gradlew.bat testDebugUnitTest jacocoDebugUnitTestCoverageVerification` en Windows y mostrar el reporte de JaCoCo. Para la interfaz, ejecutar `gradlew.bat connectedDebugAndroidTest` con emulador conectado.
- **APK firmado**: mostrar `entrega/MPAZPRO3-1.0.1-release.apk`, verificarlo con `apksigner verify --verbose --min-sdk-version 18 --print-certs` y explicar la firma V1/V2. Mantener la llave privada fuera del repositorio público.
- **Colaboración y modificación en vivo**: mostrar las ramas/commits del repositorio y explicar los cambios técnicos de esta entrega; la prueba de interfaz y la regla de cobertura sirven para explicar el aporte de pruebas.
- **API REST**: el docente acepta el modo local con Room como equivalente para demostrar persistencia y funcionamiento de la app; presenta ese flujo y describe Retrofit como integración disponible con el backend del curso.

## 10. Material didáctico y orígenes (RNF-05)

| Recurso | Origen |
|---|---|
| Ilustraciones vectoriales (`res/drawable`) | Propias del proyecto (XML vectorial). |
| Logo y paleta | Identidad del colegio entregada con la plantilla del caso. |
| Textos de contenidos, desafíos y evaluaciones | Redactados para el proyecto; sujetos a revisión de la Jefatura UTP. |
| Audio de apoyo `res/raw/centenas_explicacion.wav` | **Señal de ejemplo generada por código** (tonos que cuentan unidades, decenas y centenas), sin voz ni material de terceros. Es un recurso de reemplazo: para usar tu propia explicación, grábala (30 a 60 segundos, tu propia voz o con permiso de quien hable) y reemplaza el archivo conservando el nombre base `centenas_explicacion` (`.wav`, `.mp3`, `.ogg` o `.m4a`). En `res/raw` debe quedar un solo archivo con ese nombre; la referencia está en `ui/components/RecursosMultimedia.kt` y no hay que cambiarla. |

## 11. Tarea pendiente: validar los códigos OA

Los Objetivos de Aprendizaje figuran como `OA (por confirmar)` en los datos de ejemplo. Verifica el código oficial de cada contenido en el Currículum Nacional y actualiza los valores en `app/src/main/java/com/mpazpro3/app/data/DatosIniciales.kt`. Cuando exista el backend, sincroniza también su semilla de datos.

## 12. Guion de preparación para la defensa

1. Abrir y ejecutar la app en un emulador o dispositivo siguiendo la sección 7; seleccionar el modo local.
2. Demostrar los flujos de UTP, docente y estudiante y explicar cómo Room conserva los datos.
3. Ejecutar las pruebas JVM y mostrar el reporte JaCoCo indicado en la sección 8.
4. Mostrar el APK firmado, explicar cómo se generó y entregar el `.jks` mediante el canal que indique el docente; no publiques la llave en GitHub.
5. Mostrar las ramas y commits de cada integrante y explicar el aporte técnico de cada persona.
6. Subir el mismo código fuente al AVA grupal y completar el enlace individual solicitado.

El backend se proporciona por el docente y no es necesario implementarlo para esta demostración local.
