#!/bin/bash

# YMS Kafka API Test Script
# Testet alle API-Endpunkte mit den Beispieldaten

API_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}YMS Kafka API - Test Script${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. Health Check
echo -e "${BLUE}1. Health Check...${NC}"
curl -s -X GET "${API_URL}/api/yms/health" | jq '.'
echo ""
echo ""

# 2. Lieferung erstellen
echo -e "${BLUE}2. Lieferung erstellen...${NC}"
curl -s -X POST "${API_URL}/api/yms/delivery" \
  -H "Content-Type: application/json" \
  -d '{
    "ymsReference": "123456789",
    "plannedDeliveryDate": "2025-11-03",
    "plannedDeliveryTime": "10:30",
    "plannedWeightKg": 12500,
    "targetLocation": "S1",
    "materialType": "1002003",
    "supplier": 987,
    "isWarehhouse": 1,
    "warehouseInfo": "Außenlager",
    "isExternalWarehouse": 0,
    "externalWarehouseInfo": "",
    "remark": "Bemerkungstext"
  }' | jq '.'
echo ""
echo ""

# 3. Eingangs-Wiegung
echo -e "${BLUE}3. Eingangs-Wiegung (weighingType: 1)...${NC}"
curl -s -X POST "${API_URL}/api/yms/weighing" \
  -H "Content-Type: application/json" \
  -d '{
    "ymsReference": "123456789",
    "truckLicensePlate": "EF-AB 123",
    "weightKg": 12340,
    "registrationNumber": 15,
    "weighingType": 1,
    "timestamp": "2025-11-03T07:58:12Z"
  }' | jq '.'
echo ""
echo ""

# 4. Ausgangs-Wiegung
echo -e "${BLUE}4. Ausgangs-Wiegung (weighingType: 2)...${NC}"
curl -s -X POST "${API_URL}/api/yms/weighing" \
  -H "Content-Type: application/json" \
  -d '{
    "ymsReference": "123456789",
    "truckLicensePlate": "EF-AB 123",
    "weightKg": 33340,
    "registrationNumber": 19,
    "weighingType": 2,
    "timestamp": "2025-11-03T11:58:12Z"
  }' | jq '.'
echo ""
echo ""

# 5. Test mit ungültigen Daten (Validierung)
echo -e "${BLUE}5. Test mit ungültigen Daten (Validierung)...${NC}"
curl -s -X POST "${API_URL}/api/yms/weighing" \
  -H "Content-Type: application/json" \
  -d '{
    "ymsReference": "",
    "truckLicensePlate": "EF-AB 123",
    "weightKg": -100,
    "registrationNumber": 15,
    "weighingType": 5,
    "timestamp": "2025-11-03T07:58:12Z"
  }' | jq '.'
echo ""
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Tests abgeschlossen!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Kafka Messages überprüfen:"
echo "- Kafka UI: http://localhost:8090"
echo "- Oder mit CLI: docker exec -it yms-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic yms.delivery --from-beginning"
