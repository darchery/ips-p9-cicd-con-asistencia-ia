# Informe: Revisión frente a JAVA_CODING_GUIDELINES.md

Objetivo
--------
Evaluar el código fuente del proyecto contra las reglas expresadas en `JAVA_CODING_GUIDELINES.md` y producir un informe técnico con hallazgos, severidades, referencias y recomendaciones de corrección.

Metodología
-----------
- Búsqueda y lectura estática de los ficheros en `src/main/java`.
- Identificación de patrones que violan las guías (uso de excepciones genéricas, silenciamiento de errores, printStackTrace, System.out, manejo de recursos, falta de validaciones y documentación mínima).

Resumen ejecutivo
-----------------
Se han detectado diversas violaciones a las guías en áreas críticas: manejo de excepciones, logging, utilidades de I/O y validación de datos. Ninguna requiere cambios de diseño grandes; la mayoría pueden resolverse con parches pequeños y atómicos.

Hallazgos (ordenados por prioridad)
----------------------------------
1) Uso de `printStackTrace()` y capturas de `Exception` genérica
   - Archivos/lineas:
     * src/main/java/com/uma/example/springuma/controller/ImagenController.java — lines ~44–71 (varias llamadas a e.printStackTrace()).
     * src/main/java/com/uma/example/springuma/controller/InformeController.java — lines ~39–51.
     * src/main/java/com/uma/example/springuma/controller/MedicoController.java — lines ~33, ~43, ~61.
     * src/main/java/com/uma/example/springuma/controller/PacienteController.java — lines ~40, ~50, ~67.
   - Por qué importa: las guías recomiendan usar SLF4J Logger y evitar capturar `Exception` de forma global. Usar printStackTrace obliga a salida a stderr y pierde estructuración y contexto.
   - Riesgo: diagnóstico difícil, pérdida de trazas estructuradas en logs y mala experiencia en producción.
   - Recomendación: reemplazar por `private static final Logger logger = LoggerFactory.getLogger(X.class);` y `logger.error("mensaje contextual", e);`. Capturar excepciones concretas cuando sea posible y devolver respuestas HTTP apropiadas (4xx/5xx).

2) ImageUtils silencia excepciones y no usa try-with-resources
   - Archivo: src/main/java/com/uma/example/springuma/utils/ImageUtils.java
   - Detalle: compressImage y decompressImage capturan `Exception ignored` y devuelven bytes posiblemente incompletos. Además no usan try-with-resources para ByteArrayOutputStream.
   - Por qué importa: según las guías, recursos AutoCloseable deben usarse con try-with-resources y no se deben ocultar excepciones; es preferible propagar un `IOException` o lanzar una excepción específica.
   - Riesgo: datos corruptos, tests que pasan silenciosamente, dificultad para depurar errores de compresión.
   - Recomendación: reescribir métodos para usar try-with-resources y propagar `IOException` (declarar `throws IOException`) o lanzar unchecked con mensaje claro. Asegurarse de que llamadas (ImagenService.downloadImage, uploadImage) gestionen la excepción.

3) Uso de `System.out.println` en código de producción
   - Archivos/lineas:
     * src/main/java/com/uma/example/springuma/model/InformeService.java — llamadas a System.out.println
     * src/main/java/com/uma/example/springuma/model/ImagenService.java — (comentado) System.out.println
     * src/main/java/com/uma/example/springuma/controller/MedicoController.java y PacienteController.java — System.out.println
   - Por qué importa: guías exigen usar Logger en lugar de System.out.
   - Recomendación: sustituir por logger.debug/info según el contexto.

4) ImagenAPIPredictor: manejo de secretos y excepciones genéricas
   - Archivo: src/main/java/com/uma/example/springuma/model/ImagenAPIPredictor.java
   - Detalle: TOKEN hardcodeado como "Bearer USE_TOKEN_HERE" y método `query` y `processResponse` lanzan `Exception` genérica.
   - Riesgo: malas prácticas de seguridad (aunque placeholder), y uso de `throws Exception` que va contra las guías de excepciones específicas.
   - Recomendación: mover token a variable de entorno (`HUGGINGFACE_TOKEN`) o propiedad; cambiar firmas para lanzar `IOException` o `ApiException` específico; evitar `throws Exception` en métodos API.

