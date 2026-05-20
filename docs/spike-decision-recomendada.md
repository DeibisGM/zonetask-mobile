# Decisión Recomendada — Spike #297

## Resumen

Para la primera iteración del plano interactivo 2D, se recomienda:

- **Renderizado:** Compose `Canvas` puro.
- **Gestos:** `pointerInput` con `detectTransformGestures` (pan y zoom del lienzo) y `detectDragGestures` (arrastre de zonas individuales).
- **Persistencia:** **Solo API** — consumo directo del endpoint .NET con Retrofit. **Sin Room** por ahora.

---

## Por qué Compose Canvas

El plano se construye desde una lista de zonas que vienen del backend (no es un SVG pre-dibujado). Esto descarta las opciones basadas en cargar archivos SVG (Coil, AndroidSVG) porque no aportan nada: la app igual tendría que dibujar las zonas encima.

Compose `Canvas` es la opción nativa, está soportado oficialmente por Google, y se integra de forma directa con el resto del estado de Compose (`State`, `Flow`, `ViewModel`). No hay overhead de interoperar con Android Views.

---

## Por qué empezar sin Room

Room es una librería para guardar datos localmente en SQLite. Es muy útil, pero **solo si la app realmente necesita esos datos cacheados localmente**. En este momento no hay evidencia de que se necesite, y agregarla "por si acaso" trae costos concretos:

1. **El backend ya existe y es la fuente de verdad.** Duplicar los datos en local introduce el problema clásico de sincronización: ¿qué pasa si la copia local difiere de la del servidor? ¿quién gana? ¿cuándo refrescar? Todas esas preguntas requieren código adicional y son fuente de bugs.

2. **Arquitectura más simple = MVP más rápido.** Solo API significa: Retrofit → ViewModel → Compose. Tres capas. Agregar Room implica: Retrofit + Room → Repository con lógica de sync → ViewModel → Compose. Más capas, más código, más superficie para errores.

3. **No hay requisitos confirmados de modo offline.** Si producto no ha pedido explícitamente que la app funcione sin conexión, asumir que sí lo necesitará es sobre-ingeniería. Es preferible esperar a tener el requisito real y diseñar la solución según ese caso de uso concreto.

4. **Migrar después es barato.** La clave está en cómo se estructura el código desde el inicio: si la UI consume un `Repository` (no directamente a Retrofit), agregar Room más adelante es cambiar la implementación del `Repository`, sin tocar el `ViewModel` ni la UI. Es decir, **la decisión de no usar Room ahora no te encierra; te deja la puerta abierta**.

---

## Cuándo sí migrar a Room

La migración a "API + Room" (offline-first) se justifica si en algún momento aparece alguno de estos escenarios:

- **Producto confirma que la app debe funcionar sin conexión.** Por ejemplo: técnicos de campo que entran a sitios sin señal, o uso en zonas con conectividad intermitente.
- **La latencia del endpoint genera mala UX.** Si cada vez que el usuario abre el plano tiene que esperar 2-3 segundos a que cargue, eso se siente lento. Con caché local, la app abre instantáneamente con los datos previos y refresca en segundo plano.
- **Se necesita edición muy fluida.** Si los usuarios mueven zonas constantemente y el round-trip al servidor introduce lag perceptible, los "optimistic updates" (escribir en local primero, sincronizar después) resuelven eso. Y los optimistic updates son mucho más fáciles con Room que sin él.

Si pasa algo de esto, se abre una issue para agregar Room **encima** del repositorio existente. Es una refactorización acotada, no una reescritura.

---

## Stack mínimo del MVP

| Capa | Librería / API | Para qué |
|---|---|---|
| UI | Jetpack Compose | Composables del plano y de la app |
| Renderizado del plano | Compose `Canvas` + `pointerInput` | Dibujar zonas y manejar gestos |
| Estado de UI | `ViewModel` + `StateFlow` | Coordinar la lógica y exponer estado a la UI |
| Red | Retrofit + OkHttp + Kotlinx Serialization | Consumir el endpoint .NET |
| Inyección de dependencias | Hilt (o manual) | Conectar las capas |

---

## Recordatorio importante

Esta es una **recomendación técnica**, no una decisión final del equipo. La elección entre "solo API" y "API + Room" depende de los requisitos reales del producto, que solo el equipo (con producto y diseño) puede confirmar. Lo que el spike aporta es: el contexto técnico, las consecuencias de cada opción, y un punto de partida razonable mientras se confirman los requisitos.
