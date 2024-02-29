pipeline {
    options {
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr:'5'))
        disableConcurrentBuilds(abortPrevious: true)
    }
    agent {
        label 'basic'
    }
    tools {
        maven 'apache-maven-latest'
        jdk 'temurin-jdk17-latest'
    }
    stages {
        stage('Build') {
            steps {
                sh """
                mvn clean verify
                """
            }
            post {
                success {
                    archiveArtifacts artifacts: 'sites/org.eclipse.tea.repository/target/repository/'
                }
                always {
                    recordIssues tool: mavenConsole()
                }
            }
        }
    }
}
