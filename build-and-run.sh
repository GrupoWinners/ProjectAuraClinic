#!/bin/bash

echo "========================================"
echo " Aura Clinic - Build e Inicialização "
echo "========================================"

echo ""
echo "Instalando commons..."
cd commons || exit
mvn clean install -DskipTests

echo ""
echo "Voltando para raiz..."
cd ..

echo ""
echo "Subindo containers Docker..."
docker compose up -d --build

echo ""
echo "Containers ativos:"
docker compose ps

echo ""
echo "Swagger URLs:"
echo "Admin:         http://localhost:8081/swagger-ui.html"
echo "Agendamento:   http://localhost:8082/swagger-ui.html"
echo "Atendimento:   http://localhost:8083/swagger-ui.html"

echo ""
echo "Aura Clinic iniciado com sucesso"