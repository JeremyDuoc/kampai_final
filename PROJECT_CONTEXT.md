# 🧠 CONTEXTO TÉCNICO Y MAPA DE CÓDIGO (KAMPAI)

> **Resumen Ejecutivo para Asistentes de IA:**
> Este documento contiene el contexto completo de arquitectura, modelos, repositorios, pantallas y recursos de **Kampai**.
> Consúltalo para entender la estructura completa del proyecto sin necesidad de inspeccionar cada archivo por separado.

---

## 1. Identificación y Especificaciones Técnicas

* **Nombre de la App:** Kampai (Drinking Game Suite para Android)
* **Package Name:** `com.jeremy.kampai`
* **Versión:** `1.7` (VersionCode: `8`)
* **Target / Compile SDK:** API 35 (Android 15) | **Min SDK:** API 24 (Android 7.0 Nougat)
* **Lenguaje:** Kotlin 1.9+ | **JVM Target:** 17
* **Framework UI:** Jetpack Compose (BOM 2023.08.00) con Material Design 3
* **Inyección de Dependencias:** Dagger Hilt 2.48
* **Multimedia:** Media3 ExoPlayer 1.2.0 (Video en bucle) + MediaPlayer (SFX)
* **Persistencia:** SharedPreferences + Gson 2.10.1 (Jugadores) | DataStore Preferences (Ajustes y Tutorial)
* **Monetización:** Google Mobile Ads (AdMob SDK 23.0.0)

---

## 2. Árbol de Archivos y Responsabilidades

```
app/src/main/java/com/jeremy/kampai/
├── KampaiApplication.kt               # @HiltAndroidApp: Inicialización de Hilt
├── MainActivity.kt                    # @AndroidEntryPoint: NavHost, transiciones y AdMob
├── data/
│   ├── GameRepository.kt              # Catálogo de juegos: más jugados, nuevos, clásicos
│   └── PartyRepository.kt             # @Singleton: Persistencia de jugadores en SharedPreferences + Gson
├── domain/
│   └── models/
│       ├── GameModel.kt               # Entidad GameModel + Enum GameIntensity (Suave, Intermedio, Fuerte, Extremo)
│       └── PlayerModel.kt             # Entidad PlayerModel, Enum Gender, Catálogo AvatarEmojis
├── ui/
│   ├── RewardedAdManager.kt           # Gestor de anuncios de video recompensados
│   └── theme/
│       ├── Color.kt                   # Paleta oficial: PrimaryViolet, SecondaryPink, Acentos y Gradientes
│       ├── Theme.kt                   # KampaiTheme: LightColorScheme y DarkColorScheme
│       ├── ThemeManager.kt            # @Singleton: DataStore para modo oscuro/claro
│       ├── Type.kt                    # Tipografías oficiales
│       ├── Components.kt              # Componentes UI reutilizables (KampaiCard, PartyBackground, GameScaffold)
│       ├── SplashScreen.kt            # Pantalla de carga con animación de logo y barra de progreso
│       ├── home/
│       │   ├── HomeScreen.kt          # Pantalla principal estilo Netflix (carrusel hero, estantes, aurora background)
│       │   ├── HomeViewModel.kt       # ViewModel del Home (carga de juegos y disclaimer legal)
│       │   ├── ClassicsScreen.kt      # Pantalla alternativa de clásicos
│       │   ├── ClassicsViewModel.kt   # ViewModel de clásicos
│       │   ├── SettingsScreen.kt      # Pantalla de ajustes (idioma, sonido, soporte, feedback)
│       │   └── SettingsViewModel.kt   # ViewModel de ajustes (LocaleListCompat, correo, DataStore)
│       ├── partymanager/
│       │   ├── PartyManagerScreen.kt  # Gestión visual de jugadores (alta, baja, selector de avatares)
│       │   └── PartyManagerViewModel.kt# ViewModel del Party Manager
│       ├── warmup/                    # [MODO INSIGNIA] PartyMix
│       │   ├── WarmUpScreen.kt        # Interfaz con ExoPlayer de fondo, selector de rondas y eventos
│       │   ├── WarmUpViewModel.kt     # Lógica de 11 eventos, contador de tragos y sentencia final
│       │   └── WarmupPreferencesManager.kt # DataStore para estado de tutorial
│       ├── culture/                   # Cultura Chupística (Clásico y Bomba)
│       │   ├── CultureSelectionScreen.kt # Selector: Modo Clásico vs Modo Bomba
│       │   ├── CultureGameScreen.kt   # Modo clásico (113 categorías)
│       │   └── CultureViewModel.kt    # ViewModel de cultura clásica
│       ├── bomb/                      # Modo Bomba (Fusible invisible con audio tic-tac y explosión)
│       │   ├── BombGameScreen.kt      # Pantalla interactiva con gráficos y luces de alerta
│       │   └── BombViewModel.kt       # Temporizador aleatorio 30-60s y aceleración de sonido
│       ├── kingscup/                  # King's Cup (Baraja inglesa de 52 cartas)
│       │   ├── KingsCupGameScreen.kt  # Visualización de cartas, física de extracción y copa central
│       │   └── KingsCupViewModel.kt   # Mazo procedural y reglas universales
│       ├── impostor/                  # El Impostor (Deducción social)
│       │   ├── ImpostorGameScreen.kt  # Fases: Pass & Play, Pistas, Votación y Revelación
│       │   └── ImpostorViewModel.kt   # Sorteo de palabra (90 palabras) y asignación de rol
│       ├── never/                     # Yo Nunca Nunca
│       │   ├── NeverGameScreen.kt     # Tarjetas con gestos táctiles tipo Tinder (Swipe horizontal)
│       │   └── NeverViewModel.kt      # Barajado de 222 preguntas
│       ├── truth/                     # Verdad o Reto
│       │   ├── TruthGameScreen.kt     # Interfaz de selección y revelación de desafíos
│       │   └── TruthViewModel.kt      # 284 consignas cargadas dinámicamente por reflexión en R.string
│       └── likely/                    # ¿Quién es más probable?
│           ├── MostLikelyGameScreen.kt# Tarjeta de debate grupal
│           └── MostLikelyViewModel.kt # 319 preguntas de acusación social
└── utils/
    ├── NavigationExtension.kt         # Extensions navigateSafe y popBackStackSafe
    └── SoundManager.kt                # @Singleton: Reproducción segura de efectos de audio
```

