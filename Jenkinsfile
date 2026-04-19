// For Testcontainers + Build & Push: agent cần truy cập Docker API.
// - Jenkins trong Docker: mount -v /var/run/docker.sock:/var/run/docker.sock và --group-add $(getent group docker | cut -d: -f3) để user jenkins đọc/ghi socket.
// - Image jenkins/jenkins:lts không có lệnh `docker` (CLI): pipeline tải Docker static client vào .tools/docker (Setup Tools).
// - Jenkins trên host: cài Docker và thêm user jenkins vào nhóm docker.
pipeline {
    agent any

    environment {
        JAVA_HOME = "${WORKSPACE}/.tools/jdk-21"
        MAVEN_HOME = "${WORKSPACE}/.tools/maven"
        PATH = "${WORKSPACE}/.tools/docker:${WORKSPACE}/.tools/node/bin:${WORKSPACE}/.tools/jdk-21/bin:${WORKSPACE}/.tools/maven/bin:${env.PATH}"
    }

    parameters {
        booleanParam(name: 'FORCE_BUILD_ALL', defaultValue: false, description: 'Build all services regardless of changes')
    }

    stages {

        stage('Setup Tools') {
            steps {
                sh '''
                    mkdir -p .tools

                    if [ ! -d ".tools/jdk-21" ]; then
                        echo "Installing JDK 21..."
                        curl -fsSL https://download.java.net/java/GA/jdk21.0.2/f2283984656d49d69e91c558476027ac/13/GPL/openjdk-21.0.2_linux-x64_bin.tar.gz -o /tmp/jdk21.tar.gz
                        tar -xzf /tmp/jdk21.tar.gz -C .tools
                        mv .tools/jdk-21.0.2 .tools/jdk-21
                        rm -f /tmp/jdk21.tar.gz
                    fi

                    if [ ! -d ".tools/maven" ]; then
                        echo "Installing Maven 3.9.9..."
                        curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -o /tmp/maven.tar.gz
                        tar -xzf /tmp/maven.tar.gz -C .tools
                        mv .tools/apache-maven-3.9.9 .tools/maven
                        rm -f /tmp/maven.tar.gz
                    fi

                    # Docker CLI (static) — image jenkins/jenkins:lts không có binary `docker`; build/push cần CLI + socket.
                    DOCKER_CLI_VER=29.3.0
                    if [ ! -x ".tools/docker/docker" ]; then
                        ARCH=$(uname -m)
                        case "$ARCH" in
                            x86_64) DARCH=x86_64 ;;
                            aarch64) DARCH=aarch64 ;;
                            *) echo "Unsupported arch for Docker CLI: $ARCH"; exit 1 ;;
                        esac
                        echo "Installing Docker CLI ${DOCKER_CLI_VER} (${DARCH})..."
                        rm -rf .tools/docker
                        curl -fsSL "https://download.docker.com/linux/static/stable/${DARCH}/docker-${DOCKER_CLI_VER}.tgz" -o /tmp/docker-cli.tgz
                        tar -xzf /tmp/docker-cli.tgz -C .tools
                        rm -f /tmp/docker-cli.tgz
                    fi

                    if [ ! -d ".tools/node" ]; then
                        echo "Installing Node.js 22.15.0..."
                        ARCH=$(uname -m)
                        case "$ARCH" in
                            x86_64) NARCH=x64 ;;
                            aarch64) NARCH=arm64 ;;
                            *) echo "Unsupported arch for Node.js: $ARCH"; exit 1 ;;
                        esac
                        rm -rf /tmp/node.tar.gz .tools/node
                        curl -fsSL "https://nodejs.org/dist/v22.15.0/node-v22.15.0-linux-${NARCH}.tar.gz" -o /tmp/node.tar.gz
                        rm -rf .tools/node
                        mkdir -p .tools/node
                        tar -xzf /tmp/node.tar.gz -C .tools/node --strip-components=1
                        test -x .tools/node/bin/node || { echo "Node install failed"; ls -la .tools; ls -la .tools/node || true; exit 1; }
                        rm -f /tmp/node.tar.gz
                    fi

                    java -version
                    mvn -version
                    node -v
                    npm -v
                    docker version --format 'docker client={{.Client.Version}}' 2>/dev/null || docker version
                '''
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def allServices = [
                        'common-library',
                        'backoffice-bff',
                        'cart',
                        'customer',
                        'delivery',
                        'inventory',
                        'location',
                        'media',
                        'order',
                        'payment',
                        'payment-paypal',
                        'product',
                        'promotion',
                        'rating',
                        'recommendation',
                        'sampledata',
                        'search',
                        'storefront-bff',
                        'tax',
                        'webhook'
                    ]

                    def frontendServices = [
                        'storefront',
                        'backoffice'
                    ]

                    if (params.FORCE_BUILD_ALL) {
                        env.CHANGED_BACKEND_SERVICES  = allServices.join(',')
                        env.CHANGED_FRONTEND_SERVICES = frontendServices.join(',')
                        echo "FORCE_BUILD_ALL enabled — building everything"
                        return
                    }

                    def changedFiles = []
                    try {
                        changedFiles = sh(
                            script: "git diff --name-only HEAD~1 HEAD",
                            returnStdout: true
                        ).trim().split('\n').findAll { it }
                    } catch (e) {
                        echo "Could not detect changes (first build?). Building all services."
                        env.CHANGED_BACKEND_SERVICES  = allServices.join(',')
                        env.CHANGED_FRONTEND_SERVICES = frontendServices.join(',')
                        return
                    }

                    echo "Changed files:\n${changedFiles.join('\n')}"

                    def changedBackend  = [] as Set
                    def changedFrontend = [] as Set

                    def rootPomChanged = changedFiles.any { it == 'pom.xml' }
                    def commonLibChanged = changedFiles.any { it.startsWith('common-library/') } || rootPomChanged

                    if (commonLibChanged) {
                        changedBackend.addAll(allServices)
                    }

                    for (file in changedFiles) {
                        def service = allServices.find { file.startsWith("${it}/") }
                        if (service) changedBackend.add(service)

                        def frontend = frontendServices.find { file.startsWith("${it}/") }
                        if (frontend) changedFrontend.add(frontend)
                    }

                    env.CHANGED_BACKEND_SERVICES  = changedBackend ? changedBackend.toList().join(',') : ''
                    env.CHANGED_FRONTEND_SERVICES = changedFrontend ? changedFrontend.toList().join(',') : ''

                    echo "Backend services to build:  ${env.CHANGED_BACKEND_SERVICES ?: '(none)'}"
                    echo "Frontend services to build: ${env.CHANGED_FRONTEND_SERVICES ?: '(none)'}"
                }
            }
        }

        // ===================================================================
        //  PHASE 0 — SECRET SCAN
        // ===================================================================
        stage('Gitleaks Secret Scan') {
            steps {
                sh '''
                    if ! command -v gitleaks &> /dev/null; then
                        echo "Installing Gitleaks..."
                        curl -sSfL https://github.com/gitleaks/gitleaks/releases/download/v8.21.2/gitleaks_8.21.2_linux_x64.tar.gz -o /tmp/gitleaks.tar.gz
                        tar -xzf /tmp/gitleaks.tar.gz -C .tools gitleaks
                        rm -f /tmp/gitleaks.tar.gz
                        export PATH="${PWD}/.tools:${PATH}"
                    fi
                    gitleaks detect --source . --verbose --redact --no-git || true
                '''
            }
        }

        // ===================================================================
        //  PHASE 1 — CODE QUALITY & SECURITY SCAN
        // ===================================================================
        stage('SonarQube Analysis') {
            when {
                expression { env.CHANGED_BACKEND_SERVICES }
            }
            steps {
                script {
                    def modules = env.CHANGED_BACKEND_SERVICES.split(',').toList()
                    def projects = modules.collect { "-pl ${it}" }.join(' ')

                    sh """
                        mvn compile \
                            ${projects} \
                            -am \
                            -DskipTests
                    """

                    withSonarQubeEnv('sornaque') {
                        sh """
                            mvn sonar:sonar \
                                ${projects} \
                                -am \
                                -DskipTests \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        """
                    }
                }
            }
        }

        stage('Snyk Security Scan') {
            when {
                expression { env.CHANGED_BACKEND_SERVICES || env.CHANGED_FRONTEND_SERVICES }
            }
            steps {
                script {
                    def allChanged = []

                    if (env.CHANGED_BACKEND_SERVICES) {
                        allChanged.addAll(env.CHANGED_BACKEND_SERVICES.split(',').toList())
                    }
                    if (env.CHANGED_FRONTEND_SERVICES) {
                        allChanged.addAll(env.CHANGED_FRONTEND_SERVICES.split(',').toList())
                    }

                    for (svc in allChanged) {
                        echo "🔒 Snyk scanning: ${svc}"
                        dir(svc) {
                            // Ép JAVA_HOME và PATH cho Snyk CLI
                            withEnv([
                                "JAVA_HOME=${env.JAVA_HOME}",
                                "PATH=${env.JAVA_HOME}/bin:${env.PATH}"
                            ]) {
                                sh '''
                                    set -eux
                                    echo "JAVA_HOME=$JAVA_HOME"
                                    which java || true
                                    java -version
                                    which mvn  || true
                                    mvn -version
                                    # Đảm bảo có mvnw và có quyền execute cho Snyk Maven plugin
                                    if [ -f "../mvnw" ] && [ ! -f "./mvnw" ]; then
                                      cp ../mvnw ./mvnw
                                    fi
                                    if [ -f "./mvnw" ]; then
                                      chmod +x ./mvnw || true
                                    fi
                                '''
                                snykSecurity(
                                    snykInstallation: 'snyk',
                                    snykTokenId: 'snyk-token',
                                    failOnIssues: false,
                                    monitorProjectOnBuild: true,
                                    additionalArguments: '--all-projects -d'
                                )
                            }
                        }
                    }
                }
            }
        }

        // ===================================================================
        //  PHASE 2 — TEST
        // ===================================================================
        stage('Test') {
            when {
                expression { env.CHANGED_BACKEND_SERVICES }
            }
            stages {

                stage('Unit Tests') {
                    steps {
                        script {
                            def services = env.CHANGED_BACKEND_SERVICES.split(',')
                            def projects = services.collect { "-pl ${it}" }.join(' ')
                            sh """
                                mvn -B -ntp -T 1C test \
                                    ${projects} \
                                    -am \
                                    -Djacoco.skip=false \
                                    -Dmaven.test.failure.ignore=true
                            """
                        }
                    }
                    post {
                        always {
                            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                            jacoco(
                                execPattern:      '**/target/jacoco.exec',
                                classPattern:     '**/target/classes',
                                sourcePattern:    '**/src/main/java',
                                exclusionPattern: '**/config/**,**/exception/**,**/constants/**,**/*Application.class'
                            )
                        }
                    }
                }

                stage('Integration Tests') {
                    steps {
                        script {
                            def services = env.CHANGED_BACKEND_SERVICES.split(',')
                            def projects = services.collect { "-pl ${it}" }.join(' ')
                            sh """
                                export TESTCONTAINERS_REUSE_ENABLE=true
                                mvn -B -ntp verify \
                                    ${projects} \
                                    -am \
                                    -DskipUnitTests=true \
                                    -Djacoco.skip=false \
                                    -Dmaven.test.failure.ignore=true
                            """
                        }
                    }
                    post {
                        always {
                            junit testResults: '**/target/failsafe-reports/*.xml', allowEmptyResults: true
                        }
                    }
                }

                stage('Coverage Gate (>70% line)') {
                    steps {
                        script {
                            def services = env.CHANGED_BACKEND_SERVICES.split(',')
                            def skipList = ['common-library', 'sampledata', 'delivery']
                            def servicesToCheck = services.findAll { !(it in skipList) }

                            if (servicesToCheck) {
                                def projects = servicesToCheck.collect { "-pl ${it}" }.join(' ')
                                sh """
                                    mvn -B -ntp verify \
                                        ${projects} \
                                        -am \
                                        -DskipTests \
                                        -Djacoco.check.skip=false
                                """
                            } else {
                                echo "No services to check coverage for."
                            }
                        }
                    }
                }
            }
        }

        stage('Frontend Test') {
            when {
                expression { env.CHANGED_FRONTEND_SERVICES }
            }
            steps {
                script {
                    def services = env.CHANGED_FRONTEND_SERVICES.split(',')
                    for (svc in services) {
                        dir(svc) {
                            sh 'npm ci'
                            sh 'npm run lint  || true'
                            sh 'npm run test -- --coverage --reporters=default --reporters=jest-junit || true'
                        }
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/junit.xml', allowEmptyResults: true
                    publishHTML(target: [
                        reportDir:   'storefront/coverage/lcov-report',
                        reportFiles: 'index.html',
                        reportName:  'Storefront Coverage',
                        allowMissing: true
                    ])
                    publishHTML(target: [
                        reportDir:   'backoffice/coverage/lcov-report',
                        reportFiles: 'index.html',
                        reportName:  'Backoffice Coverage',
                        allowMissing: true
                    ])
                }
            }
        }

        // ===================================================================
        //  PHASE 3 — BUILD
        // ===================================================================
        stage('Build') {
            parallel {

                stage('Build Backend JARs') {
                    when {
                        expression { env.CHANGED_BACKEND_SERVICES }
                    }
                    steps {
                        script {
                            def services = env.CHANGED_BACKEND_SERVICES.split(',')
                            def projects = services.collect { "-pl ${it}" }.join(' ')
                            sh """
                                mvn package \
                                    ${projects} \
                                    -am \
                                    -DskipTests
                            """
                        }
                    }
                }

                stage('Build Frontend') {
                    when {
                        expression { env.CHANGED_FRONTEND_SERVICES }
                    }
                    steps {
                        script {
                            def services = env.CHANGED_FRONTEND_SERVICES.split(',')
                            for (svc in services) {
                                dir(svc) {
                                    sh 'npm ci'
                                    sh 'npm run build'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build & Push Docker Images') {
            when {
                expression { env.CHANGED_BACKEND_SERVICES || env.CHANGED_FRONTEND_SERVICES }
            }
            steps {
                script {
                    def tag = env.GIT_COMMIT.take(12)

                    def allChanged = []

                    if (env.CHANGED_BACKEND_SERVICES) {
                        allChanged.addAll(env.CHANGED_BACKEND_SERVICES.split(',').toList())
                    }
                    if (env.CHANGED_FRONTEND_SERVICES) {
                        allChanged.addAll(env.CHANGED_FRONTEND_SERVICES.split(',').toList())
                    }

                    def servicesToBuild = allChanged.findAll { svc ->
                        fileExists("${svc}/Dockerfile")
                    }

                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        def registry = "${env.DOCKER_USER}/yas"

                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        '''

                        for (svc in servicesToBuild) {
                            def imageTag = "${registry}:${svc}-${tag}"

                            echo "🚀 Building & pushing: ${imageTag}"

                            dir(svc) {
                                sh """
                                    docker build -t ${imageTag} .
                                    docker push ${imageTag}
                                """
                            }
                        }
                    }
                }
            }
        }

    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/site/jacoco/**', allowEmptyArchive: true
            cleanWs()
        }
        success {
            echo "✅ Pipeline completed successfully."
        }
        failure {
            echo "❌ Pipeline failed — check the test reports and logs above."
        }
    }
}