#!/usr/bin/env groovy

/**
 * GitOps Orchestration Script
 * 
 * This script monitors a deployment repository for changes and triggers
 * updates to docker-compose based services.
 * 
 * Usage: groovy gitops.groovy [config-file]
 */

import groovy.json.JsonSlurper
import java.security.MessageDigest

class GitOpsOrchestrator {
    def config
    def checksumFile = ".gitops_checksums.json"
    def previousChecksums = [:]
    
    GitOpsOrchestrator(String configFile = "gitops-config.json") {
        loadConfig(configFile)
        loadPreviousChecksums()
    }
    
    def loadConfig(String configFile) {
        def file = new File(configFile)
        if (!file.exists()) {
            println "Configuration file not found: ${configFile}"
            println "Creating default configuration..."
            createDefaultConfig(configFile)
        }
        
        def jsonSlurper = new JsonSlurper()
        config = jsonSlurper.parse(file)
        println "Configuration loaded: ${config.deploymentRepo}"
    }
    
    def createDefaultConfig(String configFile) {
        def defaultConfig = """
{
    "deploymentRepo": "/path/to/deployment-repo",
    "composeFile": "compose.yml",
    "envFile": ".env",
    "services": []
}
"""
        new File(configFile).text = defaultConfig
    }
    
    def loadPreviousChecksums() {
        def file = new File(config.deploymentRepo, checksumFile)
        if (file.exists()) {
            def jsonSlurper = new JsonSlurper()
            previousChecksums = jsonSlurper.parse(file)
            println "Loaded previous checksums"
        } else {
            println "No previous checksums found, treating as first run"
            previousChecksums = [:]
        }
    }
    
    def savePreviousChecksums() {
        def file = new File(config.deploymentRepo, checksumFile)
        def json = groovy.json.JsonOutput.toJson(previousChecksums)
        file.text = groovy.json.JsonOutput.prettyPrint(json)
        println "Checksums saved"
    }
    
    def calculateChecksum(File file) {
        if (!file.exists()) {
            return null
        }
        
        def digest = MessageDigest.getInstance("SHA-256")
        file.eachByte(4096) { buffer, length ->
            digest.update(buffer, 0, length)
        }
        return digest.digest().encodeHex().toString()
    }
    
    def calculateDirectoryChecksum(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return null
        }
        
        def digest = MessageDigest.getInstance("SHA-256")
        def files = directory.listFiles()?.sort { it.name } ?: []
        
        files.each { file ->
            if (file.isFile()) {
                def fileChecksum = calculateChecksum(file)
                digest.update(fileChecksum.bytes)
            }
        }
        
        return digest.digest().encodeHex().toString()
    }
    
    def gitPull() {
        println "\n==> Pulling latest changes from git..."
        def repoDir = new File(config.deploymentRepo)
        
        if (!repoDir.exists()) {
            println "ERROR: Deployment repository not found: ${config.deploymentRepo}"
            return false
        }
        
        def process = ["git", "pull"].execute(null, repoDir)
        process.waitFor()
        
        def output = process.text
        println output
        
        return process.exitValue() == 0
    }
    
    def identifyChanges() {
        println "\n==> Identifying changes..."
        def changes = [
            fullUpdate: false,
            serviceUpdates: []
        ]
        
        def repoDir = new File(config.deploymentRepo)
        
        // Check compose file
        def composeFile = new File(repoDir, config.composeFile)
        def composeChecksum = calculateChecksum(composeFile)
        def previousComposeChecksum = previousChecksums.compose ?: null
        
        println "Compose file checksum: ${composeChecksum}"
        println "Previous compose checksum: ${previousComposeChecksum}"
        
        // Check .env file
        def envFile = new File(repoDir, config.envFile)
        def envChecksum = calculateChecksum(envFile)
        def previousEnvChecksum = previousChecksums.env ?: null
        
        println "Env file checksum: ${envChecksum}"
        println "Previous env checksum: ${previousEnvChecksum}"
        
        // If compose.yml or .env changed, trigger full update
        if (composeChecksum != previousComposeChecksum || envChecksum != previousEnvChecksum) {
            println "Core files changed - full update required"
            changes.fullUpdate = true
        }
        
        // Update stored checksums
        previousChecksums.compose = composeChecksum
        previousChecksums.env = envChecksum
        
        // Check individual service directories
        if (config.services) {
            config.services.each { serviceName ->
                def serviceDir = new File(repoDir, serviceName)
                if (serviceDir.exists() && serviceDir.isDirectory()) {
                    def serviceChecksum = calculateDirectoryChecksum(serviceDir)
                    def previousServiceChecksum = previousChecksums["service_${serviceName}"] ?: null
                    
                    println "Service ${serviceName} checksum: ${serviceChecksum}"
                    println "Previous ${serviceName} checksum: ${previousServiceChecksum}"
                    
                    if (serviceChecksum != previousServiceChecksum) {
                        println "Service ${serviceName} changed"
                        changes.serviceUpdates << serviceName
                    }
                    
                    previousChecksums["service_${serviceName}"] = serviceChecksum
                }
            }
        }
        
        return changes
    }
    
    def triggerUpdates(changes) {
        println "\n==> Triggering updates..."
        def repoDir = new File(config.deploymentRepo)
        
        if (changes.fullUpdate) {
            println "Executing full update: make gitopsAll"
            def process = ["make", "gitopsAll"].execute(null, repoDir)
            process.waitForProcessOutput(System.out, System.err)
            
            if (process.exitValue() == 0) {
                println "Full update completed successfully"
            } else {
                println "ERROR: Full update failed with exit code ${process.exitValue()}"
                return false
            }
        } else if (changes.serviceUpdates.size() > 0) {
            changes.serviceUpdates.each { serviceName ->
                println "Executing service update: make gitops${serviceName.capitalize()}"
                def makeTarget = "gitops${serviceName.capitalize()}"
                def process = ["make", makeTarget].execute(null, repoDir)
                process.waitForProcessOutput(System.out, System.err)
                
                if (process.exitValue() == 0) {
                    println "Service ${serviceName} update completed successfully"
                } else {
                    println "ERROR: Service ${serviceName} update failed with exit code ${process.exitValue()}"
                }
            }
        } else {
            println "No changes detected - nothing to update"
        }
        
        return true
    }
    
    def run() {
        println "========================================"
        println "GitOps Orchestration Starting"
        println "========================================"
        println "Target Repository: ${config.deploymentRepo}"
        println "Compose File: ${config.composeFile}"
        println "Env File: ${config.envFile}"
        println "Services: ${config.services}"
        println "========================================"
        
        // Step 1: Git pull
        if (!gitPull()) {
            println "ERROR: Git pull failed"
            return
        }
        
        // Step 2: Identify changes
        def changes = identifyChanges()
        
        // Step 3: Trigger updates
        triggerUpdates(changes)
        
        // Save checksums for next run
        savePreviousChecksums()
        
        println "\n========================================"
        println "GitOps Orchestration Completed"
        println "========================================"
    }
}

// Main execution
def configFile = args.length > 0 ? args[0] : "gitops-config.json"
def orchestrator = new GitOpsOrchestrator(configFile)
orchestrator.run()
