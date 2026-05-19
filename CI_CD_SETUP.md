# CI/CD & Deployment Instructions

Este documento reúne los pasos prácticos para poner en funcionamiento el workflow de CI/CD creado en `.github/workflows/ci.yml`, desplegar la imagen Docker en un clúster Kubernetes local y las acciones necesarias en el entorno (runner y secrets). Incluye las alternativas principales y los cambios que debes realizar en los ficheros del repositorio.

ARCHIVOS AÑADIDOS
- `Dockerfile` — imagen base y entrypoint.
- `k8s/deployment.yaml` — manifiesto Deployment (contiene placeholder para la imagen).
- `k8s/service.yaml` — Service tipo NodePort.
- `.github/workflows/ci.yml` — workflow que compila, construye imagen, opcionalmente la publica en Docker Hub y desenrola en un self-hosted runner.

TODOs y lugares donde debes introducir tus datos
------------------------------------------------
- `.github/workflows/ci.yml`
  - `env.IMAGE_NAME`: reemplaza `<TODO_DOCKERHUB_USER>/springuma` por `TU_USUARIO/springuma` si vas a usar Docker Hub.
  - Secrets opcionales a añadir (Repository → Settings → Secrets):
    - `DOCKERHUB_USERNAME` — tu usuario de Docker Hub.
    - `DOCKERHUB_TOKEN` — token o contraseña para Docker Hub.
  - `deploy` job `runs-on`: actualmente es `[self-hosted, linux, k8s-deployer]`. Asegúrate de que exista un self-hosted runner con esa etiqueta, o modifica la etiqueta a la que utilices.

- `k8s/deployment.yaml`
  - El campo `image: <DOCKER_IMAGE_PLACEHOLDER>` se sustituye automáticamente por el workflow en tiempo de ejecución.
  - Si quieres probar localmente, reemplaza el placeholder por `TU_USUARIO/springuma:local` o `springuma:local` según el flujo.

- `Dockerfile`
  - El Dockerfile copia `target/*.jar` como `app.jar`. Asegúrate de que el build Maven produzca el jar en `target/` (comando por defecto: `./mvnw -DskipTests package`).

Estrategias de despliegue (elige una)
------------------------------------
Opción A — Self-hosted runner + Docker Hub (remoto)
- Flujo: CI (github-hosted) compila y sube imagen a Docker Hub. Deploy se ejecuta en un self-hosted runner que tiene kubectl y aplica los manifests.
- Requisitos:
  - Secrets DOCKERHUB_USERNAME/DOCKERHUB_TOKEN en GitHub.
  - Self-hosted runner registrado y etiquetado (p.ej. `k8s-deployer`).
  - El cluster k8s debe poder tirar de Docker Hub (o el runner debe hacer `docker pull`).

Opción B — Self-hosted runner + kind/minikube en la misma máquina (recomendado local)
- Flujo: el job de deploy construye la imagen en el runner y la carga en kind/minikube (p.ej. `kind load docker-image`) sin pasar por Docker Hub.
- Requisitos:
  - Runner registrado en la máquina que ejecuta el clúster.
  - kind o minikube instalado en esa máquina.
  - Modificar el workflow para construir la imagen en el runner y cargarla en el clúster (puedo generar la variante si me confirmas el tipo de clúster).

Pasos previos locales recomendados
---------------------------------
1. Compilar la aplicación y verificar el JAR:
   - `./mvnw -DskipTests package`
   - `ls target/*.jar`

2. Probar la imagen Docker localmente:
   - `docker build -t tuusuario/springuma:local .`
   - `docker run --rm -p 8080:8080 tuusuario/springuma:local`
   - Verifica `http://localhost:8080` (o los endpoints de la app).

3. Desplegar inicialmente en Kubernetes (test manual):
   - Sustituye `<DOCKER_IMAGE_PLACEHOLDER>` en `k8s/deployment.yaml` si es necesario.
   - `kubectl apply -f k8s/deployment.yaml`
   - `kubectl apply -f k8s/service.yaml`
   - `kubectl -n default rollout status deployment/springuma`

Registrar un self-hosted runner (resumen)
----------------------------------------
1. En GitHub: Settings → Actions → Runners → New self-hosted runner.
2. Selecciona sistema operativo (Linux) y sigue las instrucciones para descargar y ejecutar el runner.
3. Ejecuta el runner como servicio en la máquina (script que te proporciona GitHub cuando lo creas).
4. Añade la etiqueta `k8s-deployer` al registrar el runner o edita `.github/workflows/ci.yml` para usar la etiqueta que prefieras.

Comandos útiles para el runner
-----------------------------
- Verificar kubectl:
  - `kubectl version --client` (en runner)
- Verificar docker (si se usa docker en runner):
  - `docker version`
- (Kind) Cargar imagen creada en el runner al cluster kind:
  - `kind load docker-image tuusuario/springuma:TAG --name <kind-cluster-name>`

Prueba del workflow
-------------------
1. Haz un cambio trivial en la rama `main` y push (por ejemplo actualizar README).
2. Observa en Actions: `build-and-push` debe compilar y construir la imagen.
3. Si configuraste secrets, la imagen se subirá a Docker Hub.
4. Job `deploy` debe ejecutarse en el self-hosted runner y aplicar los manifests; revisa `kubectl get pods` y `kubectl logs` si hay problemas.

Puntos de fallo comunes y soluciones
----------------------------------
- Error: runner no encontrado / etiquetas no coinciden → registra el runner o cambia `runs-on`.
- Error: docker daemon no disponible → instala Docker en el runner o usa Jib para push.
- Error: ImagePullBackOff → asegúrate que la imagen está en Docker Hub o cargada en el cluster (kind load docker-image).
- Error: kubectl no configurado → añade `KUBECONFIG` al runner (o como secreto) o configura kubeconfig en la máquina runner.

¿Qué puedo hacer ahora por ti?
- Generar una variante del workflow optimizada para `kind` o `minikube` (loader-local-image) — dime qué tecnología usas.
- Añadir instrucciones completas y comandos que crear automáticamente el runner con etiquetas.
- Ejecutar un `./mvnw test` y reportar resultados en este entorno (si lo deseas).
