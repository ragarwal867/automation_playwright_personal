properties([
    parameters([
        choice(
            choices: ['QA', 'UAT', 'DOCKER01'],
            description: 'Select test environment',
            name: 'ENVIRONMENT'
        ),
        choice(
            choices: ['1', '2', '3', '4'],
            description: 'Select the number of threads that should be used when running automated tests.',
            name: 'NUMBER_OF_THREADS'
        ),
        choice(
            choices: ['playwright'],
            description: 'Select browser client type',
            name: 'BROWSER_CLIENT'
        ),
        booleanParam(
            defaultValue: false,
            description: 'Indicates whether old database records (presentations, guarantees, LCs, documents, users) should be deleted before running the tests.',
            name: 'DB_CLEANUP'
        ),
        booleanParam(
            defaultValue: true,
            description: 'Publish and archive test results to the Test Results Dashboard.',
            name: 'PUBLISH_RESULTS_TO_DASHBOARD'
        ),
        [$class: 'ChoiceParameter',
            choiceType: 'PT_RADIO',
            description: 'Select test plan to run: Regression, SmokeTest, or Custom (to run tests based on the provided gherkin tags).',
            filterLength: 1,
            filterable: false,
            name: 'TEST_CONFIGURATION',
            randomName: 'choice-parameter-test-configuration',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [classpath: [], sandbox: true, script: 'return []'],
                script: [classpath: [], sandbox: true, script: 'return ["Regression", "SmokeTest", "Custom", "Rerun"]']
            ]
        ],
        string(name: 'PARENT_BUILD_NUMBER', defaultValue: '', description: 'Parent build number for rerun'),
        file(name: 'RERUN_FILE', description: 'Upload rerun file')
    ])
])


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

def shouldRunRegression() { params.TEST_CONFIGURATION == 'Regression' }
def shouldRunSmoke() { params.TEST_CONFIGURATION == 'SmokeTest' }
def shouldRerun() { params.TEST_CONFIGURATION == 'Rerun' }
def shouldPublish() { params.PUBLISH_RESULTS_TO_DASHBOARD }

def runTestStage(String testReportName, String gherkinTags) {
    echo "Running test stage: ${testReportName}"
    echo "Running test tags: ${gherkinTags}"

    sh """
        mvn --fail-never test -B \
        -Duser.timezone=UTC \
        -Doracle.jdbc.timezoneAsRegion=false \
        -Dbrowser.headless=true \
        -DbuildNumber=${currentBuild.number} \
        -Dcucumber.filter.tags='${gherkinTags}' \
        -Dsysteminfo.AppName=${testReportName}
    """
    echo "Stage ${testReportName} completed"
}

def rerunTestStage() {
    echo "Running rerun stage"

    sh """
        mvn --fail-never test -B \
        -Duser.timezone=UTC \
        -Doracle.jdbc.timezoneAsRegion=false \
        -Dbrowser.headless=true \
        -DbuildNumber=${currentBuild.number} \
        -Denv=${params.ENVIRONMENT} \
        -Dcucumber.features=@${WORKSPACE}/${params.RERUN_FILE}
    """

     echo "Rerun Stage completed"
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
                    echo "Starting Test Run (Rerun: ${params.IS_RERUN})"

                    def payload = [
                        runType: "Galileo",
                        server: params.ENVIRONMENT,
                        branch: env.BRANCH_NAME ?: "main",
                        buildNumber: currentBuild.number,
                        datetimeStart: java.time.Instant.now().toString(),
                        status: "IN_PROGRESS"
                    ]

                    if (shouldRerun() && params.PARENT_BUILD_NUMBER?.trim()) {
                         payload["parentRun"] = [
                            runType: "Galileo",
                            server: params.ENVIRONMENT,
                            branch: env.BRANCH_NAME ?: "main",
                            buildNumber: params.PARENT_BUILD_NUMBER,
                         ]
                    }

                    def response = httpRequest(
                        url: "${API_BASE_URL}/testrun/start",
                        httpMode: 'POST',
                        contentType: 'APPLICATION_JSON',
                        requestBody: groovy.json.JsonOutput.toJson(payload),
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
            when { expression { shouldRunRegression() } }
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
        stage('Rerun Tests') {
            when { expression { shouldRerun() } }
            steps {
                script {
                    echo "=== Running Rerun ===="
                    echo "Uploaded file: ${params.RERUN_FILE}"
                    rerunTestStage(moduleName, testConfig.tags)
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Updating Test Run end time..."

                def endPayload = [
                    runType: "Galileo",
                    server: params.ENVIRONMENT,
                    branch: env.BRANCH_NAME ?: "main",
                    buildNumber: currentBuild.number,
                    datetimeEnd: java.time.Instant.now().toString(),
                    status: "COMPLETED"
                ]

               try {
                    def endResponse = httpRequest(
                        url: "${API_BASE_URL}/testrun/end",
                        httpMode: 'PUT',
                        contentType: 'APPLICATION_JSON',
                        requestBody: groovy.json.JsonOutput.toJson(endPayload),
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
