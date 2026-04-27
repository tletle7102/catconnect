pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
    }

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

        DOCKER_CONTAINER_NAME = 'catconnect-container'
        SUBDOMAIN = 'catconnect.matchhub.co.kr'

        DISCORD_WEBHOOK_BUILD_SUCCESS = credentials('discord-webhook-build-success')
        DISCORD_WEBHOOK_BUILD_FAILURE = credentials('discord-webhook-build-failure')
        DISCORD_WEBHOOK_JINHEE_SUCCESS = credentials('discord-webhook-jinhee-success')
        DISCORD_WEBHOOK_JINHEE_FAILURE = credentials('discord-webhook-jinhee-failure')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Gradle') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build & Deploy') {
            steps {
                sh 'docker compose down || true'
                sh 'docker rm -f ${DOCKER_CONTAINER_NAME} || true'
                sh 'docker compose up -d --build'
            }
        }

        stage('Health Verification') {
            steps {
                script {
                    sh '''
                        echo "${DOCKER_CONTAINER_NAME} healthy 대기 (최대 4분)..."
                        for i in $(seq 1 24); do
                            status=$(docker inspect ${DOCKER_CONTAINER_NAME} --format='{{.State.Health.Status}}' 2>/dev/null || echo "missing")
                            echo "  [$i/24] ${DOCKER_CONTAINER_NAME}: $status"
                            if [ "$status" = "healthy" ]; then
                                echo "healthy 도달"
                                break
                            fi
                            if [ "$i" -eq 24 ]; then
                                echo "타임아웃: healthy 미도달"
                                docker logs ${DOCKER_CONTAINER_NAME} --tail 50
                                exit 1
                            fi
                            sleep 10
                        done
                    '''
                    sh '''
                        echo "외부 HTTPS 응답 검증..."
                        code=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 15 https://${SUBDOMAIN}/ || echo "000")
                        if [ "$code" != "200" ] && [ "$code" != "302" ] && [ "$code" != "401" ]; then
                            echo "HTTPS 비정상: HTTP $code"
                            exit 1
                        fi
                        echo "https://${SUBDOMAIN} 정상 (HTTP $code)"
                    '''
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }

    post {
        success {
            sh '''
                PAYLOAD=$(jq -nc \
                  --arg title "catconnect 배포 성공" \
                  --arg desc "빌드 #${BUILD_NUMBER} 배포 완료\nhttps://${SUBDOMAIN}" \
                  '{embeds: [{title: $title, description: $desc, color: 3066993}]}')
                curl -sS -H "Content-Type: application/json" -d "$PAYLOAD" "${DISCORD_WEBHOOK_BUILD_SUCCESS}" >/dev/null 2>&1 || true
                curl -sS -H "Content-Type: application/json" -d "$PAYLOAD" "${DISCORD_WEBHOOK_JINHEE_SUCCESS}" >/dev/null 2>&1 || true
            '''
            echo 'Deploy SUCCESS'
        }
        failure {
            sh '''
                PAYLOAD=$(jq -nc \
                  --arg title "catconnect 배포 실패" \
                  --arg desc "빌드 #${BUILD_NUMBER} 실패\n[로그 보기](${BUILD_URL}console)" \
                  '{embeds: [{title: $title, description: $desc, color: 15158332}]}')
                curl -sS -H "Content-Type: application/json" -d "$PAYLOAD" "${DISCORD_WEBHOOK_BUILD_FAILURE}" >/dev/null 2>&1 || true
                curl -sS -H "Content-Type: application/json" -d "$PAYLOAD" "${DISCORD_WEBHOOK_JINHEE_FAILURE}" >/dev/null 2>&1 || true
            '''
            echo 'Deploy FAILED'
        }
    }
}
