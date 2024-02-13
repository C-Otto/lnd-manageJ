FROM gradle:latest
RUN apt-get update
RUN apt-get install postgresql sudo -y

RUN git clone https://github.com/C-Otto/lnd-manageJ.git
WORKDIR lnd-manageJ
RUN gradle application:bootJar

RUN mkdir -p /root/.config
RUN echo "[lnd]" >> /root/.config/lnd-manageJ.conf
RUN echo "host=localhost" >> /root/.config/lnd-manageJ.conf
RUN echo "macaroon_file=/root/.lnd/data/chain/bitcoin/testnet/admin.macaroon" >> /root/.config/lnd-manageJ.conf

EXPOSE 8081
RUN echo "server.address=0.0.0.0" >> /root/override.properties
CMD /etc/init.d/postgresql start && \
    (sudo -u postgres psql -c "CREATE USER bitcoin WITH PASSWORD 'unset'" || true) && \
    (sudo -u postgres createdb lndmanagej -O bitcoin || true) && \
    java -jar application/build/libs/application-boot.jar --spring.config.location=classpath:application.properties,/root/override.properties

# docker build -t lnd-managej .
# docker run --network host -v /home/xxx/.lnd/:/root/.lnd lnd-managej
