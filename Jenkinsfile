pipeline {

    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        NEXUS_URL = 'http://192.168.2.142:8081'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=maven-nexus-demo \
                        -Dsonar.projectName=maven-nexus-demo
                    '''
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'nexus-credentials',
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {

                    sh '''
                        cat > settings.xml <<EOF
                        <settings>
                          <servers>
                            <server>
                              <id>nexus-releases</id>
                              <username>${NEXUS_USERNAME}</username>
                              <password>${NEXUS_PASSWORD}</password>
                            </server>
                          </servers>
                        </settings>
                        EOF

                        mvn deploy \
                          -DskipTests \
                          -s settings.xml
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'CI/CD pipeline completed successfully'
        }

        failure {
            echo 'CI/CD pipeline failed'
        }

        always {
            archiveArtifacts artifacts: 'target/*.jar',
                             fingerprint: true
        }
    }
}
