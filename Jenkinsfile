import java.time.Duration

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

def formatBuildDuration(long durationMillis) {
    def duration = Duration.ofMillis(durationMillis)
    return String.format("%02dh %02dm %02ds",
        duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()
    )
}

def runTestStage(String testReportName, String gherkinTags) {
    echo "Running test stage: ${testReportName}"

    def startTime = System.currentTimeMillis()
    sh """
        mvn --fail-never test -B \
        -Duser.timezone=UTC \
        -Doracle.jdbc.timezoneAsRegion=false \
        -Dbrowser.headless=true \
        -Dcucumber.filter.tags='${gherkinTags}' \
        -Dsysteminfo.AppName=${testReportName}
    """
    def durationMillis = System.currentTimeMillis() - startTime
    echo "Stage ${testReportName} completed in ${formatBuildDuration(durationMillis)}"
}

def buildRegressionParallelStages() {
    def parallelStages = [:]
    getRegressionTestConfig().each { moduleName, testConfig ->
        parallelStages[moduleName] = {
            node('master') {
                runTestStage(moduleName, testConfig.tags)
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
            echo "Build complete."
        }
    }
}
