pipeline {
    agent any
    environment {
            GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/ragarwal867/automation_playwright_personal.git',
                    credentialsId: 'github-token-personal'
            }
        }
        stage('Build & Test') {
            steps {
                sh 'mvn clean install -B'
            }
        }
        stage('Report') {
            steps {
                cucumber buildStatus: 'UNSTABLE', fileIncludePattern: '**/cucumber.json'
            }
        }
    }
}