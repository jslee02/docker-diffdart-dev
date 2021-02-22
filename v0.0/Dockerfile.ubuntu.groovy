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
ENV version=3.19
ENV build=0
RUN apt-get -y remove cmake
RUN apt-get -y install wget libssl-dev \
    && wget -q https://github.com/Kitware/CMake/releases/download/v$version.$build/cmake-$version.$build.tar.gz \
    && tar -xzf cmake-$version.$build.tar.gz \
    && rm cmake-$version.$build.tar.gz \
    && pushd cmake-$version.$build \
    && ./bootstrap \
    && make -j$(nproc) \
    && make install \
    && popd \
    && rm -rf cmake-$version.$build

# Install Boost
RUN apt-get install -qq -y --no-install-recommends \
    libboost-dev

# Install Eigen
RUN curl https://gitlab.com/libeigen/eigen/-/archive/3.3.7/eigen-3.3.7.tar.gz > eigen.tar.gz && \
    tar -zxf eigen.tar.gz && \
    pushd eigen-3.3.7 && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j14 && \
    popd && \
    popd && \
    rm -rf eigen-3.3.7

# Install CCD
RUN git clone https://github.com/danfis/libccd.git && \
    pushd libccd && \
    mkdir build && \
    pushd build && \
    cmake .. -DENABLE_DOUBLE_PRECISION=ON && \
    make install -j14 && \
    popd && \
    popd && \
    rm -rf libccd

# Install ASSIMP
RUN git clone https://github.com/assimp/assimp.git && \
    pushd assimp && \
    git checkout v5.0.1 && \
    mkdir build && \
    pushd build && \
    # cmake .. -DASSIMP_BUILD_ZLIB=ON -DASSIMP_BUILD_TESTS=ON -DASSIMP_BUILD_ASSIMP_TOOLS=OFF -DCMAKE_BUILD_TYPE=Debug && \
    cmake .. -DASSIMP_BUILD_ZLIB=ON -DASSIMP_BUILD_TESTS=ON -DASSIMP_BUILD_ASSIMP_TOOLS=OFF && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf assimp

# Install LAPACK
RUN yum install -y lapack-devel

# Install MUMPS
RUN git clone https://github.com/coin-or-tools/ThirdParty-Mumps.git && \
    pushd ThirdParty-Mumps && \
    ./get.Mumps && \
    ./configure && \
    make -j14 && \
    make install && \
    popd && \
    rm -rf ThirdParty-Mumps

# Install IPOPT
RUN git clone https://github.com/coin-or/Ipopt.git && \
    pushd Ipopt && \
    ./configure --with-mumps && \
    make -j14 && \
    make install && \
    popd && \
    rm -rf Ipopt && \
    ln -s /usr/local/include/coin-or /usr/local/include/coin

# Install FCL
# Key note: this needs to happen before octomap
RUN git clone https://github.com/flexible-collision-library/fcl.git && \
    pushd fcl && \
    git checkout 0.3.4 && \
    # vi include/fcl/narrowphase/detail/convexity_based_algorithm/gjk_libccd-inl.h:1696 # "std::max(1.0, v0_dist)" -> "std::max(1.0, (double)v0_dist)" && \
    # sed -i '1696s/v0_dist/(double)v0_dist/' include/fcl/narrowphase/detail/convexity_based_algorithm/gjk_libccd-inl.h && \
    mkdir build && \
    pushd build && \
    cmake .. -DFCL_WITH_OCTOMAP=OFF && \
    make install -j14 && \
    popd && \
    popd && \
    rm -rf fcl

# Install octomap
RUN git clone https://github.com/OctoMap/octomap.git && \
    pushd octomap && \
    git checkout v1.8.1 && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf octomap

# Install tinyxml2
RUN git clone https://github.com/leethomason/tinyxml2.git && \
    pushd tinyxml2 && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf tinyxml2

