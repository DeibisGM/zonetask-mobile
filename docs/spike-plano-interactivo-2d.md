# Spike #297 — Plano Interactivo 2D en Android con Kotlin + Jetpack Compose

> **Tipo:** Investigación técnica (spike)
> **Stack app:** Kotlin, Jetpack Compose (BOM 2025.12.00 / Compose 1.10), Retrofit
> **Backend:** Endpoint REST en .NET (existente, fuera del alcance de este spike)
> **Persistencia local:** Por decidir (Room como caché opcional, ver issue #300)
> **Objetivo:** Generar la base de conocimiento necesaria para implementar un plano 2D editable e interactivo dentro de la app, antes de comenzar el desarrollo.

---

## 1. Preguntas de investigación — respuestas

### 1.1. ¿Cuáles son las mejores opciones para renderizar planos interactivos 2D en Jetpack Compose?

Hay cuatro caminos viables. La elección depende de si el plano se **dibuja procedimentalmente desde datos** (caso típico de este proyecto) o se **carga desde un archivo SVG/imagen** producido por otra herramienta.

| Opción | Cuándo usarla | Notas clave |
|---|---|---|
| **Compose `Canvas` / `drawBehind` / `drawWithCache`** | El plano se genera a partir de datos (zonas en una BD). Es el caso de este proyecto. | API nativa de Compose, basada en Skia. Soporta `drawRect`, `drawPath`, `drawImage`, transformaciones, gradientes, blend modes. Es la opción de primer nivel recomendada por Google. El Canvas API es la puerta de entrada a visualizaciones ricas e interactivas y UIs personalizadas en Compose. |
| **Coil 3 + `coil-svg`** | El plano se entrega como SVG (estático o remoto) y solo se le superponen zonas interactivas encima. | Coil 3 soporta `coil-svg` en todas las plataformas; en Android está respaldado por AndroidSVG. La interactividad de las zonas se implementa encima del `AsyncImage` con composables superpuestos. |
| **AndroidView + `SVGImageView` (AndroidSVG)** | SVGs muy complejos cargados en runtime (ej. planos arquitectónicos descargados de un servidor) donde Coil falla. | La forma más directa de renderizar imágenes AndroidSvg dentro de Jetpack Compose es envolver un SvgImageView en un AndroidView, pero los AndroidViews tienen un overhead significativo. Última opción. |
| **`ImageVector` / `VectorPainter`** | El plano es un asset fijo conocido en compile-time (poco usual para planos editables). | Es lo que produce el Asset Studio de Android Studio. No sirve si el plano viene de la BD o de un endpoint. |

**Recomendación para este spike:** **Compose `Canvas` puro**. El plano se construye desde una lista de `Zone` provenientes del endpoint .NET (y opcionalmente cacheadas en Room), así que no hay un SVG que cargar; se dibujan rectángulos (o paths) directamente. Las otras opciones son alternativas viables solo si en algún momento el backend empieza a entregar planos pre-renderizados como SVG.

---

### 1.2. ¿Cómo funciona el sistema de coordenadas en Compose `Canvas` y cómo se mapean las zonas del plano?

Compose `Canvas` usa un sistema de coordenadas 2D con origen `(0, 0)` en la esquina **superior izquierda**, X creciendo a la derecha, Y creciendo hacia abajo. Las unidades son **pixels** (no dp) dentro del scope de dibujo — Compose ya resolvió la densidad antes de llegar ahí.

Para mapear zonas de la BD a coordenadas de pantalla hay que separar **tres espacios**:

1. **Espacio del modelo (world coordinates):** lo que está en el modelo de datos (lo que devuelve el endpoint o lo que está en Room si se usa caché). Por ejemplo `Zone(x=100, y=200, width=50, height=40)`. Estas son unidades lógicas del plano, independientes del dispositivo.
2. **Espacio del lienzo (canvas coordinates):** pixels reales dentro del `Canvas` composable, dependientes del tamaño del contenedor.
3. **Espacio de pantalla con transformación de usuario:** después de aplicar pan y zoom con `graphicsLayer` o transformaciones de `DrawScope` (`scale { … }`, `translate { … }`).

**Patrón recomendado:**
- Guardar las zonas en **unidades del modelo** (no pixels).
- Calcular un factor de escala `worldToCanvas = canvasSize / worldSize` para llenar el lienzo manteniendo aspect ratio.
- Aplicar `scale` y `translate` del usuario por encima, usando `graphicsLayer`.
- Para hit-testing de gestos (tap sobre una zona), aplicar la **transformación inversa** al punto del toque y comparar contra las coordenadas del modelo.

```kotlin
fun screenToWorld(
    point: Offset,
    pan: Offset,
    scale: Float,
    worldToCanvas: Float
): Offset = (point - pan) / scale / worldToCanvas
```

---

### 1.3. Madurez y eficiencia de Compose `Canvas` vs. Canvas tradicional de Views

Compose `Canvas` es un wrapper sobre el `android.graphics.Canvas` clásico (Skia por debajo), así que en rendimiento crudo son equivalentes para dibujo. La diferencia está en cómo se integran con el resto de la UI:

- **A favor de Compose `Canvas`:** integración natural con `State`, recomposición selectiva, transformaciones declarativas, `drawWithCache` para evitar recomputaciones costosas, y composabilidad con el resto de la jerarquía Compose.
- **A favor del Canvas tradicional:** si el equipo ya tiene una `View` custom muy optimizada con `invalidate()` selectivo, migrar tiene costo y poco beneficio inmediato.

A diciembre de 2025, Compose alcanzó paridad con Views también en otros frentes: los benchmarks internos de scroll muestran que Compose ahora iguala el rendimiento que verías usando Views. Para un proyecto nuevo, **Compose `Canvas` es la opción correcta** sin reservas de rendimiento.

**Optimización clave:** usar `Modifier.drawBehind { … }` o `Modifier.drawWithCache { onDrawBehind { … } }` en vez del composable `Canvas` cuando se dibuja sobre un layout existente. `drawWithCache` permite memoizar `Path`, `Brush` y otros objetos costosos entre recomposiciones — crítico cuando hay muchas zonas.

---

### 1.4. ¿Cómo manejar gestos táctiles (tap, drag, pinch-to-zoom, rotate) eficientemente?

Compose ofrece tres niveles de abstracción para gestos. La regla es preferir modificadores de gesto out-of-the-box sobre el manejo custom de gestos, porque agregan más funcionalidad encima del manejo puro de eventos del puntero.

**Nivel 1 — Modificadores de alto nivel** (preferir cuando alcancen):
- `Modifier.clickable { … }` — taps simples con feedback visual y semántica de accesibilidad.
- `Modifier.draggable(state, orientation)` — drag en un eje.
- `Modifier.transformable(state)` — pan + zoom + rotate combinados, manejados por `rememberTransformableState`.

**Nivel 2 — Detectores dentro de `pointerInput`**:
- `detectTapGestures(onTap, onDoubleTap, onLongPress)`
- `detectDragGestures(onDragStart, onDrag, onDragEnd, onDragCancel)`
- `detectTransformGestures(panZoomLock, onGesture)` — detector de gestos para rotación, pan y zoom; el callback `onGesture` se invoca cuando ocurre cualquiera de los tres, pasando ángulo de rotación en grados, factor de zoom y pan como un offset en pixels.

**Nivel 3 — Eventos crudos** con `awaitPointerEventScope`: para gestos custom no cubiertos (drag-after-long-press combinado con multi-touch, por ejemplo).

**Patrón recomendado para este proyecto:**

```kotlin
@Composable
fun PlanoInteractivo(zones: List<Zone>, onZoneMoved: (Zone) -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Capa 1: pan + zoom global del lienzo
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offset += pan
                }
            }
            .graphicsLayer(
                scaleX = scale, scaleY = scale,
                translationX = offset.x, translationY = offset.y
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            zones.forEach { zone ->
                drawRect(
                    color = zone.color,
                    topLeft = Offset(zone.x, zone.y),
                    size = Size(zone.width, zone.height)
                )
            }
        }
        // Capa 2: por cada zona, un overlay con drag individual
        zones.forEach { zone ->
            ZoneDragOverlay(zone, scale, onZoneMoved)
        }
    }
}
```

**Conflictos típicos y cómo resolverlos:**
- **Drag de zona vs. pan del lienzo:** usar capas separadas de `pointerInput`. La zona consume el evento con `change.consume()` para que no llegue al pan global.
- **Tap vs. doble-tap:** si se provee `onDoubleTap`, el sistema espera un tiempo corto antes de considerar un tap como doble-tap; útil si doble-tap hace zoom-to-fit.
- **Pinch en plano vs. scroll del padre:** envolver el plano en un `Box` con `Modifier.pointerInput` que consume gestos multi-touch antes de que lleguen al `LazyColumn` o `ScrollView` padre.

---

### 1.5. ¿Cómo consumir las zonas del endpoint .NET y opcionalmente persistirlas localmente?

El backend es un endpoint REST en .NET que la app consume. Hay **dos arquitecturas posibles** según los requisitos del producto: consumir solo del API, o usar Room como caché local (offline-first). Esta sección documenta ambas para que el equipo decida.

#### Contrato del endpoint .NET

Acordar con el equipo de backend las siguientes rutas:

```
GET    /spaces/{id}/zones            → List<ZoneDto>
POST   /spaces/{id}/zones            → ZoneDto (zona creada con id)
PUT    /spaces/{id}/zones/{zoneId}   → ZoneDto (zona actualizada)
DELETE /spaces/{id}/zones/{zoneId}   → 204 No Content
```

**DTO esperado (JSON):**

```json
{
  "id": 123,
  "spaceId": 45,
  "name": "Zona A",
  "x": 100.0,
  "y": 200.0,
  "width": 50.0,
  "height": 40.0,
  "color": "#FF5733",
  "rotation": 0.0
}
```

#### Modelo de dominio (común a ambas opciones)

Separar el DTO de red del modelo que consume la UI evita acoplar la app al formato del backend.

```kotlin
data class Zone(
    val id: Long,
    val name: String,
    val rect: Rect,        // androidx.compose.ui.geometry.Rect
    val color: Color,
    val rotation: Float
)
```

Con un mapper `ZoneDto.toDomain()` y su inverso.

#### Capa de red (común a ambas opciones)

```kotlin
interface ZonesApi {
    @GET("spaces/{id}/zones")
    suspend fun getZones(@Path("id") spaceId: Long): List<ZoneDto>

    @POST("spaces/{id}/zones")
    suspend fun createZone(@Path("id") spaceId: Long, @Body zone: ZoneDto): ZoneDto

    @PUT("spaces/{id}/zones/{zoneId}")
    suspend fun updateZone(
        @Path("id") spaceId: Long,
        @Path("zoneId") zoneId: Long,
        @Body zone: ZoneDto
    ): ZoneDto

    @DELETE("spaces/{id}/zones/{zoneId}")
    suspend fun deleteZone(@Path("id") spaceId: Long, @Path("zoneId") zoneId: Long)
}
```

Stack sugerido: **Retrofit + OkHttp + Kotlinx Serialization** (o Moshi).

---

#### Opción A — Solo consumo del API (sin caché local)

La app pide las zonas al endpoint cada vez que se abre el plano. Las ediciones se envían directamente.

```kotlin
class ZoneRepository(private val api: ZonesApi) {
    suspend fun getZones(spaceId: Long): List<Zone> =
        api.getZones(spaceId).map { it.toDomain() }

    suspend fun createZone(spaceId: Long, zone: Zone): Zone =
        api.createZone(spaceId, zone.toDto()).toDomain()
}
```

El `ViewModel` expone un `StateFlow<UiState>` con estados `Loading`, `Success(zones)`, `Error`.

**Ventajas:** arquitectura simple, una sola fuente de verdad, menos código, sin riesgo de desincronización.
**Desventajas:** requiere conexión, latencia visible en cada apertura, mala UX en conexiones lentas, las ediciones bloquean al usuario.

---

#### Opción B — API + Room como caché local (offline-first)

La UI siempre lee de Room (a través de `Flow`). Un `Repository` coordina la sincronización con el endpoint .NET. Las ediciones se escriben en Room primero (optimistic update) y luego se sincronizan.

**Entidad Room:**

```kotlin
@Entity(tableName = "zones")
data class ZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: Long?,           // null mientras no se haya sincronizado
    val spaceId: Long,
    val name: String,
    val x: Float, val y: Float,
    val width: Float, val height: Float,
    val colorArgb: Int,
    val rotation: Float = 0f,
    val syncStatus: SyncStatus      // PENDING, SYNCED, ERROR
)
```

**DAO:**

```kotlin
@Dao
interface ZoneDao {
    @Query("SELECT * FROM zones WHERE spaceId = :spaceId")
    fun observeBySpace(spaceId: Long): Flow<List<ZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(zone: ZoneEntity): Long

    @Query("DELETE FROM zones WHERE id = :id")
    suspend fun delete(id: Long)
}
```

**Repository:** combina API + Room, expone `Flow<List<Zone>>` desde Room y un método `refresh()` que sincroniza con el endpoint.

**Ventajas:** funciona offline, UI instantánea (sin loading si hay caché), ediciones fluidas, arquitectura recomendada por Google (Single Source of Truth).
**Desventajas:** más código, hay que diseñar política de sincronización y resolución de conflictos, migraciones de schema.

---

#### Tabla de decisión

| Pregunta | Si la respuesta es… | Indica… |
|---|---|---|
| ¿La app debe funcionar sin conexión? | Sí | Opción B |
| ¿Se acepta un loading visible al abrir el plano? | No | Opción B |
| ¿Cuántos usuarios editan el mismo plano? | Varios simultáneos | Opción B + resolución de conflictos |
| ¿Las zonas cambian con mucha frecuencia entre usuarios? | Sí | Opción A + tiempo real (SignalR) |
| ¿El equipo tiene tiempo para implementar la capa de sync? | No | Opción A primero |

#### Recomendación técnica del spike

A falta de información concreta sobre los requisitos de offline y la frecuencia de edición, se recomienda **empezar por la Opción A** para una primera iteración:

- Permite tener un MVP funcional rápido.
- Si más adelante se confirma que se necesita offline o se nota latencia molesta en UX, agregar Room **encima** del repositorio existente es una refactorización acotada, no una reescritura.
- Empezar con la Opción B sin requisitos confirmados puede llevar a sobre-ingeniería.

La decisión final debe tomarla el equipo con producto y diseño respondiendo las preguntas de la tabla.

---

### 1.6. ¿Existen plantillas open-source de planos o ejemplos SVG?

Sí, varios. Por relevancia:

- **GitHub topic `jetpack-compose-canvas`** — incluye ejemplos como apps de drag-and-drop de cajas a áreas específicas con nombres asociados a cada caja, que es muy cercano al caso de este spike.
- **Gist de `ardakazanci` "Interactive Canvas for jetpack compose"** — punto de partida directo para un lienzo zoomable/paneable.
- **SVGs genéricos de planos** — los típicos de Floorplanner, RoomSketcher o plantillas de Figma sirven solo si se decide ir por la opción Coil + SVG. Para Canvas puro no aplican.

Para este proyecto, lo más rentable es no buscar una plantilla y construir el `Canvas` desde cero — son ~150 líneas de código y se entiende mejor el comportamiento.

---

### 1.7. ¿Cómo permitir edición dinámica del plano (agregar, mover, redimensionar, eliminar zonas) sin recargar?

La arquitectura reactiva de Compose hace esto trivial, independientemente de si los datos vienen solo del API o de Room:

1. **`Flow<List<Zone>>` o `StateFlow<List<Zone>>` desde el `Repository`** → cualquier mutación se refleja automáticamente en la UI.
2. **Estado intermedio en `ViewModel`** para zonas en edición (todavía no persistidas), evitando llamar al API/BD en cada movimiento de drag (escribir solo en `onDragEnd`).
3. **Modos de edición** controlados por un `enum EditMode { VIEW, ADD, MOVE, RESIZE, DELETE }` en el `ViewModel`. El plano cambia su comportamiento de gestos según el modo.

```kotlin
class PlanoViewModel(private val repository: ZoneRepository) : ViewModel() {
    val mode = MutableStateFlow(EditMode.VIEW)
    val zones: StateFlow<List<Zone>> = /* desde repository */

    fun onZoneDragEnd(zone: Zone) = viewModelScope.launch {
        repository.updateZone(spaceId, zone)
    }

    fun addZone(rect: Rect) = viewModelScope.launch {
        repository.createZone(spaceId, Zone(id = 0, rect = rect, …))
    }
}
```

El `Repository` se encarga de los detalles de persistencia (solo API, o API + Room). El `ViewModel` no necesita saberlo.

**Para redimensionar:** dibujar "handles" (cuadrados pequeños) en las esquinas de la zona seleccionada y aplicar `detectDragGestures` sobre cada handle. Cada handle modifica `x`/`y` o `width`/`height` según su posición (top-left, top-right, etc.).

---

### 1.8. Límites de rendimiento con muchas zonas o planos grandes

**Puntos críticos a vigilar:**

- **Número de zonas:** Compose `Canvas` puede dibujar miles de rectángulos sin problema, pero solo si todo se dibuja **dentro de un único `Canvas`** (no un composable por zona). Un composable por zona introduce overhead de medición/layout por cada uno.
- **Recomposición excesiva:** si el estado de pan/zoom está en un `mutableStateOf` que envuelve todas las zonas, cada cambio recompone toda la jerarquía. Usar `graphicsLayer` con lambdas (`graphicsLayer { scaleX = scale }`) en vez de pasar el valor directamente — esto evita la fase de composición y solo redibuja la capa.
- **Viewport culling:** si el plano lógico es enorme (10 000+ zonas), dibujar solo las zonas cuyo `rect` intersecta el viewport visible (calcular el viewport en world-coordinates a partir de `scale` + `offset`).
- **Bitmaps cacheados:** para fondos estáticos costosos (textura del piso, grid), usar `drawWithCache` y un `ImageBitmap` precomputado.
- **`Path` complejos:** si las zonas no son rectángulos sino polígonos, construir los `Path` una sola vez (cachearlos en el `Zone` o con `remember`) y reutilizar.

**Regla práctica:** para los casos típicos del proyecto (decenas a bajos cientos de zonas) ningún truco especial es necesario. La optimización entra a partir de ~1000 zonas o cuando el dispositivo objetivo es de gama baja.

---

### 1.9. ¿Cómo escalar y hacer responsivo el plano en diferentes pantallas?

Tres dimensiones de responsividad:

1. **Tamaño físico del lienzo:** usar `BoxWithConstraints` para conocer `maxWidth`/`maxHeight` y calcular `worldToCanvas = min(maxWidth / worldWidth, maxHeight / worldHeight)` para fit-to-screen manteniendo aspect ratio.
2. **Densidad de pixels:** Compose ya convierte dp → px internamente. Guardar todo en world units (no dp) y solo convertir en el último paso al dibujar. Para grosor de línea visible siempre igual (no afectado por zoom), dividir por `scale` el `strokeWidth`.
3. **Orientación:** recalcular `worldToCanvas` en cada cambio de orientación. Como `BoxWithConstraints` reacciona a cambios, esto pasa automáticamente.

Para tablets y foldables, considerar mostrar un panel lateral con propiedades de la zona seleccionada usando `Material3 Adaptive` — layouts de dos paneles a través de dispositivos, mejor experiencia en plegables/tablets/escritorio.

---

### 1.10. Compose `Canvas` vs. enfoque híbrido con Android Views

**Compose `Canvas` puro:**

✅ Una sola fuente de verdad para la UI
✅ Estado reactivo nativo
✅ Sin overhead de interop
✅ Recomposición selectiva
❌ Para SVGs complejos cargados en runtime hay que parsearlos manualmente o usar Coil
❌ APIs muy específicas de Views (ej. `ImageView.setScaleType(MATRIX)`) hay que reimplementarlas

**Híbrido (Compose + `AndroidView`):**

✅ Permite reutilizar componentes Views existentes (`SVGImageView`, `MapView`, `PhotoView`)
✅ Útil si ya hay una `CustomView` muy optimizada
❌ Los AndroidViews tienen overhead significativo
❌ La sincronización de estado entre Compose y Views requiere `update` callbacks y es propensa a desincronización
❌ Gestos de Compose y de la View pueden competir; hay que decidir quién consume el evento

**Recomendación:** Compose `Canvas` puro. El enfoque híbrido solo se justifica si aparece un requisito específico que Canvas no resuelve bien (típicamente: SVG complejo cargado en runtime, o reutilizar una View muy optimizada que ya existe en el proyecto).

---

## 2. Tabla comparativa final

| Criterio | Compose Canvas | Coil + SVG | AndroidView + AndroidSVG | Canvas tradicional (Views) |
|---|---|---|---|---|
| **Rendimiento** | Excelente (Skia nativo) | Bueno para mostrar; limitado para editar | Aceptable, con overhead de interop | Excelente |
| **Mantenimiento** | API oficial Compose, evoluciona activamente | Activo (Coil 3 estable) | Librería estable pero menos activa | API legacy estable |
| **Documentación** | Oficial, extensa, muchos ejemplos | Buena | Suficiente pero antigua | Muy extensa pero no Compose-first |
| **Integración con resto de Compose** | Nativa | Buena (vía composables) | Forzada (vía `AndroidView`) | Forzada |
| **Soporte de gestos** | Excelente (`pointerInput`) | Hay que ponerle gestos encima | Mezcla complicada de listeners | Listeners de View |
| **Edición dinámica de zonas** | Trivial (Compose state) | Posible pero forzado | Difícil | Posible pero verboso |
| **Consumo del endpoint .NET** | Trivial (datos puros, Retrofit estándar) | Igual | Igual | Igual |
| **Persistencia local opcional (Room)** | Trivial (datos puros) | Igual | Igual | Igual |
| **Curva de aprendizaje del equipo** | Media (gestos avanzados) | Baja | Media (interop) | Alta (XML + custom views) |
| **Recomendación** | ✅ **Elegida** | Alternativa si aparece SVG remoto | Solo último recurso | Descartada |

---

## 3. Conclusión y decisión

**Alternativa seleccionada para el renderizado: Compose `Canvas` puro** (con `drawBehind` o `drawWithCache` según el caso), combinado con:

- `Modifier.pointerInput` + `detectTransformGestures` para pan/zoom del lienzo.
- `Modifier.pointerInput` + `detectDragGestures` por cada zona editable.
- `Modifier.graphicsLayer` para aplicar la transformación visual sin recomponer.
- **Retrofit + Kotlinx Serialization** para consumir el endpoint .NET.
- **`ViewModel` + `StateFlow`** para coordinar modo de edición y estado de la UI.

**Decisión pendiente — capa de persistencia:** queda abierta entre dos opciones (ver sección 1.5). La recomendación técnica por defecto es **empezar por solo API (Opción A)** para el MVP, y agregar Room como caché (Opción B) solo si los requisitos de offline o UX lo justifican. La decisión final la toma el equipo con producto y diseño.

**Justificación del renderizado:**
1. El plano se construye desde datos (zonas en el endpoint .NET, opcionalmente cacheadas), no desde un asset SVG. Esto descarta los caminos SVG/Coil/AndroidView.
2. Compose Canvas es la opción nativa, sin overhead de interop.
3. La integración con gestos avanzados y con el resto del estado Compose es directa.
4. El rendimiento es suficiente para los volúmenes esperados (decenas a cientos de zonas).

**Riesgos identificados y mitigaciones:**

| Riesgo | Mitigación |
|---|---|
| Conflictos de gestos (drag de zona vs. pan global) | Capas separadas de `pointerInput`, uso disciplinado de `change.consume()` |
| Recomposición costosa con muchas zonas | Un solo `Canvas` para todas las zonas + `drawWithCache` para fondos; `graphicsLayer` lambda para transformaciones |
| Curva de aprendizaje en `pointerInput` raw | Comenzar con detectores de alto nivel (`detectTransformGestures`) y bajar a raw solo si es necesario |
| Latencia visible del endpoint .NET en cada apertura (Opción A) | Migrar a Opción B (API + Room) si la UX lo requiere |
| Conflictos de sincronización si se elige Opción B | Política de last-write-wins inicial; revisar si el producto requiere edición concurrente |

---

## 4. Próximos pasos sugeridos

1. Crear una rama `spike/plano-2d-prototipo` y construir un prototipo mínimo: lienzo con 3 rectángulos arrastrables + pan/zoom global + consumo del endpoint .NET (Opción A).
2. Validar el prototipo con el equipo de diseño UX (¿el gesto de pan-mientras-se-arrastra-una-zona se siente natural? ¿la latencia del endpoint es aceptable?).
3. Definir los modos de edición exactos del producto (¿edición libre? ¿solo arrastrar? ¿con grid snap?).
4. **Decidir con producto** si la app requiere modo offline → si sí, abrir issue para implementar la Opción B (API + Room).
5. Cerrar el spike #297 y abrir issues de implementación.

---

## 5. Referencias

- Documentación oficial Jetpack Compose — Multi-touch gestures: `developer.android.com/develop/ui/compose/touch-input/pointer-input/multi-touch`
- Documentación oficial Jetpack Compose — Understand gestures: `developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures`
- Guía oficial de arquitectura Android — capa de datos: `developer.android.com/topic/architecture/data-layer`
- Guía oficial — offline-first: `developer.android.com/topic/architecture/data-layer/offline-first`
- Android Developers Blog — Compose December '25 release (BOM 2025.12.00)
- Retrofit: `square.github.io/retrofit`
- Room: `developer.android.com/training/data-storage/room`
- Coil changelog — soporte SVG en Coil 3
- GitHub topic: `jetpack-compose-canvas`