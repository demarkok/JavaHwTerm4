pipeline {
    agent { docker { image 'gradle:alpine' } } 

    stages {
        stage('build') {
            steps {
               sh 'PROJECT_DIR=Lazy'
               sh 'cd $PROJECT_DIR'
               sh 'gradle build' 
            }
        }
    }
}

