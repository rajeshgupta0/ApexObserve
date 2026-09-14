for ($i=1; $i -le 10; $i++) {
    Write-Host "Sending normal checkout $i"
    Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/api/demo/checkout' -Body '{"userId":"user-1","items":["item-1","item-2"]}' -ContentType 'application/json'
    Start-Sleep -Milliseconds 500
}

for ($i=1; $i -le 5; $i++) {
    Write-Host "Sending error checkout $i"
    try {
        Invoke-RestMethod -Method Post -Uri 'http://localhost:9000/api/demo/checkout?simulateError=true' -Body '{"userId":"user-2","items":["item-error"]}' -ContentType 'application/json'
    } catch {
        Write-Host "Error checkout completed with expected failure"
    }
    Start-Sleep -Milliseconds 500
}
