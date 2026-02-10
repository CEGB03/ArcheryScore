# ArcheryScore Pro - Sistema Digital de Gestión de Puntajes de Tiro con Arco

Aplicación multiplataforma para registro, gestión y análisis digital de puntajes en entrenamientos y torneos de tiro con arco, con integración federativa y sistemas de entrada dual (tradicional y táctil).

---

## 📱 Estructura de Archivos

### **Modelos de Datos** (`shared/src/commonMain/kotlin/com/archeryscore/data/model/`)

| Archivo | Descripción |
|---------|------------|
| [`User.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/User.kt) | Usuario (id, dni, nombre, email, rol, club, estadoFederativo) |
| [`Archer.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/Archer.kt) | Arquero extendido (categoría, licencia, historial médico) |
| [`Tournament.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/Tournament.kt) | Torneo (id, nombre, fecha, clubOrganizador, estado, normasFATARCO) |
| [`Training.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/Training.kt) | Entrenamiento (id, arqueroId, fecha, condicionesClimáticas) |
| [`Target.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/Target.kt) | Blanco (tipo, dimensiones, imagenSVG, sistemaPuntuación) |
| [`End.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/End.kt) | Tanda (id, numero, flechas: List<ArrowImpact>, tiempoRestante) |
| [`ArrowImpact.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/ArrowImpact.kt) | Impacto (coordenadas: Point, valor: Int, timestamp, metodoEntrada) |
| [`Contention.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/Contention.kt) | Contención (id, arqueros: List<Archer>, juezAsignado, estado) |
| [`JudgeRequest.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/JudgeRequest.kt) | Solicitud de juez (id, contentionId, arqueroSolicitante, motivo, estado) |
| [`Statistics.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/Statistics.kt) | Estadísticas (dispersión, precisión, evoluciónTemporal, histograma) |
| [`FATARCOProfile.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/model/FATARCOProfile.kt) | Perfil federativo (datos scraping: club, estado, categorías) |

### **Repositorios** (`shared/src/commonMain/kotlin/com/archeryscore/data/repository/`)

| Archivo | Descripción |
|---------|------------|
| [`AuthRepository.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/repository/AuthRepository.kt) | Autenticación, roles (Arquero, Entrenador, Juez, Club) |
| [`TournamentRepository.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/repository/TournamentRepository.kt) | Gestión CRUD de torneos y contenciones |
| [`ScoringRepository.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/repository/ScoringRepository.kt) | Registro puntajes (dual: tradicional/táctil) |
| [`FATARCORepository.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/repository/FATARCORepository.kt) | Web scraping datos federativos públicos |
| [`StatisticsRepository.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/repository/StatisticsRepository.kt) | Cálculo estadísticas y gráficos |
| [`ExportRepository.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/repository/ExportRepository.kt) | Exportación PDF/Excel con formatos oficiales |

### **API y Comunicación** (`shared/src/commonMain/kotlin/com/archeryscore/data/remote/`)

| Archivo | Descripción |
|---------|------------|
| [`ApiService.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/remote/ApiService.kt) | Endpoints backend principal |
| [`FATARCOApiService.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/remote/FATARCOApiService.kt) | Interface para web scraping federativo |
| [`RealTimeService.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/remote/RealTimeService.kt) | WebSocket para actualizaciones tiempo real |
| [`RetrofitInstance.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/remote/RetrofitInstance.kt) | Configuración Retrofit + interceptores |
| [`AuthInterceptor.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/remote/AuthInterceptor.kt) | Interceptor JWT + roles |

### **Almacenamiento Local** (`shared/src/commonMain/kotlin/com/archeryscore/data/local/`)

