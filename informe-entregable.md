# Informe práctica 9: CI/CD con la asistencia de IA generativa

## Un informe (documento en pdf) que responda brevemente a estas cuestiones sobre los 8 puntos planteados

### 1. ¿Ves alguna inconsistencia u error en el fichero AGENTS.md? ¿Está claro su contenido?
- En sí su contenido está bien, hace una presentación general de proyecto. Pero también ha destacado muchísimos a cambios a mejorar en el proyecto base, incluyéndolos en el AGENTS.md.
### 2. Los cambios en el README, ¿son superficiales o ha hecho cambios profundos?
 - Ha hecho cambios superficiales, aclarando la url que redirige al SpringDoc y añadiendo información a modo de disclaimer sobre el AI Predictor(integración externa deshabilitada por defecto) y los Test resources(recordatorio para incluir lo necesarios para hacer funcionar la integraciones en CI). 
### 3. Haz un resumen a grosso de la respuesta de la IAG. ¿Sugiere muchos cambios? ¿Son todos aplicables o sólo algunos?
 - Los cambios que ha aplicado son razonables, parches pequeños y atómicos. Sugiere 7 cambios(bastantes pero pequeños). Y prácticamente todos son aplicables, los que están relacionados con el uso de la API para hacer la predicción no los voy  aplicar.
 - Lista de cambios sugeridos
    1. style(controller): use SLF4J logger instead of printStackTrace
    2. fix(utils): propagate IO exceptions from ImageUtils
    3. chore(config): read huggingface token from env/property
    4. Inyectar RepositoryPaciente en ImagenService y comprobar existencia cuando se reciba un paciente con id.
    5. style(logging): replace System.out with logger
    6. docs: document predictor configuration and test resources
    7. ci: add GitHub Actions workflow (template)
### 4. Indica qué cambios has aceptado.
 - He aceptado, todos menos el 3, 4 y  7. Este es el listado:
 1) ImageUtils: no silenciar excepciones
   - Reescrito para usar try-with-resources y propagar IOException.
   - Cierra correctamente Deflater/Inflater con end().
   - Archivo modificado: src/main/java/com/uma/example/springuma/utils/ImageUtils.java
 2) Controllers: reemplazo de printStackTrace por SLF4J Logger
   - Añadí LoggerFactory y logger en:
     - ImagenController.java
     - MedicoController.java
     - PacienteController.java
     - InformeController.java
   - Sustituí llamadas a e.printStackTrace() por logger.error(...) con contexto.
   - Reemplacé System.out.println(...) por logger.debug(...) o eliminé salidas innecesarias.
 3) ImagenService / InformeService: eliminar throws Exception innecesario y adaptar a IOException
   - ImagenService.getNewPrediccion: dejó solo throws IOException (la llamada real está deshabilitada; mantenemos la firma compatible para cuando se habilite).
   - InformeService: getNewPrediccion, addInforme y updateInforme ahora declaran solo throws IOException.
   - downloadImage ahora declara throws IOException, y el controlador lo maneja.
 4) ImagenController.downloadImage ahora captura IOException y devuelve 500 con mensaje claro.
   - Manejo de errores con logger.
 5) Eliminé prints de depuración comentados en ImagenService/InformeService y documenté que la integración externa está deshabilitada.

### 5. Igual que el punto 3: indica el número de cambios que se sugieren e indica cuáles has decidido aplicar.
 - Cambios sugeridos
    1. Validar existencia de Paciente en uploadImage
    2. Refactorizar ImagenAPIPredictor para manejo de errores específicos
    3. Añadir formateador y checks (Checkstyle / google-java-format)
    4. Añadir workflow GitHub Actions (CI)
 - Voy a aplicar los puntos 1 y 2.
### 6. Indica si el uso de la IAG te ha ayudado a mejorar los workflows de la práctica 7 e intenta cuantificar, si es factible, el tiempo que te habrías ahorrado respecto al desarrollo original de la misma.
 - No tanto a mejorar(porque no sigue muy buenas prácticas en algunos archivos), si no a automatizar  el procesos de creación de los workflows con github actions. Y evidentemente en tiempo físico que se tarda en realizar estos cambios es mucho mayor al que se tarda con este proceso usando IAG.
### 7. Comprueba e indica si se han incluido las llamadas para incluir los badges en el fichero README.md
 - Si, se ha incluido correctamente
### 8. Comprueba si los workflows se ejecutan de forma correcta y, si no, si usando la IAG enviándole los errores producidos es capaz de repararlos.
 - Los worksflows se ejecutan de forma correcta. He tenido algunos de problemas de configuración en los .yml para que detecte las varibales secrets de mi token y usuario de hub docker, pero ha conseguido solucionarlo. También he tenido bastantes problemas a la hora de configurar el deploy de kubernetes, pero tras varios intentos ha podido solucionarlos.

Adicionalmente, indica qué herramienta has usado y tu valoración al aplicarla al ejercicio.
  - He usado la aplicación de terminal de código libre OpenCode, usando GPT-5-mini. La verdad que he tenido bastante cuidado con los prompts y he usado menos del 40% de uso diario. Lo único es que este modelo es muy "verboso", se explica demasiado, a veces innecesario. Yo estoy acostumbrado a usar Claude Haiku 3.5(que viene con github education).
  
