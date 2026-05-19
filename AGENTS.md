# AGENTS.md

Propósito
---------
Análisis técnico del repositorio "SpringUMA" orientado a asistentes automáticos y mantenedores humanos. Contiene hallazgos, riesgos, recomendaciones y acciones concretas (archivos a modificar, commits y workflows) para mejorar la calidad, seguridad y mantenibilidad del proyecto.

Metodología
-----------
- Revisión de artefactos del repositorio: `README.md`, `pom.xml`, `JAVA_CODING_GUIDELINES.md`, `GIT_GUIDELINES.md`.
- Lectura del código fuente en `src/main/java/com/uma/example/springuma/` y de los tests en `src/test/java/` y `src/test/resources/`.
- Análisis estático (no se han ejecutado compilaciones ni pruebas en el entorno de este análisis).

Resumen del proyecto
--------------------
Spring Boot REST API para un sistema de historia clínica con entidades: Medico, Paciente, Imagen e Informe. Implementa endpoints REST para CRUD y manejo de imágenes (almacenamiento comprimido) y un flujo de predicción AI (actualmente simulado).

Detectados: archivos de directrices
---------------------------------
- JAVA coding guide: `JAVA_CODING_GUIDELINES.md` presente en raíz — contiene reglas detalladas (uso de record, sealed, Optional, estilo, Javadoc, Google Java Format, etc.).
- GIT guide: `GIT_GUIDELINES.md` presente — estipula Conventional Commits, commits atómicos y ejemplos.

Hallazgos principales (prioridad por impacto)
--------------------------------------------
1) Inconsistencia README / workflows
   - `README.md` menciona `.github/workflows/ci.yml` pero no existe la carpeta `.github/` ni workflows en local.

2) Credenciales y configuración
   - `ImagenAPIPredictor.java` contiene un placeholder de TOKEN. Debe usarse variable de entorno o property y documentarse.

3) Manejo de excepciones
   - `ImageUtils.compressImage` y `decompressImage` silencian excepciones; esto puede ocultar errores funcionales y dificultar debugging.
   - `ImagenController` usa `e.printStackTrace()` en catch; reemplazar por SLF4J Logger.

4) Lógica de predicción
   - `ImagenService.getNewPrediccion` tiene la integración con la API externa comentada y usa valores aleatorios. Esto está bien para pruebas locales pero debe documentarse y controlarse por configuración.

5) Validación de entradas
   - `uploadImage` acepta un objeto `Paciente` y lo asocia sin verificar existencia en BD; riesgo de inconsistencias.

6) Tests y recursos
   - Tests de integración usan recursos `src/test/resources/healthy.png` y `no_healthty.png` — deben incluirse y garantizarse en CI.

Recomendaciones prácticas y acciones sugeridas (ordenadas)
-------------------------------------------------------
Acciones de prioridad alta (aplicar primero, atómicas)
1. Mover token fuera del código
   - Leer `HUGGINGFACE_TOKEN` desde variable de entorno (preferido) o desde propiedad `huggingface.token`.
   - Archivos: `ImagenAPIPredictor.java`, documentación en `README.md`.
   - Commit: chore(config): read huggingface token from env/property

2. Propagar errores en ImageUtils
   - No silenciar excepciones; usar try-with-resources y lanzar IOException para que los tests detecten fallos.
   - Archivos: `ImageUtils.java`, adaptar llamadas en `ImagenService`.
   - Commit: fix(utils): propagate IO exceptions from ImageUtils

3. Logging en controllers
   - Reemplazar `printStackTrace()` por `Logger` (SLF4J) y devolver respuestas HTTP claras.
   - Archivos: `ImagenController.java` (y otros controllers si aplican).
   - Commit: style(controller): use SLF4J logger instead of printStackTrace

Acciones de prioridad media
4. Validar existencia de Paciente en uploadImage
   - Usar `RepositoryPaciente` para validar `paciente.getId()` antes de asignar.
   - Commit: fix(service): validate paciente exists before attaching image

5. Documentar la integración AI
   - Añadir sección en `README.md` explicando cómo habilitar la llamada a HuggingFace y añadir instrucciones de CI (secret `HUGGINGFACE_TOKEN`).
   - Commit: docs: document huggingface integration and required env

Acciones de prioridad baja (opcional)
6. Añadir formateador y reglas (Google Java Format / Checkstyle)
   - Integrar plugin en `pom.xml` y añadir configuración mínima.
   - Commit: chore(format): add google-java-format/checkstyle config

7. Añadir GitHub Actions CI
   - Crear `.github/workflows/ci.yml` que ejecute build (JDK 21), unit tests y integration tests (Failsafe), publique reportes y suba artefactos.
   - Añadir badges en `README.md`.
   - Commit: ci: add GitHub Actions workflow for build and tests

Propuesta de commits atómicos (ejemplos)
------------------------------------
- chore(config): read huggingface token from env/property
- fix(utils): propagate IO exceptions from ImageUtils
- style(controller): use SLF4J logger instead of printStackTrace
- fix(service): validate paciente exists before attaching image
- docs: document huggingface integration and CI secrets
- ci: add GitHub Actions workflow for build and tests

Notas sobre implementación
-------------------------
- Token injection: mínimo y no invasivo (leer System.getenv("HUGGINGFACE_TOKEN") o System.getProperty("huggingface.token")) vs. opción Spring-native (@Value) que requiere convertir la clase en bean. Recomiendo el enfoque mínimo para cambios rápidos.
- Cambio en ImageUtils requiere propagar IOException; adaptar servicios/controllers que llamen a estos métodos para declarar/exponer el error.
- Mantener tests en verde: ejecutar `./mvnw test` y `./mvnw verify` localmente después de cada commit.

Checklist antes de commitear
---------------------------
- [ ] Codigo formateado (Google Java Format)
- [ ] Tests pasan (`mvn test`)
- [ ] Commits atómicos y según `GIT_GUIDELINES.md`
- [ ] Documentación de cambios en `README.md` o `AGENTS.md`

Próximos pasos recomendados (si autorizas cambios)
------------------------------------------------
1. Aplico los cambios de prioridad alta uno a uno y muestro los patches antes de aplicar (recomendado).
2. Creo un workflow CI básico y lo añado en `.github/workflows/ci.yml` (separado commit).
3. Actualizo README con instrucciones para `HUGGINGFACE_TOKEN` y badges (separado commit).
