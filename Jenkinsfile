pipeline {
    agent any

    environment {
        APP_IMAGE = 'echoai-app:latest'
        DB_IMAGE = 'echoai-db:latest'
        GIT_REPO = 'https://github.com/grexrr/EchoAI.git'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git url: GIT_REPO, branch: 'main'
            }
        }

        stage('Build JAR') {
            steps {
                script{
                    sh './mvn clean package'
                }
            }
        }

        stage('Build DB image') {
            steps {
                script{
                    sh 'docker-compose -f docker-compose.db.yml build'
                    sh 'docker tag echoai-db:latest $DB_IMAGE'
                }
            }
        }

        stage('Build APP image') {
            steps {
                script{
                    sh 'docker build -t $APP_IMAGE -f Dockerfile .'
                }
            }
        }

        stage('Push Image') {
            steps {
                script{
                    sh 'docker push $APP_IMAGE'
                    sh 'docker push $DB_IMAGE'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
