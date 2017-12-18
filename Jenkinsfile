pipeline {
    
    agent { docker { image 'gradle:alpine' } } 

    stages {
        sh 'service docker start'
        stage('build') {
            steps {
               
               sh 'PROJECT_DIR=Lazy'
               sh 'cd $PROJECT_DIR'
               sh 'gradle build' 
            }
        }
    }
}
