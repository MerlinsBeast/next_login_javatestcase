// The normal CI pipeline. Runs the suite, then reports the result to xpath_healer.
pipeline {
  agent any

  environment {
    // localhost, not host.docker.internal: that name resolves only from inside a container.
    XPATH_HEALER_URL    = "${env.XPATH_HEALER_URL ?: 'http://localhost:3002/api/v1/webhooks/jenkins'}"
    XPATH_HEALER_SECRET = credentials('xpath-healer-secret')
  }

  stages {
    stage('Test') {
      steps {
        sh 'mvn -B test -Dheadless=true'
      }
    }
  }

  post {
    always {
      junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: true
      // The DOM dumps FailureCaptureListener wrote. The notifier attaches them to the payload.
      archiveArtifacts artifacts: 'target/failure-dom/*', allowEmptyArchive: true

      // Runs on green builds too: otherwise a regressed heal is never noticed.
      sh "BUILD_RESULT=${currentBuild.currentResult} python3 ci/notify_xpath_healer.py"
    }
  }
}
