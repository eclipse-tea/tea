pipeline {
    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr:'5'))
        disableConcurrentBuilds(abortPrevious: true)
    }
    agent any
    tools {
        maven 'apache-maven-latest'
        jdk 'temurin-jdk17-latest'
    }
    stages {
        stage('Build') {
            steps {
                sh """
                set -xe
                mvn clean verify
                mv sites/org.eclipse.tea.repository/target/repository .
                """
            }
            post {
                success {
                    archiveArtifacts artifacts: 'repository/'
                }
                always {
                    recordIssues tool: mavenConsole()
                }
            }
        }
    }
}
