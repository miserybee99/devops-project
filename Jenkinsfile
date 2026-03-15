pipeline {
    agent any

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    // Lấy danh sách file thay đổi
                    def changedFiles = sh(
                        script: "git diff --name-only HEAD~1 HEAD",
                        returnStdout: true
                    ).trim().split('\n')

                    // Danh sách tất cả services
                    def services = [
                        'media', 'product', 'cart', 'order',
                        'customer', 'rating', 'inventory',
                        'location', 'tax', 'search',
                        'payment', 'payment-paypal', 'promotion',
                        'storefront', 'storefront-bff',
                        'backoffice', 'backoffice-bff', 'webhook'
                    ]

                    // Tìm service nào có thay đổi
                    env.CHANGED_SERVICES = services.findAll { service ->
                        changedFiles.any { it.startsWith("${service}/") }
                    }.join(',')

                    echo "🔍 Services changed: ${env.CHANGED_SERVICES}"

                    if (env.CHANGED_SERVICES == '') {
                        currentBuild.result = 'NOT_BUILT'
                        error("No service changes detected, skipping.")
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    services.each { service ->
                        echo "🧪 Testing service: ${service}"
                        dir("${service}") {
                            sh 'mvn test -B'
                        }
                    }
                }
            }
            post {
                always {
                    script {
                        def services = env.CHANGED_SERVICES.split(',')
                        services.each { service ->
                            junit testResults: "${service}/target/surefire-reports/*.xml",
                                  allowEmptyResults: true
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    services.each { service ->
                        echo "📊 Analyzing service: ${service}"
                        withSonarQubeEnv('SonarQube') {
                            dir("${service}") {
                                sh """
                                    mvn sonar:sonar \
                                      -Dsonar.projectKey=yas-${service} \
                                      -Dsonar.projectName='YAS ${service}' \
                                      -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                      -B
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    def services = env.CHANGED_SERVICES.split(',')
                    services.each { service ->
                        echo "🔨 Building service: ${service}"
                        dir("${service}") {
                            sh 'mvn package -DskipTests -B'
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline passed!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}