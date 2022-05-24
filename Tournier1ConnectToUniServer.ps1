Set-Location -Path $PSScriptRoot

Write-Host "Starting Agents"
Start-Process -FilePath java -ArgumentList '-jar .\massim_2022\javaagents\target\javaagents-2022-1.0-jar-with-dependencies.jar .\massim_2022\javaagents\conf\NextAgentsTurnier'

Exit
