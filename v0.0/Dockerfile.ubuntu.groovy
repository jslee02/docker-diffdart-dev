FROM --platform=$TARGETPLATFORM ubuntu:groovy

ARG DEBIAN_FRONTEND=noninteractive

# Use /bin/bash to use pushd/popd
SHELL ["/bin/bash", "-c"]

# Update apt-get
RUN apt-get update -qq

# ==============================================================================
# Build tools
# ==============================================================================
RUN apt-get install -qq -y --no-install-recommends \
    build-essential \
    clang \
    curl \
    doxygen \
    git \
    lcov \
    lsb-release \
    pkg-config \
    software-properties-common \
    valgrind

# CMake
RUN apt-get install -qq -y --no-install-recommends cmake

RUN rm -rf /var/lib/apt/lists/*
