Write-Host "Starting database reset script..."

$dbFolder = "$env:USERPROFILE\raffles_db"

if (Test-Path -Path $dbFolder) {
    Write-Host "Found database folder at: $dbFolder"
    $files = Get-ChildItem -Path $dbFolder -File | Where-Object { $_.Name -like "raffleshotel*" }
    
    foreach ($file in $files) {
        Remove-Item -Path $file.FullName -Force
        Write-Host "Deleted file $($file.Name)"
    }
    
    Write-Host "All database files have been deleted."
} else {
    Write-Host "Database folder not found at: $dbFolder"
}

Write-Host "Database reset completed. Next time you run the application, a fresh database will be created." 