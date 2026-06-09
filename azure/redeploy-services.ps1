<#
.SYNOPSIS
Redeploy solo los servicios con errores de Flyway
#>
$ErrorActionPreference = "Stop"

$SUFFIX = "70551"
$RESOURCE_GROUP = "rg-clashmanager"
$ACR_NAME = "acrclashmanager$SUFFIX"
$ENV_NAME = "env-clashmanager"
$DB_SERVER_NAME = "mysql-clashmanager-$SUFFIX"
$DB_ADMIN = "tfg_user"
$DB_PASS = "Tfg_Password_2026!"
$JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

$ACR_LOGIN_SERVER = az acr show --name $ACR_NAME --query loginServer --output tsv
$EUREKA_FQDN = az containerapp show --name eureka-server --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv
$EUREKA_URL = "https://$($EUREKA_FQDN)/eureka/"

Write-Host "ACR: $ACR_LOGIN_SERVER"
Write-Host "Eureka URL: $EUREKA_URL"

Write-Host "`n[1/3] Compilando proyecto Maven (solo los servicios afectados)..." -ForegroundColor Yellow
mvn clean package -f ./backend/pom.xml -DskipTests -pl club-service,league-service,transfer-service -am

Write-Host "`n[2/3] Login en ACR..." -ForegroundColor Yellow
az acr login --name $ACR_NAME

Write-Host "`n[3/3] Rebuild y redeploy de los servicios afectados..." -ForegroundColor Yellow
$services = @("club-service", "league-service", "transfer-service")
foreach ($svc in $services) {
    Write-Host "`n  -> Construyendo imagen $svc..." -ForegroundColor Cyan
    docker build -t "$ACR_LOGIN_SERVER/$($svc):latest" -f "./backend/$svc/Dockerfile" ./backend
    
    Write-Host "  -> Subiendo $svc a ACR..." -ForegroundColor Cyan
    docker push "$ACR_LOGIN_SERVER/$($svc):latest"

    $DB_URL = "jdbc:mysql://$DB_SERVER_NAME.mysql.database.azure.com:3306/$($svc.Replace('-service',''))_db?createDatabaseIfNotExist=true"

    Write-Host "  -> Actualizando container app $svc..." -ForegroundColor Cyan
    az containerapp update `
        --name $svc `
        --resource-group $RESOURCE_GROUP `
        --image "$ACR_LOGIN_SERVER/$($svc):latest" `
        --set-env-vars "SPRING_DATASOURCE_URL=$DB_URL" "SPRING_DATASOURCE_USERNAME=$DB_ADMIN" "SPRING_DATASOURCE_PASSWORD=$DB_PASS" "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=$EUREKA_URL" "JWT_SECRET=$JWT_SECRET" "SERVER_PORT=8080"

    Write-Host "  -> $svc actualizado OK" -ForegroundColor Green
}

Write-Host "`n=== Redeploy completado! ===" -ForegroundColor Green
Write-Host "Esperando 30s para que los servicios arranquen..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "`nVerificando que los servicios estan en Eureka..."
$FRONTEND_URL = "https://frontend.happyrock-6898a204.spaincentral.azurecontainerapps.io"
$GW_URL = "https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io"
Write-Host "Frontend: $FRONTEND_URL" -ForegroundColor Magenta
Write-Host "API Gateway: $GW_URL" -ForegroundColor Magenta
