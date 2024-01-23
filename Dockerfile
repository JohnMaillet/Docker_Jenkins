FROM jenkins/jenkins:2.414.3-jdk17
USER root
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
  https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
  https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli
RUN apt update -y 
RUN apt upgrade -y
RUN apt install build-essential zlib1g-dev libncurses5-dev libgdbm-dev libnss3-dev \
	libssl-dev libsqlite3-dev libreadline-dev libffi-dev curl libbz2-dev wget -y
RUN wget https://www.python.org/ftp/python/3.9.17/Python-3.9.17.tar.xz
RUN tar -xf Python-3.9.17.tar.xz
RUN cd Python-3.9.17 && \
	./configure --enable-optimizations --enable-shared && \
	make -j 4 && \
	make altinstall
RUN ldconfig
RUN apt-get update
RUN apt install -y python3-pip
RUN rm /usr/lib/python3.11/EXTERNALLY-MANAGED
RUN pip3 install aws-sam-cli
RUN alias python3=python3.9
USER jenkins
RUN jenkins-plugin-cli --plugins "blueocean:1.25.3 docker-workflow:1.28"