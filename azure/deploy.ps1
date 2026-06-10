<#
.SYNOPSIS
Script de despliegue automatizado para Clash Manager en Azure Container Apps.
Este script creará toda la infraestructura necesaria, compilará las imágenes en la nube
y desplegará los contenedores en orden.

.PREREQUISITES
1. Tener instalada la CLI de Azure (az cli)
2. Haber ejecutado 'az login' antes de correr este script.
#>

$ErrorActionPreference = "Stop"

# ==============================================================================
# CONFIGURACIÓN DE VARIABLES GLOBALES
# ==============================================================================
# Cambia 'tfg' por tus iniciales si los nombres ya están en uso (deben ser únicos globales)
$SUFFIX = "70551"
$RESOURCE_GROUP = "rg-clashmanager"
$LOCATION = "spaincentral"
$ACR_NAME = "acrclashmanager$SUFFIX"
$DB_SERVER_NAME = "mysql-clashmanager-$SUFFIX"
$DB_ADMIN = "tfg_user"
$DB_PASS = "Tfg_Password_2026!"
$ENV_NAME = "env-clashmanager"
$JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Iniciando Despliegue de Clash Manager en Azure" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Resource Group: $RESOURCE_GROUP"
Write-Host "Ubicación: $LOCATION"
Write-Host "Registro de Contenedores: $ACR_NAME"
Write-Host "Servidor MySQL: $DB_SERVER_NAME"

Write-Host "`n[0/7] Registrando proveedores de Azure necesarios..." -ForegroundColor Yellow
az provider register -n Microsoft.OperationalInsights --wait
az provider register -n Microsoft.App --wait
az provider register -n Microsoft.DBforMySQL --wait
az provider register -n Microsoft.ContainerRegistry --wait

# 1. Crear Grupo de Recursos
Write-Host "`n[1/7] Verificando Grupo de Recursos..." -ForegroundColor Yellow
$rgExists = az group exists --name $RESOURCE_GROUP
if ($rgExists -eq "false") {
    az group create --name $RESOURCE_GROUP --location $LOCATION
} else {
    Write-Host "  -> El Grupo de Recursos ya existe."
}

# 2. Reusar Servidor MySQL Existente
Write-Host "`n[2/7] Reusando Servidor MySQL existente ($DB_SERVER_NAME)..." -ForegroundColor Yellow

# 3. Crear Azure Container Registry (ACR)
Write-Host "`n[3/7] Creando Container Registry ($ACR_NAME) en $LOCATION..." -ForegroundColor Yellow
az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true --location $LOCATION
$ACR_LOGIN_SERVER = az acr show --name $ACR_NAME --query loginServer --output tsv

# 4. Crear Azure Container Apps Environment
Write-Host "`n[4/7] Creando Container Apps Environment..." -ForegroundColor Yellow
az containerapp env create --name $ENV_NAME --resource-group $RESOURCE_GROUP --location $LOCATION

# 5. Compilar proyecto Java localmente y crear Imágenes Docker
Write-Host "`n[5/7] Compilando proyecto Java localmente con Maven..." -ForegroundColor Yellow
mvn clean package -f ./backend/pom.xml -DskipTests

Write-Host "`nIniciando sesión en Azure Container Registry..." -ForegroundColor Yellow
az acr login --name $ACR_NAME

Write-Host "`nCompilando imágenes de Docker y subiendo a ACR..." -ForegroundColor Yellow
$services = @("eureka-server", "api-gateway", "auth-service", "club-service", "league-service", "transfer-service")
foreach ($svc in $services) {
    Write-Host "  -> Construyendo imagen para $svc..."
    docker build -t "$ACR_LOGIN_SERVER/$($svc):latest" -f "./backend/$svc/Dockerfile" ./backend
    Write-Host "  -> Subiendo $svc a ACR..."
    docker push "$ACR_LOGIN_SERVER/$($svc):latest"
}

# 6. Desplegar Eureka Server primero
Write-Host "`n[6/7] Desplegando Eureka Server..." -ForegroundColor Yellow
az containerapp create `
    --name eureka-server `
    --resource-group $RESOURCE_GROUP `
    --environment $ENV_NAME `
    --image "$ACR_LOGIN_SERVER/eureka-server:latest" `
    --target-port 8761 `
    --ingress internal `
    --registry-server $ACR_LOGIN_SERVER `
    --min-replicas 1

