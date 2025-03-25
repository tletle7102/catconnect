pipeline {
    agent any
    environment {
        CATCONNECT_SPRING_PROFILE_ACTIVE = credentials('catconnect-spring-profile-active')
        CATCONNECT_TOMCAT_PORT = credentials('catconnect-tomcat-port')
        CATCONNECT_SPRING_SECURITY_JWT_SECRET = credentials('catconnect-jwt-secret')
        CATCONNECT_SPRING_SECURITY_EXPIRATION = credentials('catconnect-jwt-expiration')
        CATCONNECT_LOCAL_DB_URL = credentials('catconnect-local-db-url')
        CATCONNECT_LOCAL_DB_USERNAME = credentials('catconnect-local-db-username')
        CATCONNECT_LOCAL_DB_PASSWORD = credentials('catconnect-local-db-password')
        CATCONNECT_LOCAL_DB_NAME = credentials('catconnect-local-db-name')
        CATCONNECT_DEV_DB_URL = credentials('catconnect-dev-db-url')
        CATCONNECT_DEV_DB_USERNAME = credentials('catconnect-dev-db-username')
        CATCONNECT_DEV_DB_PASSWORD = credentials('catconnect-dev-db-password')
        CATCONNECT_DEV_DB_NAME = credentials('catconnect-dev-db-name')
        DOCKER_IMAGE = 'catconnect'
        DOCKER_CONTAINER_NAME = 'catconnect-container'
    }
    stages {
        stage('Clone Repository') {
            steps {
                git credentialsId: 'github-credentials',
                    url: 'https://github.com/tletle7102/catconnect.git',
                    branch: 'main'
            }
        }
        stage('Build Gradle') {
            steps {
                sh 'chmod +x gradlew'
                sh '''
                export CATCONNECT_SPRING_PROFILE_ACTIVE=${CATCONNECT_SPRING_PROFILE_ACTIVE}
                export CATCONNECT_TOMCAT_PORT=${CATCONNECT_TOMCAT_PORT}
                export CATCONNECT_SPRING_SECURITY_JWT_SECRET=${CATCONNECT_SPRING_SECURITY_JWT_SECRET}
                export CATCONNECT_SPRING_SECURITY_EXPIRATION=${CATCONNECT_SPRING_SECURITY_EXPIRATION}
                export CATCONNECT_DEV_DB_URL=${CATCONNECT_DEV_DB_URL}
                export CATCONNECT_DEV_DB_USERNAME=${CATCONNECT_DEV_DB_USERNAME}
                export CATCONNECT_DEV_DB_PASSWORD=${CATCONNECT_DEV_DB_PASSWORD}
                export CATCONNECT_DEV_DB_NAME=${CATCONNECT_DEV_DB_NAME}
                ./gradlew clean build
                '''
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${DOCKER_IMAGE} .'
            }
        }
        stage('Deploy Docker Container') {
            steps {
                sh 'docker rm -f ${DOCKER_CONTAINER_NAME} || true'
                sh '''
                docker run -d -p ${CATCONNECT_TOMCAT_PORT}:${CATCONNECT_TOMCAT_PORT} \
                --name ${DOCKER_CONTAINER_NAME} \
                -e CATCONNECT_SPRING_PROFILE_ACTIVE=${CATCONNECT_SPRING_PROFILE_ACTIVE} \
                -e CATCONNECT_TOMCAT_PORT=${CATCONNECT_TOMCAT_PORT} \
                -e CATCONNECT_SPRING_SECURITY_JWT_SECRET=${CATCONNECT_SPRING_SECURITY_JWT_SECRET} \
                -e CATCONNECT_SPRING_SECURITY_EXPIRATION=${CATCONNECT_SPRING_SECURITY_EXPIRATION} \
                -e CATCONNECT_LOCAL_DB_URL=${CATCONNECT_LOCAL_DB_URL} \
                -e CATCONNECT_LOCAL_DB_USERNAME=${CATCONNECT_LOCAL_DB_USERNAME} \
                -e CATCONNECT_LOCAL_DB_PASSWORD=${CATCONNECT_LOCAL_DB_PASSWORD} \
                -e CATCONNECT_LOCAL_DB_NAME=${CATCONNECT_LOCAL_DB_NAME} \
                -e CATCONNECT_DEV_DB_URL=${CATCONNECT_DEV_DB_URL} \
                -e CATCONNECT_DEV_DB_USERNAME=${CATCONNECT_DEV_DB_USERNAME} \
                -e CATCONNECT_DEV_DB_PASSWORD=${CATCONNECT_DEV_DB_PASSWORD} \
                -e CATCONNECT_DEV_DB_NAME=${CATCONNECT_DEV_DB_NAME} \
                ${DOCKER_IMAGE}
                '''
            }
        }
    }
}