| Archivo | Descripción |
|---------|------------|
| [`ScoringCache.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/local/ScoringCache.kt) | Cache offline de puntajes (cada flecha se guarda inmediatamente) |
| [`TargetTemplates.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/local/TargetTemplates.kt) | Plantillas SVG de blancos responsive |
| [`QRStorage.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/local/QRStorage.kt) | Gestión QR personalizados y de torneo |
| [`SettingsManager.kt`](shared/src/commonMain/kotlin/com/archeryscore/data/local/SettingsManager.kt) | Preferencias (método entrada, notificaciones) |

### **ViewModels** (`shared/src/commonMain/kotlin/com/archeryscore/viewmodel/`)

| Archivo | Descripción |
|---------|------------|
| [`AuthViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/AuthViewModel.kt) | Estado autenticación y gestión de roles |
| [`TournamentViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/TournamentViewModel.kt) | Torneos activos, contenciones, tiempos |
| [`ScoringViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/ScoringViewModel.kt) | Lógica dual de puntuación (tradicional/táctil) |
| [`RealTimeViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/RealTimeViewModel.kt) | Sincronización tiempo real entre dispositivos |
| [`StatisticsViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/StatisticsViewModel.kt) | Generación gráficos y análisis |
| [`JudgeViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/JudgeViewModel.kt) | Gestión de solicitudes de revisión |
| [`ExportViewModel.kt`](shared/src/commonMain/kotlin/com/archeryscore/viewmodel/ExportViewModel.kt) | Exportación PDF/Excel con datos climáticos |

### **Pantallas UI** (`androidApp/src/main/kotlin/com/archeryscore/ui/` e `iosApp/src/iosMain/kotlin/com/archeryscore/ui/`)

| Archivo | Descripción |
|---------|------------|
| [`RoleSelectionScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/RoleSelectionScreen.kt) | Selección inicial de rol (Arquero, Entrenador, Juez, Invitado) |
| [`TournamentDashboardScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/TournamentDashboardScreen.kt) | Dashboard torneo con tiempos, contenciones, notificaciones |
| [`DualScoringScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/DualScoringScreen.kt) | Puntuación dual (modo tradicional/táctil con toggle) |
| [`TraditionalScoringView.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/TraditionalScoringView.kt) | Tabla numérica con teclado y confirmación por arquero |
| [`TactileScoringView.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/TactileScoringView.kt) | Blanco interactivo con zoom táctil y minimapa |
| [`RealTimeViewScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/RealTimeViewScreen.kt) | Vista espectador/entrenador (actualizaciones en vivo) |
| [`JudgeRequestsScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/JudgeRequestsScreen.kt) | Panel de solicitudes de jueces (cola de revisiones) |
| [`StatisticsScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/StatisticsScreen.kt) | Gráficos interactivos (torta, barras, lineal, dispersión) |
| [`QRAttendanceScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/QRAttendanceScreen.kt) | Escaneo QR para asistencia y categorización |
| [`ExportScreen.kt`](androidApp/src/main/kotlin/com/archeryscore/ui/ExportScreen.kt) | Exportación PDF/Excel con escudos y datos climáticos |

### **Componentes Reutilizables** (`shared/src/commonMain/kotlin/com/archeryscore/ui/components/`)

| Archivo | Descripción |
|---------|------------|
| [`TargetCanvas.kt`](shared/src/commonMain/kotlin/com/archeryscore/ui/components/TargetCanvas.kt) | Canvas interactivo para blancos SVG responsive |
| [`TimerWidget.kt`](shared/src/commonMain/kotlin/com/archeryscore/ui/components/TimerWidget.kt) | Cronómetro con cambio de color (Verde→Amarillo→Rojo) |
| [`ArrowImpactMarker.kt`](shared/src/commonMain/kotlin/com/archeryscore/ui/components/ArrowImpactMarker.kt) | Marcador visual de impactos en blanco |
| [`StatisticsChart.kt`](shared/src/commonMain/kotlin/com/archeryscore/ui/components/StatisticsChart.kt) | Gráficos reutilizables (Compose Multiplatform) |
| [`CorrectionBadge.kt`](shared/src/commonMain/kotlin/com/archeryscore/ui/components/CorrectionBadge.kt) | Indicador de puntaje corregido por juez |

### **Utilidades** (`shared/src/commonMain/kotlin/com/archeryscore/util/`)

| Archivo | Descripción |
|---------|------------|
| [`TargetCalibration.kt`](shared/src/commonMain/kotlin/com/archeryscore/util/TargetCalibration.kt) | Calibración automática responsive de blancos |
| [`CoordinateMapper.kt`](shared/src/commonMain/kotlin/com/archeryscore/util/CoordinateMapper.kt) | Mapeo coordenadas táctiles a valores numéricos |
| [`FATARCOScraper.kt`](shared/src/commonMain/kotlin/com/archeryscore/util/FATARCOScraper.kt) | Web scraping seguro de datos federativos |
| [`WeatherIntegration.kt`](shared/src/commonMain/kotlin/com/archeryscore/util/WeatherIntegration.kt) | Obtención datos climáticos para estadísticas |
| [`QRGenerator.kt`](shared/src/commonMain/kotlin/com/archeryscore/util/QRGenerator.kt) | Generación QR personalizados y de torneo |
| [`OfflineSync.kt`](shared/src/commonMain/kotlin/com/archeryscore/util/OfflineSync.kt) | Sincronización offline vía dispositivos cercanos |

