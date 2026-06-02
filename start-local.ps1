$ErrorActionPreference = "Stop"

Write-Host "Starting Eureka Server..."
Start-Process -FilePath "java" -ArgumentList "-jar backend\eureka-server\target\eureka-server-1.0.0-SNAPSHOT.jar" -WindowStyle Minimized

Write-Host "Waiting 10 seconds for Eureka to initialize..."
Start-Sleep -Seconds 10

Write-Host "Starting Auth Service..."
Start-Process -FilePath "java" -ArgumentList "-jar backend\auth-service\target\auth-service-1.0.0-SNAPSHOT.jar" -WindowStyle Minimized

Write-Host "Starting Club Service..."
Start-Process -FilePath "java" -ArgumentList "-jar backend\club-service\target\club-service-1.0.0-SNAPSHOT.jar" -WindowStyle Minimized

Write-Host "Starting League Service..."
Start-Process -FilePath "java" -ArgumentList "-jar backend\league-service\target\league-service-1.0.0-SNAPSHOT.jar" -WindowStyle Minimized

Write-Host "Starting Transfer Service..."
Start-Process -FilePath "java" -ArgumentList "-jar backend\transfer-service\target\transfer-service-1.0.0-SNAPSHOT.jar" -WindowStyle Minimized

Write-Host "Waiting 15 seconds for microservices to register in Eureka..."
Start-Sleep -Seconds 15

Write-Host "Starting API Gateway..."
Start-Process -FilePath "java" -ArgumentList "-jar backend\api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar" -WindowStyle Minimized

Write-Host "Installing Frontend dependencies..."
Start-Process -FilePath "npm" -ArgumentList "install" -WorkingDirectory "frontend" -Wait

Write-Host "Starting Frontend (Vite)..."
Start-Process -FilePath "npm.cmd" -ArgumentList "run dev -- --host" -WorkingDirectory "frontend"

Write-Host "All services started! The frontend should be available at http://localhost:5173"
Write-Host "To stop them, you can close the opened Java and Node command windows."
