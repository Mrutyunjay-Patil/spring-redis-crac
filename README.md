# Spring Boot Redis CRaC Demo

A comprehensive Spring Boot 3.4.6 application demonstrating **Java CRaC (Coordinated Restore at Checkpoint)** functionality with Redis integration. This project showcases how to achieve significant startup performance improvements using CRaC while maintaining Redis cache persistence across checkpoint/restore cycles.

## 🚀 Key Features

- **CRaC Integration**: Java application checkpointing and restore capabilities
- **Redis Cache Persistence**: Data survives checkpoint/restore cycles
- **Spring Boot 3.4.6**: Latest Spring Boot with built-in CRaC support
- **Lettuce Redis Client**: CRaC-compatible Redis client (non-blocking)
- **RESTful APIs**: Complete CRUD operations for cache management
- **Health Monitoring**: Redis connectivity and application health checks
- **API Documentation**: Interactive Swagger UI with OpenAPI 3 spec
- **Multi-Profile Support**: Development, test, and production configurations

## 📊 Performance Benefits

- **Cold Start**: ~12-14 seconds (initial application startup)
- **CRaC Restore**: ~1 second (92% faster startup time)
- **Cache Persistence**: Data maintained across checkpoint/restore cycles
- **Production Ready**: Graceful shutdown and proper connection management

## 🏗️ Architecture

```
├── src/main/java/com/example/springrediscrac/
│   ├── SpringRedisCracApplication.java      # Main application entry point
│   ├── config/
│   │   ├── RedisConfig.java                 # Redis Template configuration
│   │   └── OpenApiConfig.java               # Swagger/OpenAPI setup
│   ├── controller/
│   │   ├── CacheController.java             # Cache CRUD operations
│   │   ├── CatalogController.java           # Demo cached operations
│   │   ├── HealthController.java            # Redis health checks
│   │   └── AdminController.java             # CRaC checkpoint trigger
│   ├── service/
│   │   ├── CacheService.java                # Cache business logic
│   │   ├── CatalogService.java              # Demo cached service
│   │   └── RedisHealthService.java          # Health monitoring
│   ├── model/
│   │   └── CacheItem.java                   # Cache data model
│   └── crac/
│       └── RedisCracResource.java           # CRaC lifecycle logging
└── src/main/resources/
    └── application.yml                      # Multi-profile configuration
```

## 🛠️ Prerequisites

- **Java 17**: CRaC-enabled JDK (e.g., Azul Zulu with CRaC support)
- **Maven 3.9+**: Build tool
- **Redis Server**: Running locally or accessible via network
- **Linux Environment**: CRaC requires Linux for checkpoint/restore operations

### Installing CRaC-Enabled Java

```bash
# Download Azul Zulu with CRaC support
wget https://cdn.azul.com/zulu/bin/zulu17...
tar -xzf zulu17...
export JAVA_HOME=/path/to/zulu17...
export PATH=$JAVA_HOME/bin:$PATH
```

### Starting Redis

```bash
# Using Docker
docker run -d --name redis -p 6379:6379 redis:latest

# Or install locally
sudo apt-get install redis-server
redis-server
```

## 🚀 Quick Start

### 1. Clone and Build

```bash
git clone https://github.com/Mrutyunjay-Patil/spring-redis-crac.git
cd spring-redis-crac
mvn clean package -DskipTests
```

### 2. Run Application

```bash
# Start with CRaC support
java -XX:CRaCCheckpointTo=cr -jar target/spring-redis-crac-0.0.1-SNAPSHOT.jar
```

### 3. Verify Application

```bash
# Check health
curl http://localhost:8080/actuator/health

# View API docs
open http://localhost:8080/swagger-ui.html
```

## 📚 API Documentation

### Cache Operations (`/api/cache`)

| Method     | Endpoint             | Description           |
| ---------- | -------------------- | --------------------- |
| `GET`    | `/api/cache/{key}` | Retrieve cached value |
| `POST`   | `/api/cache`       | Store key-value pair  |
| `PUT`    | `/api/cache/{key}` | Update cached value   |
| `DELETE` | `/api/cache/{key}` | Remove cached value   |
| `GET`    | `/api/cache`       | List all cached keys  |
| `DELETE` | `/api/cache`       | Clear all cache       |

### Catalog Operations (`/api/catalog`)

| Method   | Endpoint              | Description                 |
| -------- | --------------------- | --------------------------- |
| `GET`  | `/api/catalog/{id}` | Get cached catalog item     |
| `POST` | `/api/catalog/{id}` | Update catalog with caching |

### Health Monitoring (`/health`)

