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
            choices: ['0', '1', '2'],
            description: 'Select the number of rerun for failed scenarios.',
            name: 'REQUESTED_RERUN'
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
            defaultValue: false,
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
        base64File(name: 'RERUN_FILE', description: 'Upload rerun file')
    ])
])


@NonCPS
def getRegressionTestConfig() {
    return [
        EPRES             : [tags: '@regression and @galileo and @epres'],
        ADMININSTRATION   : [tags: '@regression and @galileo and @administration'],
        HELP              : [tags: '@regression and @galileo and @help'],
        GTEE              : [tags: '@regression and @galileo and @gtee'],
        LC                : [tags: '@regression and @galileo and @lc']
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
    echo "=== Running Rerun Stage ==="
    echo "Workspace = ${env.WORKSPACE}"

    def rerunDir = "${env.WORKSPACE}/rerun"
    sh "mkdir -p ${rerunDir}"

    def destinationFile = "${rerunDir}/rerunfile.txt"

    if (params.RERUN_FILE) {
        echo "Decoding Base64 content from RERUN_FILE parameter..."
        writeFile file: destinationFile, text: new String(params.RERUN_FILE.decodeBase64(), 'UTF-8')
        echo "Decoded rerun file saved at: ${destinationFile}"

        sh "ls -lh ${rerunDir}"

        def fileExists = sh(script: "test -f '${destinationFile}' && echo true || echo false", returnStdout: true).trim()

        if (fileExists == 'true') {
             // Number of Playwright runners
             def numRunners = 2
             echo "Splitting rerun file into ${numRunners} parts for ${numRunners} Playwright runners..."
             sh "split -n l/${numRunners} '${destinationFile}' '${rerunDir}/part_'"
             sh "ls -lh ${rerunDir}"

              def runnerIndex = 0
              def partFile = String.format("%s/part_%02d", rerunDir, runnerIndex)

              def partExists = sh(script: "test -f '${partFile}' && echo true || echo false", returnStdout: true).trim()
              if (partExists == 'true') {
                echo "Executing ONLY part ${runnerIndex + 1} for testing: ${partFile}"
                sh """
                    mvn --fail-never test -B \
                    -Duser.timezone=UTC \
                    -Doracle.jdbc.timezoneAsRegion=false \
                    -DnumberOfThreads=${params.NUMBER_OF_THREADS} \
                    -Dbrowser.headless=true \
                    -DbuildNumber=${currentBuild.number} \
                    -Denv=${params.ENVIRONMENT} \
                    -Dbranch=${env.BRANCH_NAME} \
                    -Dcucumber.features=@${partFile}
                """
              } else {
                echo "No part file found for runner index ${runnerIndex}. Skipping."
              }
        } else {
            echo "Rerun file not found after decoding. Skipping rerun stage."
        }
    } else {
        echo "No RERUN_FILE parameter provided. Skipping rerun stage."
    }

    echo "=== Rerun Stage Completed ==="
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
                    echo "Starting Test Run"

                    def payload = [
                        runType: "Galileo",
                        server: params.ENVIRONMENT,
                        branch: env.BRANCH_NAME ?: "main",
                        buildNumber: currentBuild.number,
                        requestedRerun: params.REQUESTED_RERUN,
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
        }
    }
}