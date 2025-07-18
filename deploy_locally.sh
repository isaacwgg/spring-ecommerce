#!/bin/bash
set -e  # Exit on any error

echo "Building services..."
mvn clean package -DskipTests

echo "Stopping existing containers..."
docker-compose down

echo "Cleaning up images..."
# More specific image cleanup for your services
docker-compose rm -f
docker rmi -f $(docker images 'ecommerce-*' -q) 2>/dev/null || true

echo "Starting services..."
docker-compose up --build -d

echo "Services are starting up. Viewing logs..."
# Show logs from all services
docker-compose logs -f