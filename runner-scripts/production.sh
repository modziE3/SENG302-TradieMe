fuser -k 10500/tcp || true
source production/.env

java -jar production/libs/home-helper-0.0.1-SNAPSHOT.jar \
    --server.port=10500 \
    --server.servlet.contextPath=/prod \
    --spring.application.name=home-helper \
    --spring.profiles.active=production