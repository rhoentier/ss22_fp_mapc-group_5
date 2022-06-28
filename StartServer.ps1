Set-Location -Path $PSScriptRoot

Write-Host "Starting Server"
Start-Process -FilePath java -ArgumentList '-jar .\massim_2022\server\target\server-2020-2.0-jar-with-dependencies.jar -conf .\massim_2022\server\conf\MiriConfig.json --monitor'