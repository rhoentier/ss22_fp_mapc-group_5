Set-Location -Path $PSScriptRoot

Write-Host "Starting Server"
Start-Process -FilePath java -ArgumentList '-jar .\massim_2022\server\target\server-2022-1.1-jar-with-dependencies.jar -conf .\massim_2022\server\conf\Turnier4Config.json --monitor'

Start-Sleep -s 1

Write-Host "Starting Agents"
Start-Process -FilePath java -ArgumentList '-jar .\massim_2022\javaagents\target\javaagents-2022-1.1-jar-with-dependencies.jar .\massim_2022\javaagents\conf\NextAgentsTurnierLocal'

Exit
