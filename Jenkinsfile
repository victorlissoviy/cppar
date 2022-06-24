pipeline{
  agent any
  triggers {
    pollSCM('*/5 * * * *')
  }
  stages{
    stage ('Build') {
      steps {
        sh 'mvn clean package'
      }
      post {
        success {
          echo "Archiving..."
          archiveArtifacts artifacts:'**/target/*.jar'
        }
      }
    }
    stage ('Deployments') {
      parallel {
        stage('deploy to staging') {
          steps{
            sh "cp target/*.jar /home/victor/"
          }
        }
        stage('deploy to install') {
          steps{
            sh "cp target/*.jar /storage/Install/"
          }
        }
      }
    }
  }
}