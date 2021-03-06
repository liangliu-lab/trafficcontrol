# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

############################################################
# Dockerfile to build Traffic Ops container images
# Based on CentOS 7.2
############################################################

# Keep the trafficops-common-deps in Dockerfile the same as
# trafficops-common-deps in Dockerfile-go to cache the same
# layer.
FROM centos:7 as trafficops-common-deps

RUN mkdir /etc/cron.d && \
    yum -y install https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm && \
    yum -y install epel-release && \
    yum -y install      \
        jq              \
        bind-utils      \
        net-tools       \
        gettext         \
        perl-JSON-PP    \
        mkisofs         \
        isomd5sum       \
        nmap-ncat       \
        openssl         \
        postgresql96 && \
    yum clean all

FROM trafficops-common-deps as trafficops-perl-deps

EXPOSE 443
ENV MOJO_MODE production

RUN yum install -y          \
        cpanminus           \
        expat-devel         \
        gcc-c++             \
        git                 \
        golang              \
        iproute             \
        jq                  \
        libcurl             \
        libcurl-devel       \
        libidn-devel        \
        libpcap-devel       \
        openssl-devel       \
        perl-core           \
        perl-Crypt-ScryptKDF\
        perl-DBD-Pg         \
        perl-DBI            \
        perl-DBIx-Connector \
        perl-Digest-SHA1    \
        perl-JSON           \
        perl-libwww-perl    \
        perl-TermReadKey    \
        perl-Test-CPAN-Meta \
        perl-WWW-Curl       \
        postgresql96-devel  \
        postgresql96-libs   \
        tar &&              \
    yum -y clean all && \
    mkdir -p /opt/traffic_ops/app/public && \
    cpanm Carton

ADD traffic_router/core/src/test/resources/geo/GeoLite2-City.mmdb.gz /opt/traffic_ops/app/public/

WORKDIR /opt/traffic_ops/app
ADD traffic_ops/app/cpanfile traffic_ops/install/bin/install_goose.sh ./

# Start with the existing traffic_ops/app/local directory
COPY infrastructure/cdn-in-a-box/traffic_ops/local local
RUN export POSTGRES_HOME=/usr/pgsql-9.6 PERL5LIB=$(pwd)/local/lib/perl5 && \
    echo 'Running carton...' && \
    [[ -d "${PERL5LIB}/x86_64-linux-thread-multi" ]]; existing_install=$? && \
    if ! carton && [[ $existing_install -eq 0 ]]; then \
        rm -rf "$PERL5LIB" && \
        echo 'Removed modules, retrying carton...' && \
        carton || exit 1; \
    fi && \
    rm -rf $HOME/.cpan* /tmp/Dockerfile /tmp/local.tar.gz ./cpanfile
RUN ./install_goose.sh

FROM trafficops-perl-deps
# Override TRAFFIC_OPS_RPM arg to use a different one using --build-arg TRAFFIC_OPS_RPM=...  Can be local file or http://...
ARG TRAFFIC_OPS_RPM=infrastructure/cdn-in-a-box/traffic_ops/traffic_ops.rpm
ADD $TRAFFIC_OPS_RPM /
RUN rpm -Uvh /$(basename $TRAFFIC_OPS_RPM) && \
    rm /$(basename $TRAFFIC_OPS_RPM) && \
    rm /opt/traffic_ops/app/bin/traffic_ops_golang

# Run carton again, in case the cpanfile included in the RPM differs from the one used earlier in the
# build (should never happen - Perl is supposed to be going away)
RUN POSTGRES_HOME=/usr/pgsql-9.6 PERL5LIB=$(pwd)/local/lib/perl5 carton && \
    rm -rf $HOME/.cpan* /tmp/Dockerfile /tmp/local.tar.gz

ADD infrastructure/cdn-in-a-box/enroller/server_template.json \
    infrastructure/cdn-in-a-box/traffic_ops/run.sh \
    infrastructure/cdn-in-a-box/traffic_ops/config.sh \
    infrastructure/cdn-in-a-box/traffic_ops/adduser.pl \
    infrastructure/cdn-in-a-box/traffic_ops/to-access.sh \
    infrastructure/cdn-in-a-box/traffic_ops/generate-certs.sh \
    infrastructure/cdn-in-a-box/traffic_ops/trafficops-init.sh \
    infrastructure/cdn-in-a-box/variables.env \
    /

COPY infrastructure/cdn-in-a-box/dns/set-dns.sh \
     infrastructure/cdn-in-a-box/dns/insert-self-into-dns.sh \
     infrastructure/cdn-in-a-box/traffic_ops/set-to-ips-from-dns.sh \
     /usr/local/sbin/

ADD infrastructure/cdn-in-a-box/traffic_ops_data /traffic_ops_data

CMD /run.sh
