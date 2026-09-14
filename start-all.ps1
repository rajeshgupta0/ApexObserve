$services = @(
    "backend/api-gateway",
    "backend/ingestion-service",
    "backend/processing-service",
    "backend/query-service",
    "backend/alerting-service",
    "demo-services/api-gateway-demo",
    "demo-services/user",
    "demo-services/order",
    "demo-services/payment",
    "demo-services/inventory",
    "demo-services/notification"
)

foreach ($service in $services) {
    Write-Host "Starting $service..."
    Start-Job -Name $service -ScriptBlock { param($s); Set-Location "C:\ByteRanger\Codes\WorkSpace\OBS"; .\mvnw.cmd -pl $s spring-boot:run > "$($s.Replace('/', '_')).log" 2>&1 } -ArgumentList $service
}
Write-Host "All services started."
Wait-Job *