---

## 3. Catálogo y Mecánicas de los 7 Minijuegos

1. **PartyMix (WarmUp) (`game_warmup`):**
   * Rondas configurables (5-100) y frecuencia de eventos (2-10).
   * Video en bucle de fondo (`R.raw.background_video`).
   * 116+ frases grupales y 11 eventos especiales (`SHOT_CHALLENGE`, `SPEED_TEST`, `RPS_DUEL`, `STARING_CONTEST`, `THE_JUDGE`, `GIFT`, `ICE_PASS`, `BLIND_CHICKEN`, `SELFIE`, `MEDUSA`, `CHALLENGE`).
   * Registro reactivo de tragos por participante y resumen de final de partida con corona al más bebedor y sentencia final.
2. **Cultura Chupística (`culture_selection`):**
   * *Modo Clásico:* 113 categorías aleatorias.
   * *Modo Bomba:* Fusible oculto de 30-60 segundos con audio `tic_tac` que acelera progresivamente (de 1100ms a 250ms) y explosión final `explosion.wav`.
3. **King's Cup (`game_kingscup`):**
   * Mazo de 52 naipes. Reglas canónicas del 1 al Rey.
   * Los primeros 3 Reyes echan trago a la copa; el 4to Rey debe beberla entera.
4. **El Impostor (`game_impostor`):**
   * Mínimo 3 jugadores (recomendado 4+). 90 palabras secretas.
   * Un participante recibe `???` y debe disimular en la fase de pistas antes de la votación grupal.
5. **Yo Nunca Nunca (`game_never`):**
   * 222 preguntas con tarjeta táctil deslizable (*Swipe*) y rotación inercial.
6. **Verdad o Reto (`game_truth`):**
   * 161 Verdades y 123 Retos cargados por reflexión en tiempo de ejecución (`R.string::class.java.fields`).
   * Turnos rotativos en cola equitativa (*round-robin*).
7. **¿Quién es más probable? (`game_likely`):**
   * 319 preguntas de juicio social y señalamiento a la cuenta de tres.

---

## 4. Banco de Recursos y Localización

* **Español (`res/values/strings.xml`):** Español (idioma base).
* **Inglés (`res/values-en/strings.xml`):** Inglés.
* **Portugués (`res/values-pt/strings.xml`):** Portugués.
* **String Arrays Principales:**
  * `culture_categories_list`: 113 ítems.
  * `bomb_categories_list`: 40 ítems.
  * `never_questions_list`: 222 ítems.
  * `impostor_words_list`: 90 ítems.
  * `likely_questions_list`: 319 ítems.
* **Recursos de Audio/Video (`res/raw/`):**
  * `background_video.mp4`: Fondo de discoteca en bucle.
  * `tic_tac.mp3`: Tictac de la bomba.
  * `explosion.wav`: Detonación de la bomba.

---

## 5. Cheat-Sheet para Extender el Proyecto

* **Para añadir una nueva pregunta o reto:**
  1. En `res/values/strings.xml`, agregar la entrada `<string name="truth_xxx">...</string>` o en el `<string-array>` correspondiente.
  2. Duplicar la clave con su respectiva traducción en `values-en/strings.xml` y `values-pt/strings.xml`.
  3. No requiere cambios en código Kotlin en juegos como *Verdad o Reto*, *Yo Nunca* o *Cultura* gracias a su carga dinámica.
* **Para añadir un nuevo minijuego:**
  1. Crear la entrada `GameModel` en `GameRepository.allGames`.
  2. Diseñar el paquete en `ui/theme/nuevo_juego/` con `NuevoJuegoScreen.kt` y `NuevoJuegoViewModel.kt`.
  3. Declarar la ruta en `MainActivity.kt` dentro del `NavHost`.
  4. Utilizar `navController.navigateSafe("game_nuevo_juego")`.
