pipeline {
    agent { docker 'maven:3.5.2-jdk-8-slim' } 

    stages {
        stage('build') {
            steps {
               PROJECT_DIR=Lazy
               cd $PROJECT_DIR
               chmod +x gradlew
               ./gradlew build 
            }
        }
    }
}

