pipeline {
    agent any
    environment {
            GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no'
    }
    stages {
        stage('Checkout') {
            steps {
               checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/ragarwal867/automation_playwright_personal.git',
                        credentialsId: 'github-token-personal'
                   ]],
                   extensions: [
                        [$class: 'CleanBeforeCheckout'],
                        [$class: 'CloneOption', depth: 0, noTags: false, shallow: false]
                  ]
              ])
            }
        }
        stage('Build & Test') {
            steps {
                sh 'mvn clean install -B'
            }
        }
    }
}