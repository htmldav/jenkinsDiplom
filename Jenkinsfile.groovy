pipeline {
    agent any
    tools {
        terraform 'terraform'
    }
    
    stages {
        stage('Git checkout'){
            steps{
              git branch: 'main', url: 'https://github.com/htmldav/terraform.git'
            }
        }
        stage('Terraform init'){
            steps{
                sh 'terraform init'
            }
        }
        stage('Terraform apply'){
            steps{
                sh 'terraform apply --auto-approve'
                script {
                    dd_ip = sh(
                        returnStdout: true, 
                        script: "terraform output external_ip_address_vm_1"
                    ).trim()      
                }
            }
        }
        
        stage('Create inventory file'){
            steps{
                sh "echo [build] >> ~/home.inv"
                sh "echo ${dd_ip} >> ~/home.inv"
            }
        }
        

        stage('Git checkout playbook'){
            steps{
              git branch: 'main', url: 'https://github.com/htmldav/ansibleDiplom.git'
            }
        }
        
        stage('Execute Ansible') { 
            steps { 
                ansiblePlaybook credentialsId: 'private-key', disableHostKeyChecking: true, installation: 'ansible2', inventory: '~/home.inv', playbook: 'playbook.yml'
            }
        }
        
        stage('Delete inventory file'){
            steps{
                sh "rm -f ~/home.inv"
            }
        }
    }
}