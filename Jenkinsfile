@NonCPS
def getRegressionTestConfig() {
    return [
        UBS               : [tags: '@regression and @ubs'],
        EPRES             : [tags: '@regression and @galileo and @epres'],
        ADMININSTRATION   : [tags: '@regression and @galileo and @administration'],
        HELP              : [tags: '@regression and @galileo and @help'],
        GTEE              : [tags: '@regression and @galileo and @gtee'],
        LC                : [tags: '@regression and @galileo and @lc'],
        CONVERSATION      : [tags: '@regression and @galileo and @conversation'],
        MLA               : [tags: '@regression and @galileo and @mla'],
        CORRECT_INSTRUMENT: [tags: '@regression and @galileo and @correctInstrument'],
        ADDRESS_BOOK      : [tags: '@regression and @galileo and @addressbook'],
        MIGRATION         : [tags: '@regression and @migration'],
        ONBOARDING        : [tags: '@regression and @onboarding']
    ]
}

def runTestStage(String testReportName, String gherkinTags) {
    echo "Running test stage: ${testReportName}"
    echo "Running test tags: ${gherkinTags}"

    sh """
        mvn --fail-never test -B \
        -Duser.timezone=UTC \
        -Doracle.jdbc.timezoneAsRegion=false \
        -Dbrowser.headless=true \
        -DbuildNumber=${currentBuild.number} \
        -Dbranch=${currentBuild.branch} \
        -Dcucumber.filter.tags='${gherkinTags}' \
        -Dsysteminfo.AppName=${testReportName}
    """
    echo "Stage ${testReportName} completed"
}

pipeline {
    agent any
    environment {
        GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no'
        API_BASE_URL = 'http://localhost:8090/api/v1'
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
        stage('Start Test Run') {
            steps {
                script {
                    echo "Starting Test Run"

                    def payload = """
                        {
                            "runType": "Galileo",
                            "server": "QA",
                            "branch": "main",
                            "buildNumber": "${currentBuild.number}",
                            "datetimeStart": "${java.time.Instant.now()}",
                            "status": "IN_PROGRESS"
                        }
                    """

                    def response = httpRequest(
                        url: "${API_BASE_URL}/testrun/start",
                        httpMode: 'POST',
                        contentType: 'APPLICATION_JSON',
                        requestBody: payload,
                        validResponseCodes: '200:299',
                        consoleLogResponseBody: true
                    )

                    if (response.status < 200 || response.status >= 300) {
                        error("Failed to register test run. Response code: ${response.status}")
                    }

                    echo "Test run started successfully (status ${response.status})"
                }
            }
        }
        stage('Regression') {
            steps {
                script {
                     def regressionTests = getRegressionTestConfig()
                        regressionTests.each { moduleName, testConfig ->
                            echo "=== Running Regression for ${moduleName} ==="
                            runTestStage(moduleName, testConfig.tags)
                     }
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Updating Test Run end time..."

                def endPayload = """
                    {
                        "runType": "Galileo",
                        "server": "QA",
                        "branch": "main",
                        "buildNumber": "${currentBuild.number}",
                        "datetimeEnd": "${java.time.Instant.now()}",
                        "status": "COMPLETED"
                    }
                """

               try {
                    def endResponse = httpRequest(
                        url: "${API_BASE_URL}/testrun/end",
                        httpMode: 'PUT',
                        contentType: 'APPLICATION_JSON',
                        requestBody: endPayload,
                        validResponseCodes: '200:299',
                        consoleLogResponseBody: true
                    )
                    echo "Test run marked completed (status ${endResponse.status})"
               } catch (err) {
                    echo "Failed to update test run end status: ${err.getMessage()}"
                    currentBuild.result = 'UNSTABLE'
               }
            }
            echo "Build complete."
        }
    }
}
