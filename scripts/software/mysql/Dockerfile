FROM mysql:8

#RUN localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8

ENV LANG zh_CN.utf8
ENV TZ Asia/Shanghai

COPY mysql.cnf /etc/mysql/conf.d/
COPY init.sql /docker-entrypoint-initdb.d/
