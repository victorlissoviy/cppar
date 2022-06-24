pipeline{
  agent any
  stages{
    stage ('Build') {
      steps {
        sh 'mnv clean package'
      }
      post {
        success {
          echo "Archiving..."
          archiveArtifacts artifacts:"*/target/*.jar"
        }
      }
    }
    stage ('Deploy') {
      steps{
        echo "Deploy step"
      }
    }
  }
}