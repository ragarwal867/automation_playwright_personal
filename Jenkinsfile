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
            description: 'Select test plan to run: Regression or Rerun',
            filterLength: 1,
            filterable: false,
            name: 'TEST_CONFIGURATION',
            randomName: 'choice-parameter-test-configuration',
            script: [
                $class: 'GroovyScript',
                fallbackScript: [classpath: [], sandbox: true, script: 'return []'],
                script: [classpath: [], sandbox: true, script: 'return ["Regression", "Rerun"]']
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

def shouldRun() { !env.SKIP_BUILD.toBoolean() }
def shouldRunRegression() { shouldRun() && params.TEST_CONFIGURATION == 'Regression' }
def shouldRunSmoke() { shouldRun() && params.TEST_CONFIGURATION == 'SmokeTest' }
def shouldRerun() { shouldRun() && params.TEST_CONFIGURATION == 'Rerun' }
def shouldPublish() { shouldRun() && params.PUBLISH_RESULTS_TO_DASHBOARD }

def isBranchIndexingTrigger() {
    def causes = currentBuild.getBuildCauses()
    return causes.any { cause ->
        cause._class == 'jenkins.branch.BranchIndexingCause'
    }
}

def validateBuildConfiguration() {
    if (isBranchIndexingTrigger()) {
        env.SKIP_BUILD = 'true'
        currentBuild.result = 'NOT_BUILT'
        error('Exiting: Triggered by Branch Indexing')
    }

    if (!isValidTestConfiguration()) {
        env.SKIP_BUILD = 'true'
        currentBuild.result = 'NOT_BUILT'
        error("Build skipped: No valid test configuration selected.")
    }
}

def buildRef() { "${currentBuild.number}-${params.ENVIRONMENT}" }

def runTestStage(String testReportName, String gherkinTags) {
    echo "Running test stage: ${testReportName}"
    echo "Running test tags: ${gherkinTags}"

    sh """
        mvn --fail-never test -B \
        -Duser.timezone=UTC \
        -Doracle.jdbc.timezoneAsRegion=false \
        -DnumberOfThreads=${params.NUMBER_OF_THREADS} \
        -Dbrowser.headless=true \
        -Denv=${params.ENVIRONMENT} \
        -Dbranch=${env.BRANCH_NAME} \
        -DbuildNumber=${currentBuild.number} \
        -Dcucumber.filter.tags='${gherkinTags}' \
        -Dsysteminfo.AppName=${testReportName}
    """
    echo "Stage ${testReportName} completed"
}

def rerunTestStage() {
    echo "Running rerun stage"

    echo "RERUN_FILE param = '${RERUN_FILE}'"
    echo "Workspace = ${env.WORKSPACE}"

    if (RERUN_FILE?.trim()) {
            def rerunFilePath = "${env.WORKSPACE}/${RERUN_FILE}"

            sh "ls -l ${rerunFilePath}"

            sh """
                mvn --fail-never test -B \
                -Duser.timezone=UTC \
                -Doracle.jdbc.timezoneAsRegion=false \
                -DnumberOfThreads=${params.NUMBER_OF_THREADS} \
                -Dbrowser.headless=true \
                -DbuildNumber=${currentBuild.number} \
                -Denv=${params.ENVIRONMENT} \
                -Dbranch=${env.BRANCH_NAME} \
                -Dcucumber.features=@${rerunFilePath}
            """

    } else {
            echo "No RERUN_FILE parameter provided. Skipping rerun stage."
    }

     echo "Rerun Stage completed"
}

def initializeBuildStage() {
    echo "Initializing build : ${buildRef()}"
    currentBuild.displayName = buildRef()
}

pipeline {
    agent any
    environment {
        GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no'
        API_BASE_URL = 'http://localhost:8090/api/v1'
        SKIP_BUILD = 'false'
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '14', numToKeepStr: '20'))
    }

    stages {
        stage('Init') {
            when { expression { shouldRun() } }
            steps {
                script {
                    initializeBuildStage()
                }
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
                    echo "Uploaded file: ${RERUN_FILE}"
                    rerunTestStage()
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