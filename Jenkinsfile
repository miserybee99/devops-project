pipeline {
    agent any

    environment {
        SERVICE_DIR = 'media'
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changes = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep '^${SERVICE_DIR}/' || true",
                        returnStdout: true
                    ).trim()

                    if (changes == '' && env.BRANCH_NAME != 'main') {
                        currentBuild.result = 'NOT_BUILT'
                        error("No changes in ${SERVICE_DIR}/, skipping.")
                    }
                }
            }
        }

        stage('Test') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'mvn test -B'
                }
            }
            post {
                always {
                    junit "${SERVICE_DIR}/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Build') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'mvn package -DskipTests -B'
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline passed!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}