---

## 🔄 Flujos de Comunicación

### **1. Flujo de Autenticación y Roles**

```
RoleSelectionScreen → Selección rol (Arquero/Entrenador/Juez/Invitado)
  ↓
Arquero: login tradicional o QR personal
Entrenador: credenciales club + verificación FATARCO
Juez: credenciales federativas + scraping verificación
Invitado: QR torneo → acceso sólo lectura
  ↓
AuthRepository.validateRole()
  ↓
POST /auth/login {dni, password, role}
  ↓
Backend valida + retorna JWT con permisos específicos
  ↓
Navegación a dashboard según rol
```

### **2. Flujo de Puntuación Dual**

```
DualScoringScreen → Toggle entre métodos
  ↓
MÉTODO TRADICIONAL:
  TraditionalScoringView.render()
  ↓
  Tabla numérica (6 flechas × n arqueros)
  ↓
  Teclado numérico overlay → validación rangos
  ↓
  Confirmación por arquero → guardar local + sync

MÉTODO TÁCTIL:
  TactileScoringView.render()
  ↓
  Blanco SVG responsive con calibración automática
  ↓
  Touch down → zoom automático + minimapa
  ↓
  Touch up → CoordinateMapper → valor numérico
  ↓
  ArrowImpactMarker dibuja impacto con animación
  ↓
  Auto-save coordenadas (x,y) + valor + timestamp
```

### **3. Flujo de Tiempo y Contenciones**

```
TournamentDashboardScreen → Tiempo definido por normas FATARCO
  ↓
TimerWidget inicia cuenta regresiva
  ↓
Cambios de color:
  - Verde: Primeros 2/3 del tiempo
  - Amarillo: Último 1/3
  - Rojo: Tiempo terminado (bloquea entrada)
  ↓
Fin tiempo → ScoringRepository.finalizeEnd()
  ↓
Sincronización automática a todos los dispositivos
  ↓
Entrenadores/espectadores ven actualización inmediata
```

### **4. Flujo de Corrección por Juez**

```
Arquero en contención → botón "Solicitar Revisión"
  ↓
JudgeRequest creado (contentionId, arquero, motivo)
  ↓
JudgeRequestsScreen (jueces) → muestra notificación push
  ↓
Juez selecciona solicitud → navega a contención
  ↓
Interfaz especial de juez: puede modificar puntaje
  ↓
CorrectionBadge marca puntaje modificado (color distinto)
  ↓
Historial de correcciones guardado para auditoría
  ↓
Blqueo levantado → contención puede continuar
```

### **5. Flujo de Estadísticas en Tiempo Real**

```
StatisticsScreen (Entrenador/Arquero)
  ↓
StatisticsRepository.calculateRealTime()
  ↓
Por tanda/tiro: dispersión, precisión, agrupación
  ↓
StatisticsChart renderiza:
  - Gráfico de torta: distribución por color
  - Gráfico de barras: frecuencia por valor
  - Gráfico lineal: evolución durante jornada
  - Diagrama dispersión: posición impactos
  ↓
Filtros: fecha, blanco, clima, distancia, categoría
  ↓
Exportación individual/privada
```

### **6. Flujo de Integración FATARCO**

```
Registro arquero federado → ingresa DNI
  ↓
FATARCORepository.scrapePublicData(dni)
  ↓
Web scraping seguro de https://fatarco.org/archers/
  ↓
Parseo: nombre, club, estado, categorías
  ↓
Validación automática de categoría para torneo
  ↓
Cache local + actualización periódica
  ↓
Inscripción automática en torneos según categoría
```

### **7. Flujo de Exportación Oficial**

```
ExportScreen → selecciona evento y formato
  ↓
ExportRepository.generate()
  ↓
PDF: Planilla oficial FATARCO + escudos + datos climáticos
  ↓
Excel: Formato estándar federativo para importación
  ↓
Estadísticas individuales en páginas adicionales
  ↓
Compartir: email, WhatsApp, descarga local
  ↓
Registro de exportaciones para seguimiento
```

---