# Install freeglut
# RUN yum install -y libXi-devel && \
#     yum install -y mesa-libGLU-devel && \
#     yum install -y libXmu-devel && \
#     curl https://managedway.dl.sourceforge.net/project/freeglut/freeglut/3.2.1/freeglut-3.2.1.tar.gz > freeglut.tar.gz && \
#     tar -zxf freeglut.tar.gz && \
#     rm freeglut.tar.gz && \
#     pushd freeglut-3.2.1 && \
#     mkdir build && \
#     pushd build && \
#     cmake .. && \
#     make install -j10 && \
#     popd && \
#     popd && \
#     rm -rf freeglut-3.2.1

# Install Open Scene Graph
# RUN git clone https://github.com/openscenegraph/OpenSceneGraph.git && \
#     pushd OpenSceneGraph && \
#     git checkout OpenSceneGraph-3.6.5 && \
#     mkdir build && \
#     pushd build && \
#     cmake .. && \
#     make install -j10 && \
#     popd && \
#     popd && \
#     rm -rf OpenSceneGraph

# Install tinyxml1
RUN git clone https://github.com/robotology-dependencies/tinyxml.git && \
    pushd tinyxml && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf tinyxml

# Install urdfdom_headers
RUN git clone https://github.com/ros/urdfdom_headers.git && \
    pushd urdfdom_headers && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf urdfdom_headers

# Install console_bridge
RUN git clone https://github.com/ros/console_bridge.git && \
    pushd console_bridge && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf console_bridge

# Install urdfdom
RUN git clone https://github.com/ros/urdfdom.git && \
    pushd urdfdom && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install -j10 && \
    popd && \
    popd && \
    rm -rf urdfdom

# Install PerfUtils
RUN git clone https://github.com/PlatformLab/PerfUtils.git && \
    pushd PerfUtils && \
    sed -i 's/3.11/3.6.1/g' CMakeLists.txt && \
    sed -i '94,$d' CMakeLists.txt && \
    sed -i '30,33d' CMakeLists.txt && \
    sed -i '36i\ \ \ \ CXX_STANDARD 11' CMakeLists.txt && \
    sed -i '36i\ \ \ \ CXX_STANDARD_REQUIRED YES' CMakeLists.txt && \
    sed -i '36i\ \ \ \ CXX_EXTENSIONS NO' CMakeLists.txt && \
    mkdir build && \
    pushd build && \
    cmake .. && \
    make install && \
    popd && \
    popd && \
    rm -rf PerfUtils

ENV PROTOBUF_VERSION="3.14.0"

# RUN wget https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOBUF_VERSION}/protobuf-all-${PROTOBUF_VERSION}.tar.gz && \
#     tar -xvzf protobuf-all-${PROTOBUF_VERSION}.tar.gz && \
#     rm protobuf-all-${PROTOBUF_VERSION}.tar.gz && \
#     pushd protobuf-${PROTOBUF_VERSION} && \
#     ./configure && \
#     make -j16 && \
#     make check -j16 && \
#     make install && \ 
#     popd && \
#     rm -rf protobuf-${PROTOBUF_VERSION}

RUN git clone --recurse-submodules -b v1.33.2 https://github.com/grpc/grpc
RUN pushd grpc && \
    # This fixes the boringssl build on the ancient CentOS we have to use by adding "rt" as an explicit dependency
    sed -i '642s/.*/target_link_libraries(bssl ssl crypto rt)/' third_party/boringssl-with-bazel/CMakeLists.txt && \
    pushd third_party/protobuf && \
    git checkout v${PROTOBUF_VERSION} && \
    popd && \
    mkdir -p cmake/build && \
    pushd cmake/build && \
    cmake -DgRPC_INSTALL=ON \
          -DgRPC_BUILD_TESTS=OFF \
          ../.. && \
    make -j && \
    make install && \
    popd && \
    popd
RUN rm -rf grpc

# Install Google benchmark
RUN git clone https://github.com/google/benchmark.git
RUN git clone https://github.com/google/googletest.git benchmark/googletest
RUN pushd benchmark && \
    mkdir build && \
    pushd build && \
    cmake -DCMAKE_BUILD_TYPE=Release .. && \
    make install && \
    popd && \
    popd
RUN rm -rf benchmark

RUN protoc --version

RUN rm -rf /var/lib/apt/lists/*
