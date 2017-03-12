node {
    stage 'Checkout'
    checkout scm

    stage 'Prepare'
    lock('dependencies') {
        sh 'ci/install-dependencies.sh'
    }

    stage 'Build'
    sh './gradlew clean test lint'
    androidLint pattern: 'build/**/lint*.xml'
    step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/**/*.xml'])
}