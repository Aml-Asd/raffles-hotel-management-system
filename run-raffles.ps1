# PowerShell script to run Raffles Hotel Management System

Write-Host "Starting Raffles Hotel Management System..." -ForegroundColor Cyan

# Set JAVA_HOME if not already set
if (-not $env:JAVA_HOME) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-22"
    Write-Host "Setting JAVA_HOME to $env:JAVA_HOME" -ForegroundColor Yellow
}

# First, compile with Maven once to ensure all dependencies are downloaded
Write-Host "Compiling the application..." -ForegroundColor Green
.\mvnw.cmd compile

# Run the application with JavaFX modules
Write-Host "Running the application..." -ForegroundColor Green
.\mvnw.cmd javafx:run

Write-Host "Application closed." -ForegroundColor Cyan 