5) Predicción simulada y falta de configuración
   - Archivo: src/main/java/com/uma/example/springuma/model/ImagenService.java
   - Detalle: getNewPrediccion usa Math.random() y el llamado real a ImagenAPIPredictor está comentado.
   - Recomendación: documentar claramente en README que la integración está deshabilitada por defecto y ofrecer una flag/propiedad `predictor.enabled` para activar llamadas externas.

6) Validación de entidades recibidas en endpoints
   - Archivo: src/main/java/com/uma/example/springuma/model/ImagenService.java (uploadImage) y controller handling multipart
   - Detalle: uploadImage recibe un objeto `Paciente` y lo asigna sin comprobar existencia en BD (RepositoryPaciente.existsById).
   - Recomendación: validar `paciente.getId()` (si existe) y consultar repositorio; si no existe, devolver 400 o crear el paciente explícitamente con control.

7) Javadoc y documentación pública insuficiente
   - Observación: métodos públicos carecen de Javadoc explicativo, parámetros y excepciones.
   - Recomendación: añadir Javadoc en APIs públicas según la guía.

Cambios propuestos (por prioridad y atómicos)
-------------------------------------------
Propuesta A — alta prioridad (3 commits pequeños):

1) style(controller): use SLF4J logger instead of printStackTrace
   - Reemplazar `e.printStackTrace()` por `logger.error("Descripción", e);` en todos los controladores apuntados. Capturar excepciones más específicas si procede.

2) fix(utils): propagate IO exceptions from ImageUtils
   - Cambiar firmas de `compressImage` y `decompressImage` a `throws IOException`.
   - Utilizar try-with-resources para ByteArrayOutputStream.
   - Ajustar llamadas en ImagenService (el método uploadImage ya declara throws IOException; downloadImage deberá declarar o envolver la excepción y controller manejarla).

3) chore(config): read huggingface token from env/property
   - Reemplazar el TOKEN hardcodeado por lectura de `System.getenv("HUGGINGFACE_TOKEN")` con fallback a `System.getProperty("huggingface.token")`.
   - Evitar `throws Exception` en processResponse; definir `throws IOException` o `ApiException`.

Propuesta B — media prioridad (2 commits):
4) fix(service): validate paciente exists before attaching image
   - Inyectar RepositoryPaciente en ImagenService y comprobar existencia cuando se reciba un paciente con id.

5) style(logging): replace System.out with logger
   - Sustituir System.out.println por logger.* en InformeService, ImagenService y controladores.

Propuesta C — baja prioridad (opcional)
6) docs: document predictor configuration and test resources
7) ci: add GitHub Actions workflow (template)

Pruebas y verificación
----------------------
- Tras aplicar cada commit de la Propuesta A se debe ejecutar `./mvnw test` y `./mvnw verify`.
- Verificar que los tests de integración encuentran los recursos en `src/test/resources`.

Riesgos y notas
---------------
- Cambiar ImageUtils para lanzar IOException requiere propagar cambios (ImagenService, controllers). Es un cambio pequeño de API interna pero necesario para que las fallas no queden silenciadas.
- Mover el token fuera del código no afecta tests porque la llamada a la API está comentada; si se habilita, es necesario proporcionar el secret en CI.

Siguientes pasos propuestos
--------------------------
1. Si estás de acuerdo, aplico los 3 commits de la Propuesta A uno a uno y te muestro cada patch antes de aplicarlo.
2. Después aplico Propuesta B (validaciones y logging) y ejecuto los tests.

Decisión requerida
------------------
- ¿Autorizas que aplique los cambios de la Propuesta A ahora? (responde "sí" o "no").
- Para la inyección del token, ¿prefieres la opción "env" (System.getenv fallback) o "spring" (@Value en bean)?
