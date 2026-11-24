FROM eclipse-temurin:17-jdk

# Cài Cppcheck và các dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    cppcheck \
    python3 \
    python3-venv \
    curl \
    ca-certificates \
 && rm -rf /var/lib/apt/lists/*

# Cài Bandit và Flake8
RUN python3 -m venv /opt/venv \
 && /opt/venv/bin/pip install --no-cache-dir \
    bandit==1.7.10 \
    flake8==7.1.1


# Cài SpotBugs
ARG SPOTBUGS_VERSION=4.8.6

RUN curl -fsSL \
    https://github.com/spotbugs/spotbugs/releases/download/${SPOTBUGS_VERSION}/spotbugs-${SPOTBUGS_VERSION}.tgz \
    | tar xz -C /opt \
 && chmod +x /opt/spotbugs-${SPOTBUGS_VERSION}/bin/spotbugs \
 && ln -s /opt/spotbugs-${SPOTBUGS_VERSION} /opt/spotbugs


ENV PATH="/opt/venv/bin:/opt/spotbugs/bin:${PATH}"

WORKDIR /src
