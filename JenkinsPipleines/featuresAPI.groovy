pipeline {
  agent any
  environment {
    PATH="/var/jenkins_home/.local/bin:${env.PATH}"
  }  
  stages {
    stage('Stage 1') {
      steps {
        withAWS(credentials: 'sam-cli-access', region: 'us-east-1') {
            sh 'echo $AWS_ACCESS_KEY_ID'
            sh 'echo $AWS_SECRET_ACCESS_KEY'
        }        
      }
    }      
    stage('checkout') {
      steps {
        sh 'rm -rf features_api'  
        sh 'git clone https://github.com/JohnMaillet/features_api.git'
      }
    }              
    stage('AWS SAM CLI Install') {
        steps {
            script {
                try {
                    sh 'sam --version'
                    sh 'echo sam already installed'
                } catch(Exception e) {
                    sh 'echo installing sam'
                    sh 'pip3 install aws-sam-cli'  
                    sh 'echo $PATH'
                    sh 'sam --version'
          
                }
            }
        }
    }
    stage('unit test') {
        steps {
            dir("features_api") {
                sh 'pip install -r tests/requirements.txt --user'
                sh 'python3 -m pytest tests/unit -v' 
            }
            
        }
    }    
    stage('SAM build') {
        steps {
            dir("features_api") {
                sh 'sam build'
            }
            
        }
    }
    stage('Deploy') {
        steps {
            dir("features_api") {
                withAWS(credentials: 'sam-cli-access', region: 'us-east-1') {
                    withEnv(['AWS_SAM_STACK_NAME="features-api"']) {
                        sh 'sam deploy --region us-east-1 --no-confirm-changeset'
                    }                        
                }
            }   
        }
    }
    stage('Integration Test') {
      steps {
        dir("features_api") {
            sh 'pwd'
            sh 'pip install -r tests/requirements.txt --user'
            sh 'python3 -m pytest tests/unit -v' 
            withAWS(credentials: 'sam-cli-access', region: 'us-east-1') {
                withEnv(['AWS_SAM_STACK_NAME="features-api"']) {
                    sh 'python3 -m pytest tests/integration -v'
                }                
            }                            
        }
      }
    }  
  }
}