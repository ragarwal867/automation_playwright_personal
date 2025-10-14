import java.time.Duration

@NonCPS
def getRegressionTestConfig() {
        return [
            UBS               : [tags: '@regression and @ubs'],
            EPRES             : [tags: '@regression and @galileo and @epres'],
            ADMINISTRATION    : [tags: '@regression and @galileo and @administration'],
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

def buildRef() { "${currentBuild.number}-${params.ENVIRONMENT}" }

def formatBuildDuration(long durationMillis) {
    def duration = Duration.ofMillis(durationMillis)
    return String.format("%02dh %02dm %02ds",
        duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()
    )
}

def initializeBuildStage() {
    env.DATE = new Date().format("yyyy-MM-dd")
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
        stage('Init') {
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
            script {
                echo "Skipping post-build actions."
            }
        }
    }
}