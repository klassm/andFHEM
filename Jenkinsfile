node {
    stage 'Checkout'
    checkout scm

    //stage 'Prepare'
    //lock('dependencies') {
    //    sh 'ci/install-dependencies.sh'
    //}

    stage 'Build'
    sh './gradlew app:clean app:test app:lint --no-daemon'
    androidLint pattern: 'build/**/lint*.xml'
    step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/**/*.xml'])
}
