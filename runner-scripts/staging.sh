fuser -k 9500/tcp || true
source staging/.env


java -jar staging/libs/home-helper-0.0.1-SNAPSHOT.jar \
    --server.port=9500 \
    --server.servlet.contextPath=/test \
    --spring.application.name=home-helper \
    --spring.profiles.active=staging
