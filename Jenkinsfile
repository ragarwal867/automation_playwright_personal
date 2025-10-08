pipeline {
    agent any
    environment {
            GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'git@github.com:ragarwal867/automation_playwright_personal.git',
                    credentialsId: 'github-ssh-personal'
            }
        }
        stage('Build & Test') {
            steps {
                sh 'mvn clean test -s /var/jenkins_home/.m2/personalsettings.xml -B'
            }
        }
        stage('Report') {
            steps {
                cucumber buildStatus: 'UNSTABLE', fileIncludePattern: '**/cucumber.json'
            }
        }
    }
}