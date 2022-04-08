def SERVICE_NAME = "invoice-service"
pipeline {
    agent any
    environment {
        PROD_URL = 'http://localhost:3000' 
        QA_URL = 'http://localhost:3020' 
        DOCKER_REGISTRY = 'amilcarm11' 
        // REGISTRY_URL = 'https://harbor.tallerdevops.com/' 
    }
    // Buscar cambios en el Repositorio de Git cada 5 mins.
    triggers { pollSCM('H/5 * * * *') }
    stages {
        stage("Init") {
            steps {
                // Obtener el commit message.
                script {
                    env.GIT_COMMIT_MSG = sh (script: "git log -1 --pretty=%B ${env.GIT_COMMIT}", returnStdout: true).trim()
                }

                // Determinar tag de la imagen de Docker
                script {
                    IMAGE_TAG = 'unknown'
                    IMAGE_TAG_ALT = null
                    TRIGGER_SOURCE = env.TAG_NAME ? "tag *${env.TAG_NAME}*" : "branch *${env.BRANCH_NAME}*"
                    SHORT_COMMIT_HASH = "${env.GIT_COMMIT[0..7]}"

                    // Git tags
                    if(env.TAG_NAME != null) {
                        IMAGE_TAG = "${env.TAG_NAME}"
                    }
                    // Rama develop
                    else if(env.BRANCH_NAME == 'develop') {
                        IMAGE_TAG = "develop-${SHORT_COMMIT_HASH}"
                        IMAGE_TAG_ALT = "develop"
                    }
                    // Rama master
                    else if(env.BRANCH_NAME == 'master') {
                        IMAGE_TAG = "master-${SHORT_COMMIT_HASH}"
                        IMAGE_TAG_ALT = 'latest'
                    } else {
                        // Ramas de feature, release, hotfix, bugfix, support
                        def matcher = (env.BRANCH_NAME =~ /(feature|release|hotfix|bugfix|support)\/(\S+)/)
                        def branch_suffix = matcher ? matcher[0] : null
                        if (branch_suffix != null) {
                            def branch_type = branch_suffix[1] == 'release' ? 'pre' : branch_suffix[1]
                            IMAGE_TAG = branch_type + "-" + branch_suffix[2]
                        }
                    }
                    
                    // Definir el nombre completo de la imagen de Docker.
                    IMAGE_FULL_NAME = "${DOCKER_REGISTRY}/${SERVICE_NAME}:${IMAGE_TAG}"
                    def IMAGE_FULL_NAME_ALT = "${DOCKER_REGISTRY}/${SERVICE_NAME}:${IMAGE_TAG_ALT}"
                    DOCKER_IMAGES = IMAGE_FULL_NAME + (IMAGE_TAG_ALT == null ? '' : "\n${IMAGE_FULL_NAME_ALT}")
                }

                // Notificar inicio de Pipeline, la rama, la imagen de Docker, y el commit message
                slackSend message: "Pipeline started: *<${env.BUILD_URL}|${SERVICE_NAME} #${env.BUILD_NUMBER}>* for ${TRIGGER_SOURCE} \n\nChanges: ```${env.GIT_COMMIT_MSG}```\nDocker Images: \n```${DOCKER_IMAGES}```"
            }
        }
        stage("Compile") {
            steps {
                script {
                    sh "./mvnw dependency:go-offline"
                    sh "./mvnw clean package -DskipTests"
                    sh "cd target && java -Djarmode=layertools -jar *.jar extract"
                }
            }
        }
        stage("Unit Tests") {
            steps {
                script {
                    echo "Correr pruebas automáticas..."
                    sh "./mvnw test"
                }
            }
        }
        stage("Docker Image") {
            // Se construye imagen de Docker para las ramas de git-flow 
            //      (master, develop, feature, release, hotfix, bugfix, release, support)
            // y para las tags de git, siempre que cumplan las convenciones de nombre para Docker Image tag.
            when { 
                anyOf { 
                    branch "master";
                    branch "develop";
                    branch pattern: "(feature|release|hotfix|bugfix|support)/(\\S+)", comparator: "REGEXP";
                    tag pattern: "[\\w][\\w.-]{0,127}", comparator: "REGEXP";
                } 

            }
            steps {
                echo "Crear y taguear imagen de Docker: ${IMAGE_FULL_NAME}"
                script {
                    // withDockerRegistry([credentialsId: 'harbor-amilcar', url: "${REGISTRY_URL}"]) {
                    withDockerRegistry([credentialsId: 'dockerhub-amilcar']) {
                        // Crear y publicar la imagen de Docker
                        DOCKER_IMAGE_BUILT = docker.build "${IMAGE_FULL_NAME}"
                        DOCKER_IMAGE_BUILT.push()
                        
                        // La misma imagen puede ser publicada bajo otro nombre también (ej. 'develop', o 'latest')
                        if(IMAGE_TAG_ALT != null) [
                            DOCKER_IMAGE_BUILT.push(IMAGE_TAG_ALT)
                        ]
		            }
	            }
            }
            post {
                failure {
                    slackSend color: "error", message: "Error con la imagen :\n${IMAGE_FULL_NAME}"
                }
            }
        }
        stage("Deploy QA") {
            when { branch 'develop' }
            steps {
                withKubeConfig([credentialsId: 'kube-config', serverUrl: "https://192.168.0.10:6443"]) {
                    // Reemplazar el nombre de la imagen
                    sh "sed 's|NOMBRE_IMAGEN|${IMAGE_FULL_NAME}|' ./k8s/deployment.template > ./k8s/deployment.yml"
                    // Aplicar el manifiesto.
                    sh 'kubectl apply -f ./k8s/deployment.yml -n qa'
                }
            }
            post {
                success {
                    slackSend message: "Deployed image `${IMAGE_FULL_NAME}` to <${env.QA_URL}|QA env>"
                }
                failure {
                    slackSend color: "warning",  message: "Could not deploy image `${IMAGE_FULL_NAME}` to <${env.QA_URL}|QA env>"
                }
            }
        }

        stage("Deploy Prod") {
            when { tag "" }
            options {
                timeout(time: 3, unit: "MINUTES")
            }
            steps {
                script {
                    withCredentials([string(credentialsId: 'webhook_secret', variable: 'SECRET')]) { 
                        // Registrar el Webhook
                        hook = registerWebhook(authToken: SECRET)
                        echo "Waiting for POST to ${hook.url}\n"

                        // Notificar Slack
                        slackSend message: "To deploy, run: \n```curl -X POST -d 'OK' -H \"Authorization: ${SECRET}\" ${hook.url}```"
                        
                        // Obtener respuesta
                        data = waitForWebhook hook
                        echo "Webhook called with data: ${data}"
                    }

                    // Desplegar a Producción
                    withKubeConfig([credentialsId: 'kube-config', serverUrl: "https://192.168.0.10:6443"]) {
                        // Reemplazar el nombre de la imagen
                        sh "sed 's|NOMBRE_IMAGEN|${IMAGE_FULL_NAME}|' ./k8s/deployment.template > ./k8s/deployment.yml"
                        // Aplicar el manifiesto.
                        sh 'kubectl apply -f ./k8s/deployment.yml -n prod'
                    }
                }
            }
            post {
                success {
                    slackSend message: "Deployed image `${IMAGE_FULL_NAME}` to <${env.PROD_URL}|PROD env>"
                }
                failure {
                    slackSend color: "warning",  message: "Could not deploy image `${IMAGE_FULL_NAME}` to <${env.PROD_URL}|PROD env>"
                }
            }
        }
        
    }
}