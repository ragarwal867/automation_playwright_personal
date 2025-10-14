import java.time.Duration

def CONTROLLER = 'master'
def AGENT = 'playwright'

@NonCPS
def agentRunners() {
    return ['playwright-runner-02']
}

@NonCPS
def getRegressionTestConfig() {
        return [
            UBS               : [agent: 'playwright-runner-02', tags: '@regression and @ubs'],
            EPRES             : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @epres'],
            ADMINISTRATION    : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @administration'],
            HELP              : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @help'],
            GTEE              : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @gtee'],
            LC                : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @lc'],
            CONVERSATION      : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @conversation'],
            MLA               : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @mla'],
            CORRECT_INSTRUMENT: [agent: 'playwright-runner-02', tags: '@regression and @galileo and @correctInstrument'],
            ADDRESS_BOOK      : [agent: 'playwright-runner-02', tags: '@regression and @galileo and @addressbook'],
            MIGRATION         : [agent: 'playwright-runner-02', tags: '@regression and @migration'],
            ONBOARDING        : [agent: 'playwright-runner-02', tags: '@regression and @onboarding']
        ]
}

def buildRef() { "${currentBuild.number}-${params.ENVIRONMENT}" }

def initializeBuildStage() {
    env.DATE = new Date().format("yyyy-MM-dd")
    cleanControllerWorkspace()
    echo "Initializing build : ${buildRef()}"
    currentBuild.displayName = buildRef()
}

def provisionMavenSettingsFile() {
    configFileProvider([configFile(fileId: 'maven-settings-ci', targetLocation: 'settings.xml')]) {
        echo "settings.xml provisioned to workspace."
    }
}

def withTools(Closure body) {
    def mvnHome = tool name: '3.9.9', type: 'maven'
    def jdkHome = tool name: '17.0.12-zulu', type: 'jdk'
    withEnv([
        "JAVA_HOME=${jdkHome}",
        "PATH=${jdkHome}/bin:${mvnHome}/bin:${env.PATH}"
    ]) {
        body()
    }
}

def runTestStage(String testReportName, String gherkinTags) {
    def configFileName = "config/config_${params.ENVIRONMENT}.properties"
    def testOutputDirectory = "test-output/${testReportName}"
    def remoteFlag = "false"
    def headlessFlag = "true"

    echo "Running test stage: ${testReportName}"
    def startTime = System.currentTimeMillis()

    sh """
        mvn --fail-never test -s settings.xml -B \
        -Duser.timezone=UTC \
        -Doracle.jdbc.timezoneAsRegion=false \
        -DnumberOfThreads=${params.NUMBER_OF_THREADS} \
        -DconfigFileName=${configFileName} \
        -Dbrowser.headless=${headlessFlag} \
        -DdbCleanup.enabled=${params.DB_CLEANUP} \
        -Dcucumber.filter.tags='${gherkinTags}' \
        -Dsysteminfo.AppName=${testReportName} \
        -Dscreenshot.rel.path=Screenshots/ \
    """

    def latestTestOutputDirectory = sh(script: "ls -d ${testOutputDirectory}* | sort | tail -n 1", returnStdout: true).trim()

    def durationMillis = System.currentTimeMillis() - startTime
    def formattedDuration = formatBuildDuration(durationMillis)
}

def buildSetupParallelStages() {
    def parallelStages = [:]
    agentRunners().each { runnerLabel ->
        parallelStages[runnerLabel] = {
            node(runnerLabel) {
                withTools {
                    prepareAgentWorkspace()
                }
            }
        }
    }
    return parallelStages
}

def buildRegressionParallelStages() {
        def parallelStages = [:]
        getRegressionTestConfig().each { moduleName, testConfig ->
            parallelStages[moduleName] = {
                node(testConfig.agent) {
                    withTools {
                        runTestStage(moduleName, testConfig.tags)
                    }
                }
            }
        }
        return parallelStages
}

pipeline {
    agent none

    environment {
            GIT_SSH_COMMAND = 'ssh -o StrictHostKeyChecking=no'
            SKIP_BUILD = 'false'
    }

     options {
            timestamps()
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
        stage('Validation') {
            agent { label AGENT }
        }
        stage('Init') {
            agent { label CONTROLLER }
            steps {
                script {
                    initializeBuildStage()
                }
            }
        }
        stage('Setup') {
            steps {
                script {
                    parallel buildSetupParallelStages()
                }
            }
        }
         stage('Regression') {
            steps {
                script {
                    parallel buildRegressionParallelStages()
                }
            }
         }
    }

    post {
        always {
            node(CONTROLLER) {
                script {
                    echo "Skipping post-build actions."
                }
            }
        }
    }
}