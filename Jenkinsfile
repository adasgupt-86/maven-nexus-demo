pipeline {

    agent any

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

        stage('SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=maven-nexus-demo
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

        stage('Deploy Kubernetes') {
            steps {
                sh '''
                    kubectl -n maven-demo set image \
                      deployment/maven-nexus-demo \
                      maven-nexus-demo=192.168.2.143:5000/maven-nexus-demo:${BUILD_NUMBER}

                    kubectl -n maven-demo rollout status \
                      deployment/maven-nexus-demo
                '''
            }
        }

        stage('Verify') {
            steps {
                sh '''
                    kubectl get pods -n maven-demo -o wide
                    kubectl get svc -n maven-demo
                '''
            }
        }
    }

    post {
        success {
            echo 'CI/CD deployment completed successfully'
        }

        failure {
            echo 'CI/CD pipeline failed'
        }
    }
}