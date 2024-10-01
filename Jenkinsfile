pipeline {
    agent any

    environment {
        APP_IMAGE = "grexrr/echoai-app:latest"
        GIT_REPO = "https://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@github.com/grexrr/EchoAI.git"
        BUILDER_NAME = "mybuilder"  // 定义固定的 builder 名称
        DOCKER_CREDENTIAL = "9ec9a02b-850f-4a61-adcc-6bc2ea8a6f91"
    }

    stages {
        stage('Setup Buildx') {
            steps {
                script {
                    sh '''
                        docker buildx create --name $BUILDER_NAME --use || docker buildx use $BUILDER_NAME
                    '''
                }
            }
        }
        
        stage('Docker Login') {
            steps {
                script {
                    // 使用凭证登录 Docker Hub
                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIAL, 
                                                      usernameVariable: 'DOCKER_USERNAME', 
                                                      passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh "echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin"
                    }
                }
            }
        }

        stage('Checkout Code') {
                    steps {
                        git url: GIT_REPO, branch: 'jenkins'
                    }
                }

        stage('Build JAR') {
            steps {
                script {
                    sh "mvn clean package -DskipTests -Dspring.test.database.replace=NONE"
                }
            }
        }

        stage('Build APP image') {
            steps {
                script {
                    sh """
                        docker buildx build --platform linux/amd64,linux/arm64 \
                        -t ${env.APP_IMAGE} -f Dockerfile --push .
                    """
                }
            }
        }
        
        stage('Cleanup Docker') {
            steps {
                script {
                    sh "docker ps -q | xargs --no-run-if-empty docker stop"
                    sh "docker container prune -f"
                    sh "docker image prune -f"
                    sh "docker volume prune -f"
                    sh "docker network prune -f"
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully!"
            script {
                sh "docker ps -q | xargs --no-run-if-empty docker stop"
                sh "docker container prune -f"
                sh "docker image prune -f"
            }
        }
    }
}
