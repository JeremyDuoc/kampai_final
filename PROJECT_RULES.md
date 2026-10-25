# 📜 REGLAS DE DESARROLLO Y DIRECTRICES PARA IA (KAMPAI)

> **Instrucción para cualquier Asistente / Agente de IA:**
> Lee este archivo antes de realizar cualquier cambio, sugerencia o adición de código en el proyecto **Kampai**.
> Debes seguir rigurosamente estas directrices para mantener la integridad arquitectónica, estética y funcional de la aplicación.

---

## 1. Regla de Oro: Internacionalización Estricta (i18n)

* **PROHIBICIÓN ABSOLUTA DE TEXTO PLANO:** Nunca escribas strings hardcodeados en los archivos `.kt` (Composables, ViewModels, diálogos o modelos).
* **Uso en Jetpack Compose:** Todo texto de interfaz debe resolverse mediante `stringResource(R.string.nombre_recurso)` o `pluralStringResource(...)`.
* **Uso en ViewModels / Lógica de Estado:**
  * En los ViewModels y estados, almacena el **ID del recurso** (`@StringRes val textRes: Int`), nunca el texto resuelto. Deja que la UI en Compose resuelva el string con `stringResource()`.
  * Si un texto requiere argumentos dinámicos (como nombres de jugadores), utiliza recursos formateables (`%1$s`, `%2$d`) y pásalos a través de argumentos en la UI.
* **Sincronización Obligatoria en 3 Idiomas:**
  Al agregar una nueva clave de string o string-array, debes declararla simultáneamente en los tres archivos:
  1. `app/src/main/res/values/strings.xml` (Español 🇪🇸 - Base)
  2. `app/src/main/res/values-en/strings.xml` (Inglés 🇺🇸)
  3. `app/src/main/res/values-pt/strings.xml` (Portugués 🇧🇷)

---

## 2. Arquitectura, Estado y Concurrencia (Clean Architecture + MVVM)

* **Separación de Responsabilidades:**
  * Las funciones `@Composable` deben ser lo más *stateless* posible. Su única responsabilidad es renderizar la UI y reenviar eventos de usuario (clicks, drags) al ViewModel.
  * La lógica de negocio, temporizadores, barajado de cartas, contadores y llamadas a repositorios residen exclusivamente en los **ViewModels**.
* **Gestión de Estado Unidireccional (UDF):**
  * Declara estados mutables privados con `MutableStateFlow`:
    ```kotlin
    private val _uiState = MutableStateFlow<GameState>(GameState.Idle)
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()
    ```
  * En Compose, consume los flujos siempre con `by viewModel.uiState.collectAsState()`.
* **Inyección de Dependencias con Hilt:**
  * Todo ViewModel debe estar anotado con `@HiltViewModel` y `@Inject constructor(...)`.
  * Los repositorios y gestores compartidos deben registrarse como `@Singleton`.
  * En Compose, inyecta los ViewModels usando `hiltViewModel()` como valor por defecto del parámetro:
    ```kotlin
    @Composable
    fun MiPantallaScreen(
        viewModel: MiViewModel = hiltViewModel(),
        onBack: () -> Unit
    )
    ```
* **Corrutinas:**
  * Cualquier trabajo asíncrono o temporizador en el ViewModel debe lanzarse dentro de `viewModelScope.launch`.
  * Cancela trabajos previos (`Job?.cancel()`) al reiniciar temporizadores o eventos para evitar fugas de memoria o múltiples ticks simultáneos.

---

## 3. Navegación Segura (Navigation Compose)

Para evitar cierres inesperados o la temida "pantalla negra" por toques rápidos repetidos en Compose:
* **Navegar hacia una pantalla:**
  * ❌ **NUNCA:** `navController.navigate("ruta")`
  * ✅ **SIEMPRE:** `navController.navigateSafe("ruta")` (definido en `com.jeremy.kampai.utils.NavigationExtension.kt`).