## 🎭 Ciclo de Vida de Pantallas

### **TORNEO ACTIVO**
```
TOURNAMENT DASHBOARD (Organizador/Juez)
├─ Visión general todas las contenciones
├─ Timer central con cambio de colores
├─ Notificaciones de solicitudes de juez
└─ Botones: pausa, emergencia, finalizar

DUAL SCORING SCREEN (Arquero registrador)
├─ Toggle entre métodos tradicional/táctil
├─ Timer local sincronizado
├─ Indicador de flechas restantes
├─ Botón "Solicitar Juez" (bloqueante)
└─ Confirmación por arquero antes de siguiente

REAL TIME VIEW (Entrenador/Espectador)
├─ Vista sólo lectura de múltiples contenciones
├─ Actualización automática vía WebSocket
├─ Filtros por categoría, distancia, club
└── Acceso a estadísticas básicas

JUDGE REQUESTS SCREEN (Juez)
├─ Lista priorizada de solicitudes
├─ Notificaciones push con sonido
├─ Navegación rápida a contención
└─ Historial de correcciones del día
```

### **ENTRENAMIENTO**
```
INDIVIDUAL SCORING (Arquero)
├─ Configuración personal: blanco, distancia, tiempo
├─ Método preferido guardado en SettingsManager
├── Estadísticas en tiempo real
└── Exportación privada con QR personal

COACH VIEW (Entrenador)
├─ Monitoreo múltiples arqueros simultáneo
├─ Comparativa histórica (sólo con permiso)
├─ Anotaciones personalizadas por arquero
└── Generación de informes de progreso
```

---

## 📊 Arquitectura de Datos

```
DISPOSITIVOS MÓVILES (Android/iOS - Kotlin Multiplatform)
│
├─ CAPA UI: Compose Multiplatform
│  ├── Pantallas específicas por rol
│  ├── Componentes reutilizables
│  └── Adaptación responsive automática
│
├─ CAPA VIEWMODEL: ViewModel (Shared)
│  ├── ScoringViewModel (lógica dual)
│  ├── RealTimeViewModel (WebSocket)
│  ├── StatisticsViewModel (cálculos)
│  └── JudgeViewModel (gestión correcciones)
│
├─ CAPA REPOSITORIO: Repository Pattern
│  ├── ScoringRepository (auto-save cada impacto)
│  ├── FATARCORepository (scraping con cache)
│  ├── StatisticsRepository (cálculos complejos)
│  └── ExportRepository (formatos oficiales)
│
├─ ALMACENAMIENTO LOCAL
│  ├── ScoringCache (SQLite - backup por flecha)
│  ├── TargetTemplates (SVG assets)
│  └── SettingsManager (preferencias multiplataforma)
│
├─ COMUNICACIÓN EN TIEMPO REAL
│  ├── WebSocket para datos de torneo
│  └── Nearby Connections para modo offline
│
└─ CAPA RED: Ktor Client Multiplatform
   ├── ApiService (REST principal)
   ├── FATARCOApiService (scraping)
   ├── RealTimeService (WebSocket)
   └── Auth interceptor con JWT

↓ INTERNET

BACKEND: Spring Boot / Node.js
├── Autenticación JWT con roles
├── Gestión torneos y contenciones
├── WebSocket server para tiempo real
├── Generación PDF/Excel oficial
└── Sincronización multi-dispositivo

↓

DATABASE: PostgreSQL
├── users (id, dni, role, federative_data)
├── tournaments (id, club, dates, fatarco_rules)
├── contentions (id, archers, judge, time_config)
├── ends (id, arrows, scores, corrections)
├── statistics (arquero_id, metrics, timestamps)
└── judge_requests (id, status, history)
```

---

## 🔐 Seguridad y Validaciones

### **Protección contra Fraude**
- Un único arquero por contención puede ingresar datos en torneo
- Los demás arqueros de la contención ven en tiempo real
- Correcciones sólo por jueces autorizados, con historial completo
- Modo espectador: sólo lectura, sin opciones de modificación

### **Validaciones de Datos**
- Rangos de puntuación según tipo de blanco seleccionado
- Tiempos configurados según normas FATARCO anuales
- Categorías predefinidas, actualizables sólo por administrador
- Coordenadas táctiles validadas contra dimensiones del blanco

### **Backup y Recuperación**
- Cada impacto de flecha se guarda localmente inmediatamente
- Sincronización incremental al recuperar conexión
- Historial completo de cambios (auditor trail)
- Exportaciones automáticas al finalizar eventos

