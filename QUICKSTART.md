# 🚀 YMS Kafka API - Schnellstart

## Setup in 5 Schritten

### 1️⃣ Kafka starten
```bash
docker-compose up -d
```
✅ Kafka läuft auf: `localhost:9092`  
✅ Kafka UI läuft auf: http://localhost:8090

### 2️⃣ Anwendung bauen
```bash
mvn clean package
```

### 3️⃣ Anwendung starten
```bash
mvn spring-boot:run
```
✅ API läuft auf: http://localhost:8080

### 4️⃣ API testen
```bash
# Mit Test-Script
./test-api.sh

# Oder manuell
curl http://localhost:8080/api/yms/health
```

### 5️⃣ Kafka Messages prüfen
- **Browser:** http://localhost:8090
- **CLI:** 
  ```bash
  docker exec -it yms-kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic yms.delivery \
    --from-beginning
  ```

---

## 📌 Wichtige URLs

| Service | URL |
|---------|-----|
| REST API | http://localhost:8080 |
| Health Check | http://localhost:8080/api/yms/health |
| Kafka UI | http://localhost:8090 |

---

## 🛑 Alles stoppen

```bash
# Spring Boot stoppen
Ctrl + C

# Kafka stoppen
docker-compose down

# Kafka inkl. Daten löschen
docker-compose down -v
```

---

## 📝 Beispiel Requests

### Lieferung
```bash
curl -X POST http://localhost:8080/api/yms/delivery \
  -H "Content-Type: application/json" \
  -d '{"ymsReference":"123456789","plannedDeliveryDate":"2025-11-03","plannedDeliveryTime":"10:30","plannedWeightKg":12500,"targetLocation":"S1","materialType":"1002003","supplier":987,"isWarehhouse":1,"warehouseInfo":"Außenlager","isExternalWarehouse":0,"externalWarehouseInfo":"","remark":"Bemerkungstext"}'
```

### Eingangs-Wiegung
```bash
curl -X POST http://localhost:8080/api/yms/weighing \
  -H "Content-Type: application/json" \
  -d '{"ymsReference":"123456789","truckLicensePlate":"EF-AB 123","weightKg":12340,"registrationNumber":15,"weighingType":1,"timestamp":"2025-11-03T07:58:12Z"}'
```

### Ausgangs-Wiegung
```bash
curl -X POST http://localhost:8080/api/yms/weighing \
  -H "Content-Type: application/json" \
  -d '{"ymsReference":"123456789","truckLicensePlate":"EF-AB 123","weightKg":33340,"registrationNumber":19,"weighingType":2,"timestamp":"2025-11-03T11:58:12Z"}'
```

---

## 🔧 Troubleshooting

**Problem:** Kafka Connection Failed  
**Lösung:** `docker-compose ps` → Stelle sicher, dass Kafka läuft

**Problem:** Port 8080 bereits belegt  
**Lösung:** In `application.properties` Port ändern: `server.port=8081`

**Problem:** Maven Build Error  
**Lösung:** Java Version prüfen: `java -version` (min. Java 17)

---

## 📦 Kafka Topics

- `yms.delivery` - Lieferungen
- `yms.weighing.in` - Eingangs-Wiegungen
- `yms.weighing.out` - Ausgangs-Wiegungen

---

## 🎯 Nächste Schritte

- [ ] Postman Collection importieren (`YMS-Kafka-API.postman_collection.json`)
- [ ] Kafka Consumer implementieren
- [ ] Monitoring mit Spring Boot Actuator hinzufügen
- [ ] Integration Tests schreiben
- [ ] Production-Config erstellen

---

**📖 Vollständige Dokumentation:** Siehe `README.md`
