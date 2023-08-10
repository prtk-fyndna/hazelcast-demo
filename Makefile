
start-hazelcast:
	docker compose -f ./docker/hazelcast/docker-compose.yml up -d

stop-hazelcast:
	docker compose -f ./docker/hazelcast/docker-compose.yml down

start-kafka:
	docker compose -f ./docker/kafka/docker-compose.yml up -d

stop-kafka:
	docker compose -f ./docker/kafka/docker-compose.yml down

start-infra: start-hazelcast start-kafka

stop-infra: stop-hazelcast stop-kafka

build-jar:
	gradle build

start-producer:
	 dapr run --app-id hazelcast-demo-producer --components-path components \
	--app-port 10000 --dapr-http-port 3001 --metrics-port 30001 \
	-- java -jar -Xms1G -Xmx2G -Dspring.profiles.active=default ./build/libs/hazelcast-demo-0.0.1-SNAPSHOT.jar


start-consumer:
	 dapr run --app-id hazelcast-demo-consumer --components-path components \
	--app-port 10001 --dapr-http-port 3002 --metrics-port 30002 \
	-- java -jar -Xms1G -Xmx2G -Dspring.profiles.active=consumer ./build/libs/hazelcast-demo-0.0.1-SNAPSHOT.jar

start-txn:
	curl --location --request POST 'localhost:3001/v1.0/invoke/hazelcast-demo-producer/method/transaction/start' \
    --header 'Content-Type: application/json' \
    --data-raw '{"transactionId": "txn-1" }'