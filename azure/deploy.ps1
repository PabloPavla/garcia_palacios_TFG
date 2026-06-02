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
$SUFFIX = Get-Random -Maximum 99999
$RESOURCE_GROUP = "rg-clashmanager"
$LOCATION = "westeurope"
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

# 1. Crear Grupo de Recursos
Write-Host "`n[1/7] Creando Grupo de Recursos..." -ForegroundColor Yellow
az group create --name $RESOURCE_GROUP --location $LOCATION | Out-Null

# 2. Crear Base de Datos MySQL Flexible Server
Write-Host "`n[2/7] Creando Servidor MySQL (Puede tardar de 5 a 10 minutos)..." -ForegroundColor Yellow
az mysql flexible-server create `
    --name $DB_SERVER_NAME `
    --resource-group $RESOURCE_GROUP `
    --location $LOCATION `
    --admin-user $DB_ADMIN `
    --admin-password $DB_PASS `
    --sku-name Standard_B1ms `
    --tier Burstable `
    --public-access 0.0.0.0 `
    --yes

# 3. Crear Azure Container Registry (ACR)
Write-Host "`n[3/7] Creando Container Registry ($ACR_NAME)..." -ForegroundColor Yellow
az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true | Out-Null
$ACR_LOGIN_SERVER = az acr show --name $ACR_NAME --query loginServer --output tsv

# 4. Crear Azure Container Apps Environment
Write-Host "`n[4/7] Creando Container Apps Environment..." -ForegroundColor Yellow
az containerapp env create --name $ENV_NAME --resource-group $RESOURCE_GROUP --location $LOCATION | Out-Null

# 5. Compilar Imágenes en la nube (ACR Build)
Write-Host "`n[5/7] Compilando imágenes del Backend en la nube..." -ForegroundColor Yellow

$services = @("eureka-server", "api-gateway", "auth-service", "club-service", "league-service", "transfer-service")
foreach ($svc in $services) {
    Write-Host "  -> Compilando $svc..."
    az acr build --registry $ACR_NAME --image "$svc:latest" "backend/$svc" | Out-Null
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
    --registry-server $ACR_LOGIN_SERVER | Out-Null

$EUREKA_FQDN = az containerapp show --name eureka-server --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv
$EUREKA_URL = "http://$($EUREKA_FQDN)/eureka/"
Write-Host "  -> Eureka URL Interna: $EUREKA_URL" -ForegroundColor Green

# Desplegar el resto de microservicios (excepto gateway)
$internalServices = @("auth-service", "club-service", "league-service", "transfer-service")
foreach ($svc in $internalServices) {
    Write-Host "  -> Desplegando $svc..."
    
    $DB_URL = "jdbc:mysql://$DB_SERVER_NAME.mysql.database.azure.com:3306/$($svc.Replace('-service',''))_db?createDatabaseIfNotExist=true&useSSL=true&requireSSL=false"

    az containerapp create `
        --name $svc `
        --resource-group $RESOURCE_GROUP `
        --environment $ENV_NAME `
        --image "$ACR_LOGIN_SERVER/$svc:latest" `
        --target-port 8080 `
        --ingress internal `
        --registry-server $ACR_LOGIN_SERVER `
        --env-vars "SPRING_DATASOURCE_URL=$DB_URL" "SPRING_DATASOURCE_USERNAME=$DB_ADMIN" "SPRING_DATASOURCE_PASSWORD=$DB_PASS" "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=$EUREKA_URL" "JWT_SECRET=$JWT_SECRET" | Out-Null
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
    --env-vars "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=$EUREKA_URL" "JWT_SECRET=$JWT_SECRET" | Out-Null

$GATEWAY_FQDN = az containerapp show --name api-gateway --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv
$GATEWAY_URL = "https://$GATEWAY_FQDN/"
Write-Host "  -> API Gateway URL Pública: $GATEWAY_URL" -ForegroundColor Green

# 7. Compilar y Desplegar el Frontend React (Inyectando la URL del Gateway)
Write-Host "`n[7/7] Compilando y Desplegando el Frontend React..." -ForegroundColor Yellow
Write-Host "  -> Compilando frontend inyectando VITE_API_URL=$GATEWAY_URL..."
az acr build --registry $ACR_NAME --image "frontend:latest" --build-arg "VITE_API_URL=$GATEWAY_URL" frontend | Out-Null

Write-Host "  -> Desplegando frontend..."
az containerapp create `
    --name frontend `
    --resource-group $RESOURCE_GROUP `
    --environment $ENV_NAME `
    --image "$ACR_LOGIN_SERVER/frontend:latest" `
    --target-port 80 `
    --ingress external `
    --registry-server $ACR_LOGIN_SERVER | Out-Null

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