* **Volver atrás (Backstack):**
  * ❌ **NUNCA:** `navController.popBackStack()` a secas.
  * ✅ **SIEMPRE:** `navController.popBackStackSafe()` (definido en `com.jeremy.kampai.utils.NavigationExtension.kt`).
* **Rutas Centralizadas:**
  * El grafo de navegación vive en `MainActivity.kt` dentro del `KampaiApp()`. Cada pantalla debe recibir callbacks simples (`onBack: () -> Unit`, `onGameSelected: (String) -> Unit`) sin acoplar el `NavController` directamente dentro de las pantallas secundarias.

---

## 4. Sistema de Diseño, Temas y UI

* **Paleta de Colores Corporativa:**
  * Utiliza los tokens de color oficiales definidos en `com.jeremy.kampai.ui.theme.Color.kt`:
    * Primario: `PrimaryViolet` (`#6A1B9A`)
    * Secundario: `SecondaryPink` (`#E100FF`)
    * Acentos: `AccentAmber` (`#F59E0B`), `AccentCyan` (`#06B6D4`), `AccentRed` (`#EF4444`)
    * Fondos / Superficies: `BackgroundDark` (`#121212`), `SurfaceDark` (`#1E1E1E`)
  * No crees códigos hexadecimales arbitrarios dispersos por las vistas.
* **Componentes Reutilizables:**
  * Antes de crear contenedores o tarjetas desde cero, verifica `com.jeremy.kampai.ui.theme.Components.kt`:
    * `KampaiCard`: Tarjeta con efecto *glassmorphism* translúcido y borde brillante.
    * `PartyBackground`: Fondo con orbes animados de fiesta.
    * `GameScaffold`: Estructura estándar con cabecera y botón atrás.
* **Diseño Responsivo:**
  * Calcula tamaños proporcionales o con límites `coerceIn(...)` usando `LocalConfiguration.current.screenWidthDp.dp` para garantizar visualización óptima en pantallas pequeñas, teléfonos grandes y tablets.

---

## 5. Integración con el Sistema de Jugadores (Party Manager)

* **Respetar el Modelo `PlayerModel`:**
  * Al interactuar con jugadores, utiliza siempre la entidad `PlayerModel` (`id`, `name`, `gender`, `colorIndex`, `avatarEmoji`).
* **Validación de Fallback:**
  * Nunca asumas que la lista de jugadores tiene un tamaño mínimo sin comprobarlo (`if (players.size < 2) return`).
  * Si un juego requiere jugadores pero la lista está vacía, muestra un estado vacío instructivo o adapta la dinámica para jugar en grupo anónimo.
* **Diversidad de Género:**
  * Soporta siempre las tres opciones de `Gender`: `MALE`, `FEMALE` y `OTHER`. Al emparejar participantes (como en duelos RPS), provee lógica de respaldo si no hay participantes de ambos géneros.

---

## 6. Multimedia, Audio y Rendimiento

* **Efectos de Sonido:**
  * Usa exclusivamente `SoundManager` inyectado. No crees instancias de `MediaPlayer` locales sin llamar a `.release()`.
* **Video y Animaciones Pesadas:**
  * En componentes con `ExoPlayer` (como `WarmUpScreen`), asegura la liberación del reproductor en un `DisposableEffect(Unit) { onDispose { exoPlayer.release() } }`.
  * Utiliza `rememberInfiniteTransition` y `graphicsLayer` para transformaciones de escala, rotación o alfa, aprovechando la aceleración por hardware.

---

## 7. Monetización y Publicidad (AdMob)

* No modifiques los identificadores de anuncios ni la lógica de `RewardedAdManager` a menos que sea explícitamente solicitado.
* Los anuncios intersticiales están regulados por frecuencia (por ejemplo, cada 3 transiciones en `MainActivity.showInterstitial`). Respeta esta cadencia para no degradar la experiencia de usuario.
