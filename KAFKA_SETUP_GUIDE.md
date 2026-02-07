# Apache Kafka & Kafdrop Setup Guide for NovaPay

## Prerequisites
- Java 11+ (you likely have this for Spring Boot)
- Windows PowerShell

---

## Step 1: Download Apache Kafka

1. **Go to**: https://kafka.apache.org/downloads
2. **Download**: Latest stable version (e.g., `kafka_2.13-3.7.0.tgz` or `.zip`)
3. **Extract** to a location (e.g., `C:\kafka`)

### Quick Download via PowerShell:
```powershell
# Create kafka directory
New-Item -ItemType Directory -Path "C:\kafka" -Force

# Download Kafka (replace version if newer)
$url = "https://archive.apache.org/dist/kafka/3.7.0/kafka_2.13-3.7.0.tgz"
$output = "C:\kafka.tgz"
Invoke-WebRequest -Uri $url -OutFile $output

# Extract (you'll need 7-Zip or similar)
# Or download from the website and extract manually
```

---

## Step 2: Start Zookeeper (Required by Kafka)

Kafka needs Zookeeper to run. Open **PowerShell** and run:

```powershell
cd C:\kafka\kafka_2.13-3.7.0

# Start Zookeeper
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
```

✅ Wait for message: `[main] INFO Zookeeper server started`

Keep this terminal **running**.

---

## Step 3: Start Apache Kafka (New PowerShell Window)

Open a **new PowerShell window** and run:

```powershell
cd C:\kafka\kafka_2.13-3.7.0

# Start Kafka Broker
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

✅ Wait for message: `[KafkaServer] INFO [KafkaServer id=0] started`

Keep this terminal **running**.

---

## Step 4: Download & Run Kafdrop (Web UI)

Kafdrop is the easiest web UI for Kafka. Open a **third PowerShell window**:

```powershell
# Create a directory for Kafdrop
New-Item -ItemType Directory -Path "C:\kafdrop" -Force
cd C:\kafdrop

# Download Kafdrop jar (latest version)
$url = "https://github.com/obsidiandynamics/kafdrop/releases/download/v4.0.1/kafdrop-4.0.1-exec.jar"
$output = "C:\kafdrop\kafdrop.jar"
Invoke-WebRequest -Uri $url -OutFile $output

# Run Kafdrop
java -jar kafdrop.jar --kafka.brokers=localhost:9092
```

✅ Wait for message: `Started KafdropApplication`

---

## Step 5: Access Kafdrop Web UI

Open your browser and go to:
```
http://localhost:9000
```

You should see:
- ✅ Kafka cluster information
- ✅ List of topics
- ✅ Option to create topics
- ✅ Real-time message viewing

---

## Creating Topics in Kafdrop

1. Go to **http://localhost:9000**
2. Click **"Topic"** in the menu
3. Click **"Create topic"**
4. Enter:
   - **Topic name**: `wallet-transfer-events` (or any name)
   - **Partitions**: 3
   - **Replication factor**: 1
5. Click **"Create"**

---

## Alternative: Create Topics via Command Line

If you want to create topics from PowerShell:

```powershell
cd C:\kafka\kafka_2.13-3.7.0

# Create a topic
.\bin\windows\kafka-topics.bat --create --topic wallet-transfer-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# List all topics
.\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

# Describe a topic
.\bin\windows\kafka-topics.bat --describe --topic wallet-transfer-events --bootstrap-server localhost:9092

# Delete a topic
.\bin\windows\kafka-topics.bat --delete --topic wallet-transfer-events --bootstrap-server localhost:9092
```

---

## Testing Kafka with Your NovaPay App

Your `application.properties` is already configured correctly:
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Just **start your Spring Boot application** and it will automatically connect to Kafka!

---

## Monitoring in Kafdrop

In the Kafdrop web UI, you can:

✅ **View Topics** - See all topics and their details
✅ **View Messages** - Browse messages in real-time
✅ **Monitor Consumers** - See consumer groups and lag
✅ **View Brokers** - Check broker health and status
✅ **ACLs** - Manage access control (if enabled)

---

## Troubleshooting

### Kafka won't start
- Make sure Zookeeper is running first
- Check Java is installed: `java -version`
- Ensure no other service is using port 9092

### Kafdrop shows "No brokers available"
- Verify Kafka is running
- Check Kafka is listening on 9092: `netstat -ano | findstr :9092`
- Restart Kafdrop

### Port already in use
```powershell
# Find what's using port 9092
netstat -ano | findstr :9092

# Kill the process (replace PID)
taskkill /PID <PID> /F
```

---

## Quick Start Checklist

- [ ] Download and extract Kafka
- [ ] Start Zookeeper (`zookeeper-server-start.bat`)
- [ ] Start Kafka (`kafka-server-start.bat`)
- [ ] Download Kafdrop jar
- [ ] Start Kafdrop (`java -jar kafdrop.jar ...`)
- [ ] Open http://localhost:9000
- [ ] Create test topics
- [ ] Update `application.properties` if needed
- [ ] Start your Spring Boot app
- [ ] Monitor messages in Kafdrop

---

## Useful Links

- Kafka Download: https://kafka.apache.org/downloads
- Kafdrop GitHub: https://github.com/obsidiandynamics/kafdrop
- Kafka Documentation: https://kafka.apache.org/documentation/
