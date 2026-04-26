# PRD — LPU: Liga Pauper Uruguay

## Problem Statement

La comunidad de jugadores de Magic: The Gathering en formato Pauper de Uruguay carece de una estructura organizada que les permita competir regularmente, acumular un historial de rendimiento, y tener visibilidad sobre el meta del formato. No existe una plataforma centralizada donde los jugadores puedan registrarse, seguir un ranking nacional, y ver estadísticas de torneos. Esto dificulta la consolidación de la comunidad y limita el crecimiento del formato en el país.

## Solution

Construir **LPU - Liga Pauper Uruguay**, una plataforma web (backend-first) que gestione el registro de jugadores, la organización de una temporada anual de torneos mensuales, y el cálculo de un ranking nacional. La plataforma se integra con Melee.gg para importar resultados de torneos sin reinventar la rueda de gestión de brackets. El sitio es mayormente público, permitiendo que cualquier persona consulte rankings, perfiles de jugadores y estadísticas de meta sin necesidad de cuenta.

El modelo está inspirado en la Lega Pauper Italia (LPI), adaptado a la escala y realidad del Uruguay.

## User Stories

### Jugador

1. Como jugador, quiero registrarme en LPU con mi nombre y username de Melee, para que mis resultados de torneos sean reconocidos automáticamente.
2. Como jugador, quiero ver mi perfil público con mi historial completo de torneos, posiciones obtenidas, arquetipos jugados y evolución de ranking a lo largo del tiempo.
3. Como jugador, quiero ver el ranking nacional actualizado de la temporada en curso, para saber mi posición relativa frente a otros jugadores.
4. Como jugador, quiero ver cuántos puntos tengo acumulados y cuáles son mis mejores 10 torneos contabilizados, para entender cómo se calcula mi ranking.
5. Como jugador, quiero ver los próximos torneos programados con fecha, tienda sede y enlace a Melee, para poder inscribirme.
6. Como jugador, quiero ver los resultados completos de torneos pasados, incluyendo posiciones finales y arquetipos jugados por cada participante.
7. Como jugador, quiero buscar el perfil de otro jugador por nombre, para comparar historial y estadísticas.
8. Como jugador, quiero ver qué arquetipos son los más jugados en el meta actual de LPU, para informar mis decisiones de deck.
9. Como jugador, quiero saber si estoy entre los top 8 del ranking para clasificar al Gran Premio LPU y recibir un bye.

### Organizador de torneo

10. Como organizador, quiero autenticarme con mi cuenta de Google para acceder al panel de gestión.
11. Como organizador, quiero registrar un nuevo torneo ingresando el ID/URL del torneo en Melee.gg, la fecha y la tienda sede.
12. Como organizador, quiero importar los resultados de un torneo desde Melee.gg con un solo click, para que el sistema calcule automáticamente los puntos y actualice el ranking.
13. Como organizador, quiero ver qué participantes de Melee no pudieron ser mapeados a un jugador LPU, para resolverlos manualmente.
14. Como organizador, quiero asignar manualmente un participante de Melee a un jugador LPU en caso de que el matching automático falle.
15. Como organizador, quiero ver el historial de torneos que he organizado y su estado (pendiente de importación, importado).
16. Como organizador, quiero agregar un nuevo arquetipo a la lista predefinida cuando aparece un deck nuevo en el meta.

### Administrador

17. Como administrador, quiero autenticarme con Google OAuth y tener acceso completo a todas las funciones del sistema.
18. Como administrador, quiero crear y gestionar temporadas (año, fecha de inicio, fecha de fin).
19. Como administrador, quiero asignar el rol de organizador a un usuario registrado.
20. Como administrador, quiero gestionar la lista de tiendas donde se realizan los torneos (nombre, ciudad, contacto).
21. Como administrador, quiero gestionar la lista completa de arquetipos predefinidos (crear, editar, desactivar).
22. Como administrador, quiero poder corregir o anular la importación de un torneo en caso de error.
23. Como administrador, quiero ver un panel con el estado general de la temporada: torneos jugados, jugadores activos, ranking actualizado.

### Público general

24. Como visitante sin cuenta, quiero ver el ranking de la temporada actual sin necesidad de registrarme.
25. Como visitante, quiero ver el perfil público de cualquier jugador registrado.
26. Como visitante, quiero ver los resultados de torneos pasados.
27. Como visitante, quiero ver estadísticas del meta: arquetipos más jugados, win rates por arquetipo.
28. Como visitante, quiero entender el sistema de puntos y cómo funciona la liga, a través de una página explicativa.

## Implementation Decisions

### Módulos del sistema

**1. Auth Module**
- Autenticación exclusivamente via Google OAuth (Spring Security + OAuth2)
- Tres roles: `ADMIN`, `ORGANIZER`, `PLAYER`
- Los jugadores públicos no requieren autenticación para consultar datos
- Tokens JWT para sesiones de admin y organizadores

**2. Player Module**
- Registro abierto y autoservicio
- Campos: nombre, username de Melee, perfil público
- Expone endpoints públicos de perfil e historial
- El username de Melee es único y se usa para el matching automático de resultados

**3. Season Module**
- Una temporada por año calendario (enero–diciembre)
- Solo puede haber una temporada activa a la vez
- Los torneos se asocian a una temporada al momento de creación

**4. Tournament Module**
- Creado por un organizador, asociado a una tienda y una temporada
- Campos: ID de Melee, fecha, tienda, organizador, estado (PENDING, IMPORTED)
- Un torneo solo puede importarse una vez (idempotencia)

