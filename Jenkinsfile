/* generated jenkins file used for building and deploying prov-app in projects prova */
def final projectId = 'prova'
def final componentId = 'prov-app'
def final credentialsId = "${projectId}-cd-cd-user-with-password"
def dockerRegistry
node {
  dockerRegistry = env.DOCKER_REGISTRY
}

@Library('ods-jenkins-shared-library@2.x') _

// See https://www.opendevstack.org/ods-documentation/ods-jenkins-shared-library/latest/index.html for usage and customization.
odsPipeline(
  image: "${dockerRegistry}/cd/jenkins-slave-maven:2.x",
  projectId: projectId,
  componentId: componentId,
  branchToEnvironmentMapping: [
    'master': 'test',
    '*': 'dev'
  ]
) { context ->
  stageBuild(context)
  stageScanForSonarqube(context)
  stageStartOpenshiftBuild(context)
  stageDeployToOpenshift(context)
}

def stageBuild(def context) {
  def javaOpts = "-Xmx512m"
  def gradleTestOpts = "-Xmx128m"
  def springBootEnv = context.environment
  if (springBootEnv.contains('-dev')) {
    springBootEnv = 'dev'
  }
  stage('Build and Unit Test') {
    sh 'echo ${APP_DNS}'
    sh 'openssl s_client -showcerts -connect ${APP_DNS}:443 < /dev/null | openssl x509 -outform DER > docker/derp.der'
    withEnv(["TAGVERSION=${context.tagversion}", "NEXUS_HOST=${context.nexusHost}", "NEXUS_USERNAME=${context.nexusUsername}", "NEXUS_PASSWORD=${context.nexusPassword}", "JAVA_OPTS=${javaOpts}","GRADLE_TEST_OPTS=${gradleTestOpts}","ENVIRONMENT=${springBootEnv}"]) {
      def status = sh(script: "./gradlew clean build --stacktrace --no-daemon", returnStatus: true)
      if (status != 0) {
        error "Build failed!"
      }
    }
  }
}