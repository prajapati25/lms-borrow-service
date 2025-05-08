# Borrow Service

A microservice for managing book borrows in a Library Management System. This service handles book borrowing, returns, extensions, and overdue tracking.

## Features

- Book borrowing and return management
- Due date extensions
- Overdue tracking and fine calculation
- Integration with Book and User services
- Event publishing for notifications
- RESTful API with OpenAPI documentation
- Kubernetes deployment support

## Prerequisites

- Java 17
- Maven
- Docker and Docker Compose
- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl CLI
- PostgreSQL
- Kafka

## Deployment Steps

### 1. Local Development Setup

1. Clone the repository:
```bash
git clone https://github.com/prajapati25/lms-borrow-service.git
cd borrow-service
```

2. Start the required services using Docker Compose:
```bash
docker-compose up -d
```

3. Build and run the application:
```bash
mvn clean install
mvn spring-boot:run
```

The service will start on port 8082.

### 2. Kubernetes Deployment

1. Ensure your Kubernetes cluster is running and kubectl is configured:
```bash
kubectl cluster-info
```

2. Create the required namespaces:
```bash
kubectl create namespace library-system
```

3. Deploy PostgreSQL:
```bash
kubectl apply -f k8s/postgres-pv.yaml
kubectl apply -f k8s/postgres-pvc.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/postgres-service.yaml
```

4. Deploy Kafka:
```bash
kubectl apply -f k8s/kafka-pv.yaml
kubectl apply -f k8s/kafka-pvc.yaml
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/kafka-service.yaml
```

5. Deploy the Borrow Service:
```bash
kubectl apply -f k8s/borrow-service-deployment.yaml
kubectl apply -f k8s/borrow-service-service.yaml
```

6. Verify the deployments:
```bash
kubectl get pods -n library-system
kubectl get services -n library-system
```

7. Access the service:
```bash
# Get the NodePort
kubectl get svc borrow-service -n library-system
```

The service will be accessible at `http://<node-ip>:<node-port>/api`

### 3. Monitoring and Troubleshooting

1. Check pod status:
```bash
kubectl get pods -n library-system
```

2. View pod logs:
```bash
kubectl logs -f <pod-name> -n library-system
```

3. Check service endpoints:
```bash
kubectl get endpoints -n library-system
```

4. Verify database connection:
```bash
kubectl exec -it <postgres-pod-name> -n library-system -- psql -U postgres -d library_borrow
```

5. Check Kafka topics:
```bash
kubectl exec -it <kafka-pod-name> -n library-system -- kafka-topics.sh --list --bootstrap-server localhost:9092
```

## API Documentation

Once the application is running, you can access the Swagger UI at:
```
http://<node-ip>:<node-port>/api/swagger-ui.html
```

## API Endpoints

- `GET /api/borrows` - List all borrow records (paginated)
- `GET /api/borrows/{id}` - Get borrow record details
- `POST /api/borrows` - Create a new borrow record
- `POST /api/borrows/{id}/return` - Process book return
- `GET /api/borrows/user/{userId}` - Get borrowing history for a user
- `GET /api/borrows/book/{bookId}` - Get borrowing history for a book
- `GET /api/borrows/overdue` - List overdue borrowings
- `POST /api/borrows/{id}/extend` - Extend due date

## Configuration

The service can be configured through environment variables or Kubernetes ConfigMaps. The default configuration is provided in the `application.properties` file.

### Kubernetes Configuration

1. Create ConfigMap:
```bash
kubectl create configmap borrow-service-config --from-file=src/main/resources/application.properties -n library-system
```

2. Create Secrets:
```bash
kubectl create secret generic borrow-service-secrets \
  --from-literal=SPRING_DATASOURCE_PASSWORD=postgres \
  --from-literal=JWT_SECRET=your-256-bit-secret \
  -n library-system
```

## Development

### Building

```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

### Building Docker Image
```bash
docker build -t borrow-service:latest .
```

## Troubleshooting Guide

1. If pods are not starting:
   - Check pod logs: `kubectl logs <pod-name> -n library-system`
   - Verify ConfigMaps and Secrets: `kubectl describe configmap borrow-service-config -n library-system`
   - Check resource limits: `kubectl describe pod <pod-name> -n library-system`

2. If services are not accessible:
   - Verify service endpoints: `kubectl get endpoints -n library-system`
   - Check service configuration: `kubectl describe service borrow-service -n library-system`
   - Test connectivity: `kubectl exec -it <pod-name> -n library-system -- curl localhost:8082/api/health`

3. If database connection fails:
   - Check PostgreSQL logs: `kubectl logs <postgres-pod-name> -n library-system`
   - Verify database credentials in secrets
   - Test database connection from pod: `kubectl exec -it <pod-name> -n library-system -- nc -zv postgres 5432`

4. If Kafka connection fails:
   - Check Kafka logs: `kubectl logs <kafka-pod-name> -n library-system`
   - Verify Kafka service: `kubectl get svc kafka -n library-system`
   - Test Kafka connectivity: `kubectl exec -it <pod-name> -n library-system -- nc -zv kafka 9092`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Logging Configuration

The service uses SLF4J with Logback for logging. Logs are written to both console and file.

### Log Files
- Main log file: `./logs/borrow-service.log`
- Archived logs: `./logs/archived/borrow-service-YYYY-MM-DD.i.log`

### Log Levels
- Application code (com.bits.borrowservice): DEBUG
- Spring Web: INFO
- Hibernate: INFO
- Root level: INFO

### Log Format
Console:
```
2024-03-14 10:15:30,123 DEBUG [http-nio-8082-exec-1] BookServiceClient: Checking availability for book ID: 123
```

File:
```
2024-03-14 10:15:30,123 DEBUG BookServiceClient [http-nio-8082-exec-1] Checking availability for book ID: 123
```

### Logging Configuration
The logging configuration can be modified in `src/main/resources/logback-spring.xml`. Key features:
- Console appender with colored output
- Rolling file appender with size-based rotation
- 30 days log retention
- 10MB maximum file size
- Separate log levels for different packages 