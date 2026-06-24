# Create Windows shortcut to run the Raffles Hotel Management application
$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("$env:USERPROFILE\Desktop\Raffles Hotel.lnk")
$Shortcut.TargetPath = "powershell.exe"
$Shortcut.Arguments = "-ExecutionPolicy Bypass -File $PSScriptRoot\run-raffles.ps1"
$Shortcut.WorkingDirectory = "$PSScriptRoot"
$Shortcut.IconLocation = "$PSScriptRoot\src\main\resources\images\hotel_icon.png"
$Shortcut.Description = "Run Raffles Hotel Management System"
$Shortcut.Save()

Write-Host "Desktop shortcut created successfully!" -ForegroundColor Green
Write-Host "You can now launch the application from your desktop." -ForegroundColor Cyan 