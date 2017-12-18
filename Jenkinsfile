pipeline {
    
    agent any
   

    stages {
      
        stage('build') {
            steps {
               sh 'dnf install gradle'
               sh 'PROJECT_DIR=Lazy'
               sh 'cd $PROJECT_DIR'
               sh 'gradle build' 
            }
        }
    }
}