---

## 🎯 Casos de Uso Principales

### **Caso 1: Arquero en Torneo (Registrador)**
```
1. Escanea QR del torneo → asigna contención
2. DualScoringScreen → selecciona método preferido
3. Ingresa puntajes (6 flechas × 3 arqueros)
4. Timer cambia verde → amarillo → rojo
5. Al terminar tiempo: confirma cada arquero
6. Sistema valida y envía a servidor
7. Solicita juez si hay discrepancia
```

### **Caso 2: Entrenador Monitoreando**
```
1. Accede como entrenador (rol verificada)
2. RealTimeViewScreen → selecciona sus arqueros
3. Ve múltiples contenciones simultáneamente
4. StatisticsScreen → análisis en profundidad
5. Filtra por condiciones climáticas históricas
6. Exporta reportes personalizados por arquero
```

### **Caso 3: Juez Gestionando Revisiones**
```
1. JudgeRequestsScreen → lista de solicitudes
2. Selecciona contención → interfaz de corrección
3. Modifica puntaje específico (marcado en color)
4. Confirma → desbloquea contención
5. Historial guardado para informe final
6. Notifica a organizador de correcciones críticas
```

### **Caso 4: Organizador de Torneo**
```
1. Crea torneo con normas FATARCO actuales
2. Configura categorías, distancias, tiempos
3. Genera QR general para asistencia
4. Monitorea progreso en TournamentDashboard
5. Gestiona tiempos extraordinarios (pausas)
6. Exporta resultados oficiales con escudos
```

### **Caso 5: Arquero en Entrenamiento**
```
1. Individual mode → configura sesión personal
2. TactileScoringView → dibuja impactos con zoom
3. Statistics en tiempo real mientras entrena
4. Compara con sesiones anteriores
5. Genera QR personal para compartir con entrenador
6. Exporta PDF con análisis completo
```

---

## 🚀 Flujos de Error y Recuperación

| Escenario | Manejo |
|-----------|--------|
| **Pérdida de conexión** | Cache local inmediato + sincronización posterior vía Nearby Connections |
| **Tiempo agotado** | Bloqueo de entrada + notificación + opción de extensión por juez |
| **Error en scraping FATARCO** | Cache local + modo manual con verificación posterior |
| **Discrepancia en puntajes** | Bloqueo hasta revisión de juez + registro de conflicto |
| **QR inválido** | Validación local + reintento + modo manual de asistencia |
| **Espacio de almacenamiento** | Auto-cleanup de cache antiguo + compresión de datos |

---

## 🔧 Tecnologías Utilizadas

- **Multiplataforma:** Kotlin Multiplatform (Android, iOS)
- **UI:** Jetpack Compose (Android), SwiftUI bridge (iOS)
- **Arquitectura:** MVI + Repository Pattern + Clean Architecture
- **Persistencia:** SQLDelight (SQLite multiplataforma)
- **Red:** Ktor Client + WebSocket + Retrofit (Android)
- **Gráficos:** Skia/Compose Graphics + MPAndroidChart wrappers
- **Scraping:** Jsoup (Android) + Ktor client parser
- **QR:** ZXing multiplataforma
- **PDF:** PdfKit (iOS) + iText (Android) wrappers
- **Excel:** Apache POI (Android) + CoreXLSX (iOS) wrappers
- **Tiempo Real:** WebSocket + Nearby Connections API
- **Dependencias:** Koin multiplataforma para DI

---

## 📝 Características Únicas

### **1. Sistema de Entrada Dual**
- **Tradicional:** Tabla numérica optimizada para velocidad
- **Táctil:** Blanco interactivo con zoom automático y minimapa
- **Toggle instantáneo** durante la competencia
- **Coordenadas guardadas** para análisis de dispersión

### **2. Integración FATARCO Inteligente**
- Web scraping de datos públicos federativos
- Validación automática de categorías y licencias
- Actualización de normas anuales por administrador
- Cache inteligente para reducir solicitudes

### **3. Sistema de Tiempo Inteligente**
- Configuración según normas FATARCO por categoría
- Cambio de colores visual (Verde→Amarillo→Rojo)
- Control centralizado por jueces de mesa
- Sincronización multi-dispositivo