| Method  | Endpoint             | Description                |
| ------- | -------------------- | -------------------------- |
| `GET` | `/health/redis`    | Redis connectivity status  |
| `GET` | `/actuator/health` | Overall application health |

### CRaC Administration (`/admin`)

| Method   | Endpoint              | Description             |
| -------- | --------------------- | ----------------------- |
| `POST` | `/admin/checkpoint` | Trigger CRaC checkpoint |

## 🧪 CRaC Workflow

### Creating a Checkpoint

1. **Start Application**:

   ```bash
   java -XX:CRaCCheckpointTo=cr -jar target/spring-redis-crac-0.0.1-SNAPSHOT.jar
   ```
2. **Add Cache Data**:

   ```bash
   # Store some data
   curl -X POST http://localhost:8080/api/cache \
     -H "Content-Type: application/json" \
     -d '{"key": "user:123", "value": "John Doe"}'

   # Store catalog data
   curl -X POST http://localhost:8080/api/catalog/item456 \
     -H "Content-Type: application/json" \
     -d '"Premium Widget"'
   ```
3. **Create Checkpoint**:

   ```bash
   # Via API
   curl -X POST http://localhost:8080/admin/checkpoint

   # Or via jcmd
   jcmd spring-redis-crac JDK.checkpoint
   ```

### Restoring from Checkpoint

```bash
# Restore application (fast startup!)
java -XX:CRaCRestoreFrom=cr
```

### Verify Data Persistence

```bash
# Check that data survived checkpoint/restore
curl http://localhost:8080/api/cache/user:123
curl http://localhost:8080/api/catalog/item456
```

## 🔧 Configuration

### Environment Variables

| Variable              | Default       | Description               |
| --------------------- | ------------- | ------------------------- |
| `SPRING_REDIS_HOST` | `localhost` | Redis server hostname     |
| `SPRING_REDIS_PORT` | `6379`      | Redis server port         |
| `REDIS_SSL_ENABLED` | `false`     | Enable SSL for production |

## 📈 Performance Monitoring

### Startup Time Comparison

```bash
# Measure cold start
time java -jar target/spring-redis-crac-0.0.1-SNAPSHOT.jar

# Measure CRaC restore
time java -XX:CRaCRestoreFrom=cr
```

### Expected Results

- **Cold Start**: ~12-14 seconds
- **CRaC Restore**: ~1 second
- **Performance Gain**: ~92% faster startup

### Memory Usage

```bash
# Monitor memory during operation
jcmd <pid> VM.info
jcmd <pid> GC.run_finalization
```

## 🔍 Troubleshooting

### Common Issues

1. **IllegalSelectorException during checkpoint**:

   - Ensure using Lettuce (not Redisson) Redis client
   - Spring Boot 3.4.6+ has built-in CRaC support for Lettuce
2. **Connection failures after restore**:

   - Verify Redis server is running
   - Check network connectivity
   - Review logs for connection pool issues
3. **Cache data not persisting**:

   - Ensure using proper Spring Cache annotations
   - Verify Redis persistence settings
   - Check cache configuration in application.yml

### Debug Logging

```bash
# Enable debug logging
java -Dlogging.level.com.example.springrediscrac=DEBUG \
     -XX:CRaCCheckpointTo=cr \
     -jar target/spring-redis-crac-0.0.1-SNAPSHOT.jar
```

## 🏭 Production Deployment

### Docker Support

```dockerfile
FROM azul/zulu-openjdk:17-crac
COPY target/spring-redis-crac-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:CRaCCheckpointTo=/tmp/cr", "-jar", "app.jar"]
```

### Production Considerations

- Use external Redis cluster for scalability
- Configure SSL/TLS for Redis connections
- Set up monitoring and alerting
- Plan checkpoint storage and management
- Consider security implications of checkpoint files

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Boot Team**: For excellent CRaC integration
- **CRaC Project**: For coordinated restore at checkpoint technology
- **Redis Team**: For reliable caching infrastructure
- **Lettuce Team**: For CRaC-compatible Redis client

## 📞 Support

- **Documentation**: [Spring Boot CRaC Checkpoint-Restore Documentation](https://docs.spring.io/spring-boot/reference/packaging/checkpoint-restore.html)
- **CRaC Project**: [OpenJDK CRaC](https://openjdk.org/projects/crac/)
- **Issues**: Report bugs or request features via GitHub Issues or email me [Mrutyunjay Patil](mailto:patilmrutyunjay2@gmail.com)

---

**Built with ❤️ using Spring Boot 3.4.6 and Java CRaC**
