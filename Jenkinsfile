pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Environment Check') {
            steps {
                sh '''
                    echo "Java:"
                    java -version

                    echo "Maven:"
                    mvn -version

                    echo "Docker:"
                    docker --version

                    echo "Kubernetes:"
                    kubectl version --client
                '''
            }
        }

        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn clean verify \
                            org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                                -Dsonar.projectKey=maven-nexus-demo \
                                -Dsonar.projectName=maven-nexus-demo
                        '''
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                      -t 192.168.2.143:5000/maven-nexus-demo:${BUILD_NUMBER} .
                '''
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    trivy image \
                      --exit-code 1 \
                      --severity HIGH,CRITICAL \
                      192.168.2.143:5000/maven-nexus-demo:${BUILD_NUMBER}
                '''
            }
        }

        stage('Push Image') {
            steps {
                sh '''
                    docker push \
                      192.168.2.143:5000/maven-nexus-demo:${BUILD_NUMBER}
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    kubectl -n maven-demo set image \
                      deployment/maven-nexus-demo \
                      maven-nexus-demo=192.168.2.143:5000/maven-nexus-demo:${BUILD_NUMBER}

                    kubectl -n maven-demo rollout status \
                      deployment/maven-nexus-demo \
                      --timeout=120s
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "=== Pods ==="
                    kubectl get pods -n maven-demo -o wide

                    echo "=== Deployment ==="
                    kubectl get deployment maven-nexus-demo \
                      -n maven-demo

                    echo "=== Service ==="
                    kubectl get svc maven-nexus-demo \
                      -n maven-demo
                '''
            }
        }
    }

    post {

        success {
            echo "CI/CD deployment completed successfully."
            echo "Docker image: 192.168.2.143:5000/maven-nexus-demo:${BUILD_NUMBER}"
        }

        failure {
            echo "CI/CD pipeline failed."
        }

        always {
            archiveArtifacts artifacts: 'target/*.jar',
                             fingerprint: true,
                             allowEmptyArchive: true
        }
    }
}