### **4. Estadísticas Avanzadas**
- Análisis en tiempo real durante la competencia
- Gráficos interactivos con filtros múltiples
- Comparativa histórica con condiciones climáticas
- Exportación profesional con datos contextuales

### **5. Sistema de Correcciones**
- Flujo formalizado de solicitud de juez
- Historial completo de cambios para auditoría
- Marcado visual de puntajes corregidos
- Bloqueo/desbloqueo automático de contenciones

---

## 📞 Endpoints Principales Propuestos

```
# Autenticación y Usuarios
POST   /auth/login                    - Login con rol
POST   /auth/fatarco-verify           - Verificación federativa
GET    /users/fatarco/{dni}           - Obtener datos federativos
PUT    /users/categories              - Actualizar categorías (admin)

# Torneos
POST   /tournaments                   - Crear torneo
GET    /tournaments/active            - Listar torneos activos
POST   /tournaments/{id}/attendance   - Registrar asistencia QR
GET    /tournaments/{id}/contentions  - Obtener contenciones

# Puntuación
POST   /scoring/arrow                 - Registrar impacto individual
PUT    /scoring/end/{id}              - Finalizar tanda
GET    /scoring/contention/{id}       - Obtener puntajes contención
PUT    /scoring/correction            - Aplicar corrección de juez

# Tiempo Real
WS     /realtime/{tournamentId}       - WebSocket para actualizaciones
POST   /realtime/judge-request        - Solicitar juez

# Estadísticas
GET    /statistics/arquero/{id}       - Estadísticas individuales
GET    /statistics/tournament/{id}    - Estadísticas de torneo
POST   /statistics/filter             - Estadísticas con filtros

# Exportación
POST   /export/pdf/tournament         - Generar PDF oficial
POST   /export/excel/tournament       - Generar Excel federativo
GET    /export/history/{userId}       - Historial de exportaciones
```

---

## 🏗️ Estructura del Proyecto

```
archeryscore-pro/
├── shared/                           # Código compartido KMP
│   ├── src/commonMain/kotlin/
│   │   ├── com/archeryscore/
│   │   │   ├── data/                # Modelos, repositorios
│   │   │   ├── domain/              Casos de uso
│   │   │   ├── presentation/        # ViewModels, estados
│   │   │   └── di/                  # Inyección dependencias
│   │   └── resources/               # Assets compartidos
│   └── build.gradle.kts
├── androidApp/                       # App Android
│   ├── src/main/kotlin/
│   │   └── com/archeryscore/
│   │       ├── ui/                  # Pantallas Android
│   │       └── MainActivity.kt
│   └── build.gradle.kts
├── iosApp/                          # App iOS
│   ├── src/iosMain/kotlin/
│   │   └── com/archeryscore/
│   │       └── ui/                  # Pantallas iOS
│   ├── xcode/                       # Proyecto Xcode
│   └── build.gradle.kts
├── backend/                         # Servidor (opcional)
│   ├── src/main/java/
│   └── build.gradle
└── build.gradle.kts                 # Root build
```

---

## 🚦 Estado del Proyecto

**Fase Actual:** Diseño Arquitectónico  
**Plataformas:** Android (prioridad), iOS (segunda fase)  
**Backend:** Por definir (Spring Boot recomendado)  
**Integración FATARCO:** Web scraping (fase 1), API oficial (futuro)  
**Formalización:** En conversaciones con clubes piloto  

---

## 👥 Roles y Permisos

| Rol | Permisos | Acceso FATARCO |
|-----|----------|----------------|
| **Administrador** | Todo acceso, actualiza categorías | API completa |
| **Organizador Club** | Crear torneos, gestionar asistencias | Verificación básica |
| **Juez FATARCO** | Corregir puntajes, gestionar tiempos | Credenciales federativas |
| **Entrenador** | Ver estadísticas, analizar arqueros | Datos de sus arqueros |
| **Arquero Federado** | Cargar puntajes, ver estadísticas propias | Datos personales |
| **Arquero No Federado** | Cargar puntajes, ver estadísticas propias | Ninguno |
| **Invitado/Espectador** | Solo lectura tiempo real | Ninguno |

---

**Última actualización:** `10/02/2026`  
**Versión:** 1.0 - Diseño Arquitectónico  
**Autor:** CEGB03  
**Contacto:** emanuelgb03@gmail.com  

*Nota: Este proyecto sigue las normativas FATARCO 2025/26 y está diseñado para evolucionar con los cambios federativos.*