**5. MeleeIntegration Module**
- Dada una URL o ID de torneo de Melee, retorna lista estructurada de resultados: `{ meleeUsername, position, decklistUrl, archetypeName }`
- Encapsula completamente la lógica de fetch/parsing de Melee
- Interfaz simple y testeable con respuestas mockeadas

**6. Result Module**
- Almacena resultados importados: jugador LPU, torneo, posición final, arquetipo
- Lógica de matching: busca jugador LPU por username de Melee; si no encuentra, genera una entrada UNMATCHED para resolución manual
- Calcula puntos por posición según tabla predefinida (configurable por admin)

**7. Archetype Module**
- Lista predefinida de arquetipos gestionada por admin/organizador
- Campos: nombre, activo/inactivo
- Los resultados importados referencian arquetipos de esta lista

**8. Ranking Module**
- Dado un jugador y una temporada, calcula puntos totales tomando los mejores 10 torneos
- Genera el ranking ordenado de todos los jugadores de la temporada
- Lógica pura y sin efectos secundarios: testeable con datos de entrada fijos
- Identifica automáticamente el top 8 (clasificados al Gran Premio LPU con bye)

### Decisiones arquitectónicas
- Backend Spring Boot con API REST, desarrollo primero antes del frontend
- Base de datos PostgreSQL
- Autenticación Google OAuth via Spring Security
- Integración con Melee.gg: fetch bajo demanda al ingresar ID de torneo (no webhooks)
- Integración con Scryfall API para ban list (Fase 2)
- Deploy a definir en una fase posterior

### Tabla de puntos por posición (base LPI — configurable)
| Posición | Puntos |
|---|---|
| 1° | 20 |
| 2° | 15 |
| 3°–4° | 10 |
| 5°–8° | 6 |
| 9°–16° | 3 |
| Resto | 1 |

### Estructura de ranking
- Se toman los mejores 10 torneos del jugador en la temporada
- En caso de empate en puntos, desempate por mayor cantidad de torneos jugados, luego por mejor posición individual

## Testing Decisions

### Qué hace un buen test en este proyecto
- Testea comportamiento externo del módulo, no detalles de implementación
- Usa datos de entrada fijos y verifica salidas esperadas
- Para módulos de integración externa (Melee), mockea el cliente HTTP
- No testea que se llamó a un método específico, sino que el resultado es correcto

### Módulos y enfoque de tests

**Auth Module**
- Test de que endpoints protegidos rechazan requests sin token
- Test de que roles incorrectos reciben 403
- Test de que el flujo OAuth retorna un JWT válido

**Player Module**
- Test de registro exitoso y validación de campos requeridos
- Test de que username de Melee duplicado retorna error
- Test de endpoint público de perfil retorna datos correctos

**Season Module**
- Test de que no pueden coexistir dos temporadas activas
- Test de que crear una temporada con fechas inválidas falla

**Tournament Module**
- Test de que un organizador solo puede gestionar sus propios torneos
- Test de que importar un torneo ya importado retorna error idempotente
- Test de creación con ID de Melee válido e inválido

**MeleeIntegration Module**
- Test con respuesta mockeada de Melee: verifica parsing correcto de posiciones y usernames
- Test de manejo de errores cuando Melee no responde o retorna datos inesperados
- Este módulo es el más crítico para testear en aislamiento con mocks

**Result Module**
- Test de matching automático: dado username de Melee existente en LPU, resultado se asigna correctamente
- Test de matching fallido: genera entrada UNMATCHED
- Test de asignación manual posterior
- Test de cálculo de puntos por posición según tabla configurada

**Archetype Module**
- Test de CRUD de arquetipos
- Test de que un arquetipo inactivo no aparece en opciones de resultados

**Ranking Module**
- Test unitario puro: dado un conjunto fijo de resultados de torneo para un jugador, verifica que se toman los 10 mejores y el total es correcto
- Test de jugador con menos de 10 torneos: se toman todos
- Test de desempate entre jugadores con igual puntaje
- Test de identificación correcta del top 8

## Out of Scope

- Gestión del formato del torneo (brackets Swiss, eliminación doble) — delegado completamente a Melee.gg
- Validación de legalidad de decklists (cartas baneadas) — Fase 2
- Página de referencia de ban list vía Scryfall API — Fase 2
- Estadísticas avanzadas de meta (win rates por arquetipo) — Fase 2
- Gran Premio LPU con sistema de byes — Fase 2
- Evolución histórica del ranking (gráficos) — Fase 2
- Estructura regional / multi-departamento — Fase futura
- Aplicación móvil
- Notificaciones (email, WhatsApp)
- Integración con redes sociales

## Further Notes

- El modelo está basado en la Lega Pauper Italia (LPI). El reglamento oficial de referencia está disponible en: https://www.mtgforfun.cz/storage/public/pdf/LPI-Rules.pdf
- El Gran Premio LPU es el torneo cumbre anual: abierto a todos los jugadores, con byes para los top 8 del ranking de temporada. El nombre es provisorio.
- La estructura es nacional centralizada en esta fase, pero el modelo de datos debe contemplar la posibilidad futura de regiones/departamentos sin requerir una migración mayor.
- Las tiendas (game stores) rotan como sede de los torneos mensuales — no hay sede fija.
- Melee.gg es gratuito para torneos de hasta 128 jugadores, lo cual cubre holgadamente el tamaño inicial de LPU.