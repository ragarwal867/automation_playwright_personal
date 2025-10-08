pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'git@github.com-ragarwal:ragarwal867/automation_playwright_personal.git',
                    credentialsId: 'github-ssh'
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