$EUREKA_FQDN = az containerapp show --name eureka-server --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv
$EUREKA_URL = "https://$($EUREKA_FQDN)/eureka/"
Write-Host "  -> Eureka URL Interna: $EUREKA_URL" -ForegroundColor Green

# Desplegar el resto de microservicios (excepto gateway)
$internalServices = @("auth-service", "club-service", "league-service", "transfer-service")
foreach ($svc in $internalServices) {
    Write-Host "  -> Desplegando $svc..."
    
    # Hemos eliminado el símbolo ampersand (&) de la URL para evitar errores de parseo en CMD/PowerShell al usar az cli
    $DB_URL = "jdbc:mysql://$DB_SERVER_NAME.mysql.database.azure.com:3306/$($svc.Replace('-service',''))_db?createDatabaseIfNotExist=true"

    az containerapp create `
        --name $svc `
        --resource-group $RESOURCE_GROUP `
        --environment $ENV_NAME `
        --image "$ACR_LOGIN_SERVER/$($svc):latest" `
        --target-port 8080 `
        --ingress internal `
        --registry-server $ACR_LOGIN_SERVER `
        --min-replicas 1 `
        --env-vars "SPRING_DATASOURCE_URL=$DB_URL" "SPRING_DATASOURCE_USERNAME=$DB_ADMIN" "SPRING_DATASOURCE_PASSWORD=$DB_PASS" "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=$EUREKA_URL" "JWT_SECRET=$JWT_SECRET" "SERVER_PORT=8080" "CLUB_SERVICE_URL=http://club-service" "AUTH_SERVICE_URL=http://auth-service" "LEAGUE_SERVICE_URL=http://league-service" "TRANSFER_SERVICE_URL=http://transfer-service"
}

# Desplegar API Gateway (Público)
Write-Host "  -> Desplegando api-gateway..."
az containerapp create `
    --name api-gateway `
    --resource-group $RESOURCE_GROUP `
    --environment $ENV_NAME `
    --image "$ACR_LOGIN_SERVER/api-gateway:latest" `
    --target-port 8080 `
    --ingress external `
    --registry-server $ACR_LOGIN_SERVER `
    --min-replicas 1 `
    --env-vars "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=$EUREKA_URL" "JWT_SECRET=$JWT_SECRET"

$GATEWAY_FQDN = az containerapp show --name api-gateway --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv
$GATEWAY_URL = "https://$GATEWAY_FQDN/"
Write-Host "  -> API Gateway URL Pública: $GATEWAY_URL" -ForegroundColor Green

# 7. Compilar y Desplegar el Frontend React (Inyectando la URL del Gateway)
Write-Host "`n[7/7] Compilando y Desplegando el Frontend React..." -ForegroundColor Yellow
Write-Host "  -> Compilando frontend localmente inyectando VITE_API_URL=$GATEWAY_URL..."
docker build -t "$ACR_LOGIN_SERVER/frontend:latest" --build-arg "VITE_API_URL=$GATEWAY_URL" ./frontend
Write-Host "  -> Subiendo frontend a ACR..."
docker push "$ACR_LOGIN_SERVER/frontend:latest"

Write-Host "  -> Desplegando frontend..."
az containerapp create `
    --name frontend `
    --resource-group $RESOURCE_GROUP `
    --environment $ENV_NAME `
    --image "$ACR_LOGIN_SERVER/frontend:latest" `
    --target-port 80 `
    --ingress external `
    --registry-server $ACR_LOGIN_SERVER `
    --min-replicas 1

$FRONTEND_FQDN = az containerapp show --name frontend --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv
$FRONTEND_URL = "https://$FRONTEND_FQDN"

Write-Host "`n==============================================" -ForegroundColor Cyan
Write-Host "¡DESPLIEGUE COMPLETADO CON ÉXITO!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Tu aplicación Clash Manager está disponible públicamente en:"
Write-Host "👉 $FRONTEND_URL" -ForegroundColor Magenta
Write-Host "El API Gateway está disponible en:"
Write-Host "👉 $GATEWAY_URL" -ForegroundColor Magenta
Write-Host "==============================================" -ForegroundColor Cyan
