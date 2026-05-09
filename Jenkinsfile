pipeline {
    agent any

    options {
        timeout(time: 20, unit: 'MINUTES')
        timestamps()
    }

    environment {
        NYANGVIL_SPRING_PROFILE_ACTIVE    = credentials('nyangvil-spring-profile-active')
        NYANGVIL_DEV_TOMCAT_PORT          = credentials('nyangvil-dev-tomcat-port')
        NYANGVIL_DEV_JWT_SECRET           = credentials('nyangvil-dev-jwt-secret')
        NYANGVIL_DEV_JWT_EXPIRATION       = credentials('nyangvil-dev-jwt-expiration')
        NYANGVIL_DEV_DB_URL               = credentials('nyangvil-dev-db-url')
        NYANGVIL_DEV_DB_USERNAME          = credentials('nyangvil-dev-db-username')
        NYANGVIL_DEV_DB_PASSWORD          = credentials('nyangvil-dev-db-password')
        NYANGVIL_DEV_DB_NAME              = credentials('nyangvil-dev-db-name')
        NYANGVIL_DEV_MAIL_USERNAME        = credentials('nyangvil-dev-mail-username')
        NYANGVIL_DEV_MAIL_PASSWORD        = credentials('nyangvil-dev-mail-password')
        NYANGVIL_DEV_SOLAPI_API_KEY       = credentials('nyangvil-dev-solapi-api-key')
        NYANGVIL_DEV_SOLAPI_API_SECRET    = credentials('nyangvil-dev-solapi-api-secret')
        NYANGVIL_DEV_SOLAPI_SENDER_PHONE  = credentials('nyangvil-dev-solapi-sender-phone')

        DOCKER_CONTAINER_NAME = 'nyangvil-container'
        SUBDOMAIN = 'nyang.matchhub.co.kr'

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
                sh 'docker rm -f nyangvil-frontend || true'
                sh 'docker compose up -d --build'
            }
        }

        stage('Health Verification') {
            steps {
                script {
                    sh '''
                        echo "backend healthy 대기 (최대 4분)..."
                        for i in $(seq 1 24); do
                            status=$(docker inspect ${DOCKER_CONTAINER_NAME} --format='{{.State.Health.Status}}' 2>/dev/null || echo "missing")
                            echo "  [$i/24] ${DOCKER_CONTAINER_NAME}: $status"
                            if [ "$status" = "healthy" ]; then break; fi
                            if [ "$i" -eq 24 ]; then
                                echo "타임아웃: backend healthy 미도달"
                                docker logs ${DOCKER_CONTAINER_NAME} --tail 50
                                exit 1
                            fi
                            sleep 10
                        done

                        echo "frontend healthy 대기 (최대 1분)..."
                        for i in $(seq 1 6); do
                            status=$(docker inspect nyangvil-frontend --format='{{.State.Health.Status}}' 2>/dev/null || echo "missing")
                            echo "  [$i/6] nyangvil-frontend: $status"
                            if [ "$status" = "healthy" ]; then break; fi
                            if [ "$i" -eq 6 ]; then
                                echo "타임아웃: frontend healthy 미도달"
                                docker logs nyangvil-frontend --tail 50
                                exit 1
                            fi
                            sleep 10
                        done
                    '''
                    sh '''
                        echo "외부 HTTPS 응답 검증..."
                        code_root=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 15 https://${SUBDOMAIN}/ || echo "000")
                        code_api=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 15 https://${SUBDOMAIN}/api/board-categories || echo "000")
                        echo "  / -> $code_root"
                        echo "  /api/board-categories -> $code_api"
                        if [ "$code_root" != "200" ]; then
                            echo "frontend 라우팅 비정상"; exit 1
                        fi
                        if [ "$code_api" != "200" ] && [ "$code_api" != "401" ] && [ "$code_api" != "403" ]; then
                            echo "backend 라우팅 비정상"; exit 1
                        fi
                        echo "라우팅 분리 정상"
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
                  --arg title "nyangvil 배포 성공" \
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
                  --arg title "nyangvil 배포 실패" \
                  --arg desc "빌드 #${BUILD_NUMBER} 실패\n[로그 보기](${BUILD_URL}console)" \
                  '{embeds: [{title: $title, description: $desc, color: 15158332}]}')
                curl -sS -H "Content-Type: application/json" -d "$PAYLOAD" "${DISCORD_WEBHOOK_BUILD_FAILURE}" >/dev/null 2>&1 || true
                curl -sS -H "Content-Type: application/json" -d "$PAYLOAD" "${DISCORD_WEBHOOK_JINHEE_FAILURE}" >/dev/null 2>&1 || true
            '''
            echo 'Deploy FAILED'
        }
    }
}
