pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    parameters {
        choice(name: 'TAGS', choices: ['not @wip', '@smoke', '@api', '@ui', '@negative'], description: 'Cucumber tag expression')
        choice(name: 'BROWSER', choices: ['chromium', 'firefox', 'webkit'], description: 'Browser for UI tests')
    }

    environment {
        // In a real pipeline credentials come from the Jenkins credentials store, never from code.
        ORANGEHRM_USERNAME = credentials('orangehrm-username')
        ORANGEHRM_PASSWORD = credentials('orangehrm-password')
    }

    stages {
        stage('Install browsers') {
            steps {
                sh 'mvn -B -q compile exec:java -Dexec.args="install --with-deps ${BROWSER}"'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B clean test -Dcucumber.filter.tags="${TAGS}" -Dbrowser=${BROWSER}'
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            // With the "Cucumber Reports" plugin installed: cucumber jsonReportDirectory: 'target/cucumber-reports', fileIncludePattern: '*.json'
            archiveArtifacts artifacts: 'target/cucumber-reports/**, target/traces/**, target/screenshots/**', allowEmptyArchive: true
        }
    }
}
