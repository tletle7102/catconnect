pipeline {
    agent any

    environment {
        CATCONNECT_SPRING_PROFILE_ACTIVE = credentials('catconnect-spring-profile-active')
        CATCONNECT_TOMCAT_PORT = credentials('catconnect-tomcat-port')
        CATCONNECT_SPRING_SECURITY_JWT_SECRET = credentials('catconnect-jwt-secret')
        CATCONNECT_SPRING_SECURITY_EXPIRATION = credentials('catconnect-jwt-expiration')
        CATCONNECT_DEV_DB_URL = credentials('catconnect-dev-db-url')
        CATCONNECT_DEV_DB_USERNAME = credentials('catconnect-dev-db-username')
        CATCONNECT_DEV_DB_PASSWORD = credentials('catconnect-dev-db-password')
        CATCONNECT_DEV_DB_NAME = credentials('catconnect-dev-db-name')
        MAIL_USERNAME = credentials('catconnect-mail-username')
        MAIL_PASSWORD = credentials('catconnect-mail-password')
        SOLAPI_API_KEY = credentials('catconnect-solapi-api-key')
        SOLAPI_API_SECRET = credentials('catconnect-solapi-api-secret')
        SOLAPI_SENDER_PHONE = credentials('catconnect-solapi-sender-phone')

        DOCKER_IMAGE = 'catconnect'
        DOCKER_CONTAINER_NAME = 'catconnect-container'
    }

    stages {

        stage('Debug Environment') {
            steps {
                sh """
                echo "===== DEBUG START ====="
                echo "DOCKER_IMAGE=${env.DOCKER_IMAGE}"
                echo "DOCKER_CONTAINER_NAME=${env.DOCKER_CONTAINER_NAME}"
                echo "PORT=${env.CATCONNECT_TOMCAT_PORT}"
                echo "PROFILE=${env.CATCONNECT_SPRING_PROFILE_ACTIVE}"
                echo "===== DEBUG END ====="
                """
            }
        }

        stage('Clone Repository') {
            steps {
                git credentialsId: 'github-credentials',
                    url: 'https://github.com/tletle7102/catconnect.git',
                    branch: 'main'
            }
        }

        stage('Build Gradle') {
            steps {
                sh """
                chmod +x gradlew
                ./gradlew clean build
                """
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t ${env.DOCKER_IMAGE} .
                """
            }
        }

        stage('Deploy Docker Container') {
            steps {
                sh """
                docker rm -f ${env.DOCKER_CONTAINER_NAME} || true

                docker run -d \
                --restart unless-stopped \
                -p ${env.CATCONNECT_TOMCAT_PORT}:${env.CATCONNECT_TOMCAT_PORT} \
                --name ${env.DOCKER_CONTAINER_NAME} \
                -e CATCONNECT_SPRING_PROFILE_ACTIVE=${env.CATCONNECT_SPRING_PROFILE_ACTIVE} \
                -e CATCONNECT_TOMCAT_PORT=${env.CATCONNECT_TOMCAT_PORT} \
                -e CATCONNECT_SPRING_SECURITY_JWT_SECRET=${env.CATCONNECT_SPRING_SECURITY_JWT_SECRET} \
                -e CATCONNECT_SPRING_SECURITY_EXPIRATION=${env.CATCONNECT_SPRING_SECURITY_EXPIRATION} \
                -e CATCONNECT_DEV_DB_URL=${env.CATCONNECT_DEV_DB_URL} \
                -e CATCONNECT_DEV_DB_USERNAME=${env.CATCONNECT_DEV_DB_USERNAME} \
                -e CATCONNECT_DEV_DB_PASSWORD=${env.CATCONNECT_DEV_DB_PASSWORD} \
                -e CATCONNECT_DEV_DB_NAME=${env.CATCONNECT_DEV_DB_NAME} \
                -e MAIL_USERNAME=${env.MAIL_USERNAME} \
                -e MAIL_PASSWORD=${env.MAIL_PASSWORD} \
                -e SOLAPI_API_KEY=${env.SOLAPI_API_KEY} \
                -e SOLAPI_API_SECRET=${env.SOLAPI_API_SECRET} \
                -e SOLAPI_SENDER_PHONE=${env.SOLAPI_SENDER_PHONE} \
                ${env.DOCKER_IMAGE}
                """
            }
        }

    }
}