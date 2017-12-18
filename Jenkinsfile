pipeline {
    agent { docker 'maven:3.5.2-jdk-8-slim' } 

    stages {
        stage('build') {
            steps {
                bash 'mvn --version'
            }
        }
    }
}

